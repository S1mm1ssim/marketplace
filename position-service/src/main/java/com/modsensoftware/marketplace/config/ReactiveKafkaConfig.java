package com.modsensoftware.marketplace.config;

import com.modsensoftware.marketplace.dto.PlacedUserTransaction;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;

/**
 * @author andrey.demyanchik on 1/5/2023
 */
@Configuration
public class ReactiveKafkaConfig {

    @Bean
    public ReactiveKafkaProducerTemplate<String, PlacedUserTransaction> reactiveKafkaProducerTemplate(
            KafkaProperties properties) {
        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(properties.buildProducerProperties()));
    }

    @Bean
    public ReceiverOptions<String, PlacedUserTransaction> kafkaReceiverOptions(KafkaProperties properties) {
        ReceiverOptions<String, PlacedUserTransaction> basicReceiverOptions = ReceiverOptions
                .create(properties.buildConsumerProperties());
        return basicReceiverOptions.subscription(Collections.singletonList("userTransactionProcessing"));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, PlacedUserTransaction> reactiveKafkaConsumerTemplate(
            ReceiverOptions<String, PlacedUserTransaction> kafkaReceiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
    }
}
