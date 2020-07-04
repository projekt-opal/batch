package org.dice_research.opal.batch.processor.internal;

import java.io.File;
import java.util.Map.Entry;

import org.apache.jena.vocabulary.DCAT;
import org.dice_research.opal.batch.Batch;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.model.PropertyCounter;
import org.dice_research.opal.batch.processor.Processors;

public class ThemeCounter {

	/**
	 * Main entry point.
	 */
	public static void main(String[] args) throws Exception {

		// Set default or custom configuration
		if (args.length == 0) {
			new ThemeCounter().execute(new Cfg());
		} else {
			new ThemeCounter().execute(new Cfg(new File(args[0])));
		}
	}

	private void execute(Cfg cfg) throws Exception {

		StringCounter counter = new StringCounter();
		PropertyCounter languageCounter = new PropertyCounter(counter, DCAT.theme);
		new Batch().execute(cfg, new Processors().addModelProcessor(languageCounter));

		// TODO: sort by value, maybe export csv
		System.out.println(counter.getCounter().size());
		for (Entry<String, Long> entry : counter.getCounter().entrySet()) {
			System.out.println(entry);
		}

	}

}