package com.sellics.atrposki.core.estimation;
/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Value
@Wither
@Getter
@Builder
public class EstimationAlgorithmSettings {
        private Function<String, Set<String>> matchFinder;
        private int maxResultSetSize;
        private List<String> suffixStopWords;

        public Set<String> findMatches(String keyword){
                return matchFinder.apply(keyword);
        }
}
