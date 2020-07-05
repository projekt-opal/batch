package org.dice_research.opal.batch.construction.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.configuration.Filenames;
import org.dice_research.opal.batch.construction.AbstractConstructor;
import org.dice_research.opal.batch.construction.Constructor;
import org.dice_research.opal.batch.model.PropertyCounter;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Abstract counter.
 * 
 * Implement {@link #getCfgKey()} to check, if the component is used.
 * 
 * Implement {@link #getProperty()} to set the property to be counted.
 * 
 * Override {@link #createModelProcessor(Cfg)} to set a specifig configuration.
 *
 * @author Adrian Wilke
 */
public abstract class AbstractCounterConstructor extends AbstractConstructor {

	private static final Logger LOGGER = LogManager.getLogger();

	private StringCounter stringCounter;

	public abstract String getCfgKey();

	public abstract String getProperty();

	@Override
	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors) {
		return super.addModelProcessor(cfg, processors, getCfgKey());
	}

	@Override
	public ModelProcessor createModelProcessor(Cfg cfg) {
		stringCounter = new StringCounter();
		return new PropertyCounter(stringCounter, getProperty());
	}

	@Override
	public Constructor finish(Cfg cfg) {
		if (cfg.getBoolean(getCfgKey())) {
			File file = new File(cfg.get(CfgKeys.IO_OUTPUT_DIRECTORY), Filenames.THEMES);
			try (FileOutputStream fos = new FileOutputStream(file)) {
				for (Entry<String, Long> entry : stringCounter.getCounterSortedByValue().entrySet()) {
					fos.write(entry.getValue().toString().getBytes(StandardCharsets.UTF_8));
					fos.write(",".getBytes(StandardCharsets.UTF_8));
					fos.write(entry.getKey().getBytes(StandardCharsets.UTF_8));
					fos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
				}
			} catch (Exception e) {
				LOGGER.error("Error on writing counter " + getCfgKey(), e);
			}
		}
		return this;
	}

}