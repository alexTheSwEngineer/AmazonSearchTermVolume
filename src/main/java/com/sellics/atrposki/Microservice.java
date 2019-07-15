package com.sellics.atrposki;

/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellics.atrposki.core.amazon.AmazonAutocompleteApi;
import com.sellics.atrposki.core.amazon.RestTemplateAutocompleteApi;
import com.sellics.atrposki.core.estimation.EstimationAlgorithmSettings;
import com.sellics.atrposki.core.estimation.IterativeEstimation;
import com.sellics.atrposki.core.estimation.WeightedPrefixCountAlgorithm;
import com.sellics.atrposki.restapi.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.function.BiFunction;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
public class Microservice {
    public static void main(String[] args) {
        SpringApplication.run(Microservice.class, args);
    }

    @Bean
    public Time timestampProvider() {
        return System::nanoTime;
    }

    @Bean
    @Autowired
    AmazonAutocompleteApi amazonAutocompleteApi(RestOperations restOperations, @Value("${com.sellics.atrposki.core.amazon.url}") String amazonUrl, ObjectMapper objectMapper) {
        return new RestTemplateAutocompleteApi(restOperations, amazonUrl, objectMapper);
    }

    @Bean
    RestOperations restOperations() {
        return new RestTemplate();
    }

    @Bean
    BiFunction<EstimationAlgorithmSettings, String, IterativeEstimation> algorithmFactory() {
        return WeightedPrefixCountAlgorithm::new;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
