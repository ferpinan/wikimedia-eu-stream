package com.ferpinan.wikimedia_eu_stream.topology;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferpinan.wikimedia_eu_stream.dto.WikimediaChange;
import com.ferpinan.wikimedia_eu_stream.dto.WikimediaEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
public class WikimediaEuFilterTopology {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WikimediaChange parseChange(String rawMessage) {
        try {
            WikimediaEvent event = objectMapper.readValue(rawMessage, WikimediaEvent.class);
            return objectMapper.readValue(event.getData(), WikimediaChange.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Bean
    public KStream<String, String> euWikiUrlStream(StreamsBuilder builder) {
        KStream<String, String> input = builder.stream(
                "wikimedia.recentchange",
                Consumed.with(Serdes.String(), Serdes.String())
        );

        input
                .filter((key, value) -> {
                    WikimediaChange wikimediaChange = parseChange(value);
                    return wikimediaChange != null  && "euwiki".equals(wikimediaChange.getWiki());
                })
                .to("wikimedia.recentchange.eu", Produced.with(Serdes.String(), Serdes.String()));

        return input;
    }
}
