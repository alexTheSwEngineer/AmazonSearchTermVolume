package com.sellics.atrposki.core.amazon;
/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 *
 */

/**
 * An interface for accesing amazon's autocomplete api.
 * Example request
 * http://completion.amazon.com/search/complete?search-alias=aps&mkt=1&q=apple%20watch
 */
public interface AmazonAutocompleteApi {
    /**
     * @param searchTerm the search term
     * @param market     the market code
     * @param department the sub department to filter by
     * @return AutocompleteResults containing the most relevant searches matching the search term as prefix. Will not be null, may be empty.
     */
    AutocompleteResults autocomplete(String searchTerm, String market, String department);
}
