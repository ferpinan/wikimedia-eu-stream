package com.ferpinan.wikimedia_eu_stream.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Mapper for parsing Wikimedia SSE event messages.
 * <p>
 * Extracts structured data from raw JSON messages received from the Wikimedia
 * recent changes event stream. The expected message structure is:
 * <pre>
 * {
 *   "data": "{\"wiki\": \"euwiki\", ...}"
 * }
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class WikimediaMessageMapper {

    public static final String DATA_PATH = "data";
    public static final String WIKI_PATH = "wiki";

    private final ObjectMapper objectMapper;

    /**
     * Extracts the wiki identifier from a raw Wikimedia SSE message.
     * @param rawMessage the raw JSON message from Wikimedia SSE stream
     * @return an {@link Optional} containing the wiki ID (e.g., "euwiki", "enwiki"),
     *         or {@link Optional#empty()} if parsing fails or the field is missing
     */
    public Optional<String> extractWikiId(String rawMessage) {
        try {
            JsonNode root = objectMapper.readTree(rawMessage);
            String data = root.path(DATA_PATH).asText();
            String wikiId = objectMapper.readTree(data).path(WIKI_PATH).asText(null);
            return Optional.ofNullable(wikiId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
