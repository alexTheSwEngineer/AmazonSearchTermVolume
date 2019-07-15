package com.sellics.atrposki.core.estimation;
/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import java.util.Iterator;

/**
 * Simple interface for an iterative approach to estimating how "hot" a search term is.
 * With each itteration the result is more precise.
 */
public interface IterativeEstimation extends Iterator<Integer> {
}
