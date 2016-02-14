package com.home.index;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.home.common.LogDocument;
import com.home.extract.SkypeLogDocExtractor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;

import java.util.concurrent.TimeUnit;

public class LogIndexer {

    private static final Log logger = LogFactory.getLog(LogIndexer.class);

    private ObjectMapper objectMapper;

    private SkypeLogDocExtractor skypeLogDocExtractor;

    private String index;

    private String type;

    private Client esClient;

    BulkProcessor bulkProcessor;

    private int docIndex;

    /**
     * Constructs a new LogIndexer object.
     *
     * @param skypeLogDocExtractor Object of type {@link SkypeLogDocExtractor} used to extract documents from logs.
     */
    public LogIndexer(final SkypeLogDocExtractor skypeLogDocExtractor, final String index, final String type) {
        validateVariables(skypeLogDocExtractor, index, type);
        this.skypeLogDocExtractor = skypeLogDocExtractor;
        this.index           = index;
        this.type            = type;
        this.docIndex        = 0;
        createStandardMapper();
    }

    private void validateVariables(final SkypeLogDocExtractor skypeLogDocExtractor, final String index, final String type) {
        if (skypeLogDocExtractor == null || index == null || type == null)
            throw new NullPointerException("Null parameter provided!");
    }

    /**
     * Index all documents.
     */
    public void indexDocs() {
        indexDocs(Integer.MAX_VALUE);
    }

    /**
     * Index specified number of documents.
     * @param maxDocs The maximum number of documents to index.
     */
    public void indexDocs(int maxDocs) {
        this.createBulkProcessor();
        int docCounter = 0;
        while(docCounter++ < maxDocs && skypeLogDocExtractor.hasNext()) {
            indexDoc(skypeLogDocExtractor.current());
            skypeLogDocExtractor.parseNext();
        }
        this.closeBulkProccessor();
    }

    private void indexDoc(LogDocument doc) {
        try {
            String json = objectMapper.writeValueAsString(doc);
            bulkProcessor.add(new IndexRequest(this.index, this.type, String.valueOf(docIndex++)).source(json));
        }
        catch(JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void createBulkProcessor() {
        bulkProcessor = BulkProcessor.builder(
                esClient,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId,
                                           BulkRequest request) {
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          BulkResponse response) {
                        logger.info(response.getItems().length + " elements processed");
                        for (BulkItemResponse item : response.getItems()) {
                            if (item.isFailed()) {
                                logger.error(item.getId() + " " + item.getFailureMessage());
                            }
                        }
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          Throwable failure) {
                        logger.error("Error on after bulk method: " + failure);
                    }
                })
                .setBulkActions(1000)
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
                .setConcurrentRequests(1)
                .build();
    }

    private void closeBulkProccessor() {
        try {
            bulkProcessor.flush();
            bulkProcessor.awaitClose(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Error while waiting to close bulk processor: " + e);
        }
    }

    /**
     * Sets a ES client for indexing.
     * @param esClient The client to set.
     */
    public void setClient(Client esClient) {
        assert esClient != null;
        this.esClient = esClient;
    }

    private void createStandardMapper() {
        objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JodaModule());
        // parser to close resources autonomously
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS , false);
    }

    static {
        BasicConfigurator.configure();
    }
}
