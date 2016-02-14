package com.home.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a document extracted from a log file.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogDocument {

    private List<String> text = new ArrayList<>();

    private String id = new String();

    public LogDocument(String id) {
        this.id = id;
    }

    public LogDocument(int id) {
        this.id = String.valueOf(id);
    }

    @JsonProperty("text")
    public List<String> getText() {
        return text;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void addLine(String line) {
        text.add(line.replaceAll("/", ""));
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        text.forEach(x -> { stringBuffer.append(x); stringBuffer.append(System.lineSeparator()); });
        return stringBuffer.toString();
    }
}
