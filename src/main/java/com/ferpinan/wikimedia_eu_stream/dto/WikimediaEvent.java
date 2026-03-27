package com.ferpinan.wikimedia_eu_stream.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WikimediaEvent {

    @JsonProperty("data")
    private String data;  // es un String, no un objeto

    // getter

    public String getData() {
        return data;
    }
}
