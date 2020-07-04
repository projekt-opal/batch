package org.dice_research.opal.batch.processor.internal;

import java.util.HashMap;
import java.util.Map;

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
}
