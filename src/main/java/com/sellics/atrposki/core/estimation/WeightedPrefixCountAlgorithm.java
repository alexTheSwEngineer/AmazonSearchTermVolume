package com.sellics.atrposki.core.estimation;
/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import lombok.Getter;
import lombok.ToString;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.removeEnd;

/**
 * Implementation of IterativeEstimation interface.
 * It calculates the score based on how many of the subprefixes of a search term, will result in autocompletion containing the search term itself.
 * Each iteration is weighted by the number of different autocompletion results.
 * Cutting the itterations short of this algorithm will produce less precise scores, and usually bellow the actuall value.
 */
@ToString(exclude = {"settings"})
public class WeightedPrefixCountAlgorithm implements IterativeEstimation {
    public static final int PERCENT = 100;
    @Getter
    private EstimationAlgorithmSettings settings;
    private String subPrefix;
    private String keyword;
    private String trimedKeyword;
    private float score = 0;
    private int maxItterations = 0;

    public WeightedPrefixCountAlgorithm(EstimationAlgorithmSettings settings, String keyword) {
        this.keyword = keyword;
        this.settings = settings;
        this.trimedKeyword = removeAllStopSufixes(keyword.toLowerCase());
        maxItterations = calculateMaxItterations(trimedKeyword);
        subPrefix = trimedKeyword;
    }

    /**
     * @return true if more itterations of the algorithm are possible
     */
    @Override
    public boolean hasNext() {
        return subPrefix.length() > 0;
    }

    /**
     * @return the estimated score as of this itteration.
     */
    @Override
    public Integer next() {
        Set<String> mathes = settings.findMatches(subPrefix);
        if (mathes.contains(trimedKeyword)) {
            score += getMatchesWeight(mathes);
            subPrefix = subPrefix.substring(0, subPrefix.length() - 1);
            subPrefix = removeAllStopSufixes(subPrefix);
        } else {
            subPrefix = ""; //signal end of algorithm
        }

        return (int) ((score * PERCENT) / maxItterations);
    }

    /**
     * @return the ratio of distinct autocompletion results over the total posible number returned by the match finder.
     */
    public float getMatchesWeight(Set<String> mathes) {
        return ((float) mathes.size()) / settings.getMaxResultSetSize();
    }

    /**
     * @return the itterations needed for maximum precision
     */
    public int calculateMaxItterations(String input) {
        int itterations = 0;
        input = removeAllStopSufixes(input);
        while (input.length() > 0) {
            itterations++;
            input = input.substring(0, input.length() - 1);
            input = removeAllStopSufixes(input);
        }
        return itterations;
    }

    /**
     * @return a string, trimed on the right side of any suffixes that are defined in  {@link WeightedPrefixCountAlgorithm#getSettings()}  {@link EstimationAlgorithmSettings#getSuffixStopWords()}
     */
    public String removeAllStopSufixes(String input) {
        String res = input;
        boolean hasChanged;
        do {
            hasChanged = false;
            for (String stopSufix : settings.getSuffixStopWords()) {
                if (res.endsWith(stopSufix)) {
                    res = removeEnd(res, stopSufix);
                    hasChanged = true;
                }
            }
        } while (hasChanged);   //takes care of consecutive stopwords
        return res;
    }

}
