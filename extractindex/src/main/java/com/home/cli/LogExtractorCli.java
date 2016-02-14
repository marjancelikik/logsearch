package com.home.cli;

import com.home.extract.DiskDocumentWritter;
import com.home.extract.SkypeLogDocExtractor;
import com.home.index.LogIndexer;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LogExtractorCli implements CommandLineRunner {

    private final Client client;

    @Autowired
    public LogExtractorCli(Client client) {
        this.client = client;
    }

    @Override
    public void run(String... args) throws IOException {
        if (args.length == 5 && args[0].equals("save")) {
            SkypeLogDocExtractor skypeLogDocExtractor = SkypeLogDocExtractor.getInstance(args[1], Integer.parseInt(args[2]));
            DiskDocumentWritter diskDocumentWritter = new DiskDocumentWritter(skypeLogDocExtractor);
            diskDocumentWritter.saveDocuments(args[3], args[4], Integer.MAX_VALUE);
        } else if (args.length == 3 && args[0].equals("index")) {
            SkypeLogDocExtractor skypeLogDocExtractor = SkypeLogDocExtractor.getInstance(args[1], Integer.parseInt(args[2]));
            LogIndexer logIndexer = new LogIndexer(skypeLogDocExtractor, "logindex", "skype");
            logIndexer.setClient(client);
            logIndexer.indexDocs();
        } else {
            printUsage();
        }
    }

    private void printUsage() {
        System.out.println("usage: LogExtractor [command] [params]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("index - extract logs and index them using elastic search");
        System.out.println("save  - extract logs and save them to disk");
        System.out.println();
        System.out.println("Params for save:");
        System.out.println("param1: path to log file");
        System.out.println("param2: max number of minutes between two conversations");
        System.out.println("param3: output folder");
        System.out.println("param4: prefix for each output file (e.g. doc for doc0, doc1, etc.)");
        System.out.println();
        System.out.println("Params for index:");
        System.out.println("param1: path to log file");
        System.out.println("param2: max number of minutes between two conversations");
    }
}
