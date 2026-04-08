package com.ferpinan.wikimedia_eu_stream.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class WikimediaMessageMapperTest {

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private WikimediaMessageMapper mapper;

    @ParameterizedTest
    @CsvSource({
            "euwiki, '{\"data\": \"{\\\"wiki\\\":\\\"euwiki\\\",\\\"type\\\":\\\"editt\\\"}\"}' ",
            "enwiki, '{\"data\": \"{\\\"wiki\\\":\\\"enwiki\\\"}\" }' ",
            "eswiki, '{\"event\": \"message\", \"id\": \"123\", \"data\": \"{\\\"wiki\\\":\\\"eswiki\\\",\\\"server_name\\\":\\\"es.wikipedia.org\\\",\\\"type\\\":\\\"edit\\\"}\"}' "
    })
    void shouldExtractWikiIdFromValidMessage(String expectedWikiId, String rawMessage) {
        Optional<String> result = mapper.extractWikiId(rawMessage);

        assertTrue(result.isPresent());
        assertEquals(expectedWikiId, result.get());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "{\"data\": \"{\\\"type\\\":\\\"edit\\\"}\"}",  // wiki field missing
            "{\"event\": \"change\"}",  // data field missing
            "{ invalid json }",  // invalid JSON
            "{\"data\": \"not a json string\"}"  // data is not valid JSON
    })
    void shouldReturnEmptyForInvalidMessages(String rawMessage) {
        Optional<String> result = mapper.extractWikiId(rawMessage);

        assertTrue(result.isEmpty());
    }

}