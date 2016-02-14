package com.home;

import com.home.index.EsFactory;
import com.home.index.EsSettings;
import org.elasticsearch.client.Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App {

    @Bean
    public Client esClient() {
        return EsFactory.getClient(EsSettings.getLocalSettings());
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(App.class);
        app.setWebEnvironment(false);
        app.run(args);
    }
}
