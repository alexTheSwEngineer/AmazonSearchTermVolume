package com.sellics.atrposki.core.amazon;
/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutocompleteResults {
    List<JsonNode> response;
    Set<String> results;

    /**
     * @param response, must be a json of the following srtucture: <a href="file:///../../../../../test/java/resources/sampleApiResponse.json">sampleApiResponse.json</a>
     *                  The response must be a list, which contains a string on position zero, and a list of strings on position 1
     */
    public AutocompleteResults(List<JsonNode> response) {
        this.response = response;
    }

    public String getSearchTerm() {
        return response.get(0).asText();
    }

    public Set<String> getMatches() {
        if (results != null) {
            return results;
        }

        results = new HashSet<>();
        response.get(1)
                .iterator()
                .forEachRemaining(match -> results.add(match.asText()));
        return results;
    }
}
