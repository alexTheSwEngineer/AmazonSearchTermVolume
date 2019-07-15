package com.sellics.atrposki.core.estimation;
/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

public class WeightedPrefixCountAlgorithmTest {
    @Test
    public void getMaximumItterationCount_takesConsecutiveStopWOrdsIntoAccount() {
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "1abc121abc3a3abc1";
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);
        int result = weightedPrefixCountAlgorithm.calculateMaxItterations(keyword);
        Assert.assertEquals(10, result);
    }

    @Test
    public void getMaximumItterationCount_returnsZeroForEmptyString() {
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "";
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);
        int result = weightedPrefixCountAlgorithm.calculateMaxItterations(keyword);
        Assert.assertEquals(0, result);
    }

    @Test
    public void getMaximumItterationCount_returnsStrLengthForSufixless() {
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "abcde";
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);
        int result = weightedPrefixCountAlgorithm.calculateMaxItterations(keyword);
        Assert.assertEquals(keyword.length(), result);
    }

    @Test
    public void getMaximumItterationCount_returnsStrLengthForEmptySufixArray() {
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(new ArrayList<>())
                .build();
        String keyword = "abcde";
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);
        int result = weightedPrefixCountAlgorithm.calculateMaxItterations(keyword);
        Assert.assertEquals(keyword.length(), result);
    }

    @Test
    public void getMaximumItterationCount_ReturnsZeroForStopWOrdsOnlyKeyword() {
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "11231";
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);
        int result = weightedPrefixCountAlgorithm.calculateMaxItterations(keyword);
        Assert.assertEquals(0, result);
    }

    @Test
    public void removeAllSufixes_ReturnsCorrectWordForDuplicateStopSuffixesInMixedOrder() {
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "1abc121";
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);

        String result = weightedPrefixCountAlgorithm.removeAllStopSufixes(keyword);
        Assert.assertEquals("1abc", result);
    }

    @Test
    public void removeAllSufixes_DoesntChange_sufixlessWord() {
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "1abc";
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);

        String result = weightedPrefixCountAlgorithm.removeAllStopSufixes(keyword);
        Assert.assertEquals("1abc", result);
    }

    @Test
    public void removeAllSufixes_DoesntChangeWord_WhenSufixArrayIsEmpty() {
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "1abc";
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);

        String result = weightedPrefixCountAlgorithm.removeAllStopSufixes(keyword);
        Assert.assertEquals("1abc", result);
    }


    @Test
    public void whenKeywordMatchesInAllIterations_andResultsAreFull_scoreIs100() {
        String keyword = "1abc1asd23";
        String sufixlessKeyword = "1abc1asd";
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .maxResultSetSize(3)
                .matchFinder((term) -> asList(sufixlessKeyword, term + "aaaaa", term + "bbbbb").stream().collect(toSet()))
                .build();
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);


        int result = 0;
        while (weightedPrefixCountAlgorithm.hasNext()) {
            result = weightedPrefixCountAlgorithm.next();
        }
        Assert.assertEquals(100, result);
    }

    @Test
    public void whenKeywordMatchesInHalfOfAllIterations_andResultsAreFull_scoreIs50() {
        String keyword = "a1b1";
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .maxResultSetSize(3)
                .matchFinder((term) -> asList(term, term + "aaaaa", term + "bbbbb").stream().collect(toSet()))
                .build();
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);


        int result = 0;
        while (weightedPrefixCountAlgorithm.hasNext()) {
            result = weightedPrefixCountAlgorithm.next();
        }
        Assert.assertEquals(50, result);
    }

    @Test
    public void whenKeywordMatchesInAllIterations_andResultsAreHalfEmptyFull_scoreIs50() {
        String keyword = "a1b1";
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .maxResultSetSize(6)
                .matchFinder((term) -> asList("a1b", term + "aaaaa", term + "bbbbb").stream().collect(toSet()))
                .build();
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);


        int result = 0;
        while (weightedPrefixCountAlgorithm.hasNext()) {
            result = weightedPrefixCountAlgorithm.next();
        }
        Assert.assertEquals(50, result);
    }


    @Test
    public void whenAmazonReturnsNoMatches_scoreIs0() {
        String keyword = "a1b1";
        EstimationAlgorithmSettings settings = EstimationAlgorithmSettings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .maxResultSetSize(6)
                .matchFinder((term) -> new HashSet<>())
                .build();
        WeightedPrefixCountAlgorithm weightedPrefixCountAlgorithm = new WeightedPrefixCountAlgorithm(settings, keyword);


        int result = 0;
        while (weightedPrefixCountAlgorithm.hasNext()) {
            result = weightedPrefixCountAlgorithm.next();
        }
        Assert.assertEquals(0, result);
    }
}
