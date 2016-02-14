package com.home.extract;

import com.home.common.LogDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class extracts documents using {@link SkypeLogDocExtractor} and writes them to disk.
 */
public class DiskDocumentWritter {

    private static final Log logger = LogFactory.getLog(DiskDocumentWritter.class);

    private SkypeLogDocExtractor skypeLogDocExtractor;

    private long count;

    private long maxNofDoc;

    private String prefix;

    private String outputDir;

    /**
     * Constructs a disk document writer object.
     *
     * @param skypeLogDocExtractor Object of type {@link SkypeLogDocExtractor} used to extract documents from logs.
     */
    public DiskDocumentWritter(final SkypeLogDocExtractor skypeLogDocExtractor) {
        if (skypeLogDocExtractor == null)
            throw new NullPointerException("Null parameter provided!");
        this.skypeLogDocExtractor = skypeLogDocExtractor;
    }

    /**
     * Saves a number of documents to a provided directory.
     *
     * @param outputDir The output path to write the documnts.
     * @param prefix Each file will begin with this prefix followed by a count.
     * @param maxNofDocs Maximum number of document to save to the provided directory.
     */
    public void saveDocuments(final String outputDir, final String prefix, final long maxNofDocs) {
        setAndValidateVars(outputDir, prefix, maxNofDocs);
        while(moreDocsToSave()) {
            saveDocument(skypeLogDocExtractor.current());
            skypeLogDocExtractor.parseNext();
        }
    }

    private void setAndValidateVars(final String outputDir, final String prefix, final long maxNofDocs) {
        if (outputDir == null || prefix == null)
            throw new IllegalArgumentException("Null argument provided!");
        if (maxNofDocs < 0)
            throw new IllegalArgumentException("Max number of documents must be non-negative!");
        this.prefix    = prefix;
        this.count     = 0;
        this.outputDir = ensureSeparator(outputDir);
        this.maxNofDoc = maxNofDocs;
    }

    private boolean moreDocsToSave() {
        return count < maxNofDoc && skypeLogDocExtractor.hasNext();
    }

    private String ensureSeparator(String path) {
        assert path != null;
        String pathWithSeparator;
        if (path.charAt(path.length() - 1) != File.separatorChar)
            pathWithSeparator = path + File.separatorChar;
        else
            pathWithSeparator = path;
        return pathWithSeparator;
    }

    private void saveDocument(LogDocument doc) {
        BufferedWriter writer = null;
        try {
            String filename = outputDir + prefix + String.valueOf(++count);
            writer = new BufferedWriter(new FileWriter(filename));
            writer.write(doc.toString());
        }
        catch(IOException e) {
            logger.error("Can't write to file: " + e);
        }
        finally {
            if (writer != null)
                try {
                    writer.close();
                }
                catch(IOException e) {
                }
        }
    }

    static {
        BasicConfigurator.configure();
    }
}
