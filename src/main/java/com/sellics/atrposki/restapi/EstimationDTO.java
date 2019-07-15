package com.sellics.atrposki.restapi;
/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EstimationDTO {
    private int score;
    @JsonProperty("Keyword")
    private String keyword;

    public EstimationDTO(int score, String keyword) {
        this.score = score;
        this.keyword = keyword;
    }
}
