package com.ferpinan.wikimedia_eu_stream.topology;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferpinan.wikimedia_eu_stream.mapper.WikimediaMessageMapper;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikimediaEuFilterTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    @BeforeEach
    void setUp() {
        // Configure components
        WikimediaMessageMapper mapper = new WikimediaMessageMapper(new ObjectMapper());

        // Build topology
        StreamsBuilder builder = new StreamsBuilder();
        WikimediaEuFilterTopology topology = new WikimediaEuFilterTopology(
                "euwiki",
                "wikimedia.recentchange",
                "wikimedia.recentchange.eu",
                mapper
        );
        topology.euWikiUrlStream(builder);

        // Create test driver with the built topology
        Properties props = new Properties();
        props.put("application.id", "test");
        props.put("bootstrap.servers", "dummy:1234");
        testDriver = new TopologyTestDriver(builder.build(), props);

        // Create test topics
        inputTopic = testDriver.createInputTopic(
                "wikimedia.recentchange",
                new StringSerializer(),
                new StringSerializer()
        );

        outputTopic = testDriver.createOutputTopic(
                "wikimedia.recentchange.eu",
                new StringDeserializer(),
                new StringDeserializer()
        );
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldFilterEuWikiEvents() {
        String euWikiMessage = """
                {
                    "data": "{\\"wiki\\":\\"euwiki\\",\\"type\\":\\"edit\\"}"
                }
                """;

        inputTopic.pipeInput("key1", euWikiMessage);

        assertEquals(1, outputTopic.getQueueSize());
        assertEquals(euWikiMessage, outputTopic.readValue());
    }

    @Test
    void shouldFilterOutNonEuWikiEvents() {
        String enWikiMessage = """
                {
                    "data": "{\\"wiki\\":\\"enwiki\\",\\"type\\":\\"edit\\"}"
                }
                """;

        inputTopic.pipeInput("key1", enWikiMessage);

        assertTrue(outputTopic.isEmpty());
    }

    @Test
    void shouldFilterOutInvalidMessages() {
        String invalidMessage = "{ invalid json }";

        inputTopic.pipeInput("key1", invalidMessage);

        assertTrue(outputTopic.isEmpty());
    }

    @Test
    void shouldFilterOutMessagesWithMissingWikiField() {
        String messageWithoutWiki = """
                {
                    "data": "{\\"type\\":\\"edit\\"}"
                }
                """;

        inputTopic.pipeInput("key1", messageWithoutWiki);

        assertTrue(outputTopic.isEmpty());
    }

    @Test
    void shouldProcessMultipleMessages() {
        String euMessage1 = """
                {
                    "data": "{\\"wiki\\":\\"euwiki\\",\\"type\\":\\"edit\\"}"
                }
                """;
        String enMessage = """
                {
                    "data": "{\\"wiki\\":\\"enwiki\\",\\"type\\":\\"edit\\"}"
                }
                """;
        String euMessage2 = """
                {
                    "data": "{\\"wiki\\":\\"euwiki\\",\\"type\\":\\"new\\"}"
                }
                """;

        inputTopic.pipeInput("key1", euMessage1);
        inputTopic.pipeInput("key2", enMessage);
        inputTopic.pipeInput("key3", euMessage2);

        assertEquals(2, outputTopic.getQueueSize());
        assertEquals(euMessage1, outputTopic.readValue());
        assertEquals(euMessage2, outputTopic.readValue());
    }
}