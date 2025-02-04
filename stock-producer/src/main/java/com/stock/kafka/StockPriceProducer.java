package com.stock.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.network.KafkaChannel;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class StockPriceProducer {
    private static final String KAFKA_BROKER = "localhost:9092";
    private static final String TOPIC = "stock-prices";
    private static final String API_KEY = System.getenv("FINNHUB_API_KEY");
    private static final String POLYGON_API_URL = "https://finnhub.io/api/v1/quote?symbol=";
    private static final List<String> STOCK_SYMBOLS = Arrays.asList("AAPL", "GOOG", "MSFT", "TSLA");;

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        System.out.println("kafka producer is running");
        while (true) {
            try {
                for (String stockSymbol : STOCK_SYMBOLS) {
                    String stockData = fetchStockPrice((stockSymbol));
                    ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, stockSymbol, stockData);
                    producer.send(record);
                }
                Thread.sleep(21000);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    private static String fetchStockPrice(String stockSymbol) {
        String apiUrl = POLYGON_API_URL + stockSymbol + "&token=" + API_KEY;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);
            try (CloseableHttpResponse response = httpClient.execute(request);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()))) {
                StringBuilder jsonResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResponse.append(line);
                }
                System.out.println("🛠️ Full API Response: " + jsonResponse.toString());

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(jsonResponse.toString());
                String stockPrice = jsonNode.path("c").asText();

                return stockPrice;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching stock price";
        }
    }
}
