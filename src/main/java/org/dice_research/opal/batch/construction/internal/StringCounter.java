package org.dice_research.opal.batch.construction.internal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Counts strings.
 *
 * @author Adrian Wilke
 */
public class StringCounter {

	private Map<String, Long> counter = new HashMap<>();

	public void increment(String string) {
		if (!counter.containsKey(string)) {
			counter.put(string, 0l);
		}
		counter.put(string, counter.get(string) + 1);
	}

	public Map<String, Long> getCounter() {
		return counter;
	}

	public Map<String, Long> getCounterSortedByValue() {
		// https://dzone.com/articles/how-to-sort-a-map-by-value-in-java-8
		return counter.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
}