package com.stock.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

public class StockPriceConsumer {
    private static final String TOPIC = "stock-prices";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String GROUP_ID = "stock-consumer-group";
    public static void main(String[] args){
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(TOPIC));
        HashMap<String, String> latestPrices = new HashMap<>();
        while(true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
            for(ConsumerRecord<String, String> record: records){
                latestPrices.put(record.key(), record.value());
                System.out.println("📩 Latest Stock Price → " + record.key() + ": " + latestPrices.get(record.key()));
            }
        }

    }
    
}
