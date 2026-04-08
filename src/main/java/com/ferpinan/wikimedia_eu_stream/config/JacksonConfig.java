package com.ferpinan.wikimedia_eu_stream.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson configuration for JSON serialization/deserialization.
 * <p>
 * Provides a customized {@link ObjectMapper} bean with lenient deserialization.
 */
@Configuration
public class JacksonConfig {

    /**
     * Creates and configures an {@link ObjectMapper} bean.
     * <p>
     * Configuration:
     * <ul>
     *   <li>Ignores unknown JSON properties during deserialization</li>
     * </ul>
     *
     * @return configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Don't fail on unknown properties in JSON
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}