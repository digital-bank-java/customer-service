package com.digitalbank.customerservice;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.logback.StructuredLogEncoder;
import org.springframework.mock.env.MockEnvironment;

class FlatStructuredLoggingJsonMembersCustomizerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void emitsFlatContractFieldsAndPreservesEcsMembers() throws Exception {
        var environment = new MockEnvironment()
                .withProperty("spring.application.name", "customer-service")
                .withProperty("digital-bank.service.runtime-profile", "sit")
                .withProperty("logging.structured.ecs.service.name", "customer-service")
                .withProperty("logging.structured.ecs.service.environment", "sit")
                .withProperty("logging.structured.json.rename.service", "ecs_service")
                .withProperty("logging.structured.json.rename.correlation_id", "ecs_correlation_id")
                .withProperty("logging.structured.json.rename.traceId", "ecs_trace_id")
                .withProperty("logging.structured.json.rename.spanId", "ecs_span_id")
                .withProperty("logging.structured.json.rename.flat_service", "service")
                .withProperty("logging.structured.json.rename.flat_correlation_id", "correlation_id")
                .withProperty("logging.structured.json.rename.flat_trace_id", "trace_id")
                .withProperty("logging.structured.json.rename.flat_span_id", "span_id")
                .withProperty(
                        "logging.structured.json.customizer",
                        FlatStructuredLoggingJsonMembersCustomizer.class.getName());
        var context = new LoggerContext();
        context.putObject(org.springframework.core.env.Environment.class.getName(), environment);
        var encoder = new StructuredLogEncoder();
        encoder.setContext(context);
        encoder.setFormat("ecs");
        encoder.start();

        var document = objectMapper.readTree(new String(
                encoder.encode(event(
                        Instant.parse("2026-09-06T08:10:11.123456Z"),
                        Map.of("correlation_id", "corr-123", "traceId", "ABCDEF1234567890"))),
                StandardCharsets.UTF_8));
        encoder.stop();

        assertThat(document.get("timestamp").asText()).isEqualTo("2026-09-06T08:10:11.123Z");
        assertThat(document.get("level").asText()).isEqualTo("INFO");
        assertThat(document.get("service").asText()).isEqualTo("customer-service");
        assertThat(document.get("environment").asText()).isEqualTo("sit");
        assertThat(document.get("correlation_id").asText()).isEqualTo("corr-123");
        assertThat(document.get("trace_id").asText()).isEqualTo("abcdef1234567890");
        assertThat(document.get("logger").asText()).isEqualTo("com.digitalbank.customerservice.CustomerService");
        assertThat(document.get("message").asText()).isEqualTo("safe event");
        assertThat(document.get("ecs_service").get("name").asText()).isEqualTo("customer-service");
        assertThat(document.get("ecs_service").get("environment").asText()).isEqualTo("sit");
        assertThat(document.get("log").get("level").asText()).isEqualTo("INFO");
        assertThat(document.get("log").get("logger").asText())
                .isEqualTo("com.digitalbank.customerservice.CustomerService");
        assertThat(document.get("ecs").get("version").asText()).isEqualTo("8.11");
    }

    private static LoggingEvent event(Instant instant, Map<String, String> mdc) {
        var context = new LoggerContext();
        Logger logger = context.getLogger("com.digitalbank.customerservice.CustomerService");
        var event = new LoggingEvent(
                FlatStructuredLoggingJsonMembersCustomizerTest.class.getName(),
                logger,
                Level.INFO,
                "safe event",
                null,
                new Object[0]);
        event.setMDCPropertyMap(mdc);
        event.setInstant(instant);
        return event;
    }
}
