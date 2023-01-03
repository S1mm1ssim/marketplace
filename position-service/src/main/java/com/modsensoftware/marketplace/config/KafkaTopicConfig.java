package com.modsensoftware.marketplace.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * @author andrey.demyanchik on 12/29/2022
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${topics.processedTransactions.name}")
    private String userTransactionStatusResultsTopicName;
    @Value("${topics.transactionsPlacedForProcessing.name}")
    private String userTransactionProcessingTopicName;

    private static final int PARTITIONS_COUNT = 5;
    private static final int REPLICAS_COUNT = 1;

    @Bean
    public NewTopic userTransactionStatusResults() {
        return TopicBuilder.name(userTransactionStatusResultsTopicName)
                .partitions(PARTITIONS_COUNT)
                .replicas(REPLICAS_COUNT)
                .build();
    }

    @Bean
    public NewTopic userTransactionProcessing() {
        return TopicBuilder.name(userTransactionProcessingTopicName)
                .partitions(PARTITIONS_COUNT)
                .replicas(REPLICAS_COUNT)
                .build();
    }
}
