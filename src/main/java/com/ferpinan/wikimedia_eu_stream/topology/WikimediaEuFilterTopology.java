package com.ferpinan.wikimedia_eu_stream.topology;

import com.ferpinan.wikimedia_eu_stream.mapper.WikimediaMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.Optional;

/**
 * Kafka Streams topology for filtering Wikimedia recent changes by language.
 * <p>
 * This topology consumes raw Wikimedia SSE events from a source topic, extracts
 * the wiki identifier, filters events matching the configured language, and
 * publishes them to a dedicated sink topic.
 * <p>
 * The filtering is language-agnostic and configurable via application properties.
 */
@Configuration
@EnableKafkaStreams
@Slf4j
public class WikimediaEuFilterTopology {

    /**
     * Wiki identifier to filter (e.g., "euwiki", "enwiki", "eswiki").
     * Configurable via {@code wikimedia.lang-wiki-id} property.
     */
    private final String langWikiId;

    /**
     * Source Kafka topic containing all Wikimedia recent changes.
     * Configurable via {@code wikimedia.source-topic} property.
     */
    private final String sourceTopic;

    /**
     * Sink Kafka topic for filtered language-specific events.
     * Configurable via {@code wikimedia.sink-topic} property.
     */
    private final String sinkTopic;

    private final WikimediaMessageMapper wikimediaMessageMapper;

    public WikimediaEuFilterTopology(
            @Value("${wikimedia.lang-wiki-id:euwiki}") String langWikiId,
            @Value("${wikimedia.source-topic:wikimedia.recentchange}") String sourceTopic,
            @Value("${wikimedia.sink-topic:wikimedia.recentchange.eu}") String sinkTopic,
            WikimediaMessageMapper wikimediaMessageMapper) {
        this.langWikiId = langWikiId;
        this.sourceTopic = sourceTopic;
        this.sinkTopic = sinkTopic;
        this.wikimediaMessageMapper = wikimediaMessageMapper;
    }

    /**
     * Builds the Kafka Streams topology for filtering Wikimedia events.
     * <p>
     * Processing steps:
     * <ol>
     *   <li>Extract wiki ID from raw SSE message</li>
     *   <li>Filter out messages where extraction failed</li>
     *   <li>Unwrap Optional to get the wiki ID string</li>
     *   <li>Filter by configured language wiki ID</li>
     *   <li>Publish filtered events to sink topic</li>
     * </ol>
     *
     * @param builder the Kafka Streams builder
     * @return the input KStream for potential further processing
     */
    @Bean
    public KStream<String, String> euWikiUrlStream(StreamsBuilder builder) {
        KStream<String, String> input = builder.stream(
                sourceTopic,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        input
                .filter((key, value) -> {
                    // TODO: Optimize filtering. Consider raw string matching (e.g., rawMessage.contains("\"wiki\":\"euwiki\"")) or a streaming JSON parser to avoid loading the full JSON tree into memory.
                    Optional<String> wikiId = wikimediaMessageMapper.extractWikiId(value);
                    return wikiId.isPresent() && langWikiId.equals(wikiId.get());
                })
                .peek((key, value) -> log.debug("Filtered message: {}", value))
                .to(sinkTopic, Produced.with(Serdes.String(), Serdes.String()));

        return input;
    }
}