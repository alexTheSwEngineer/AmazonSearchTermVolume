package com.sellics.atrposki.core.amazon;
/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

public class RestTemplateAutocompleteApi implements AmazonAutocompleteApi {
    private static final String DEPARTMENT_FILTER = "search-alias";
    private static final String MKT_FILTER = "mkt";
    private static final String SEARCH_TERM = "q";
    private RestOperations restOperations;
    private String amazonUrl;
    private ObjectMapper mapper;


    public RestTemplateAutocompleteApi(RestOperations restOperations, String amazonUrl, ObjectMapper mapper) {
        this.restOperations = restOperations;
        this.amazonUrl = amazonUrl;
        this.mapper = mapper;
    }

    @Override
    public AutocompleteResults autocomplete(String searchTerm, String market, String department) {
        String url = UriComponentsBuilder.fromHttpUrl(amazonUrl)
                .queryParam(DEPARTMENT_FILTER, department)
                .queryParam(MKT_FILTER, market)
                .toUriString();
        String result = restOperations.getForObject(url + "&q=" + searchTerm, String.class);
        return getAutocompleteResultsFromJson(result);
    }

    private AutocompleteResults getAutocompleteResultsFromJson(String result) {
        try {
            JsonNode resultAsJson = mapper.readTree(result);
            return new AutocompleteResults(stream(resultAsJson.spliterator(), false).collect(Collectors.toList()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
