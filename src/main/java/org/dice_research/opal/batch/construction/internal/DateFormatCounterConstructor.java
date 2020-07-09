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
import org.dice_research.opal.batch.model.DateFormatCounter;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Counts date formats and types.
 *
 * @author Adrian Wilke
 */
public class DateFormatCounterConstructor extends AbstractConstructor {

	private static final Logger LOGGER = LogManager.getLogger();

	private StringCounter datatypeCounter;
	private StringCounter dateFormatCounter;
	private StringCounter stringFormatCounter;

	@Override
	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors) {
		return super.addModelProcessor(cfg, processors, CfgKeys.RUN_DATE_COUNTER);
	}

	@Override
	public ModelProcessor createModelProcessor(Cfg cfg) {
		datatypeCounter = new StringCounter();
		dateFormatCounter = new StringCounter();
		stringFormatCounter = new StringCounter();
		return new DateFormatCounter(datatypeCounter, dateFormatCounter, stringFormatCounter);
	}

	@Override
	public Constructor finish(Cfg cfg) {

		if (cfg.getBoolean(CfgKeys.RUN_DATE_COUNTER)) {
			write(datatypeCounter, Filenames.getFile(cfg, Filenames.DATETYPES));
			write(dateFormatCounter, Filenames.getFile(cfg, Filenames.DATEFORMATS));
			write(stringFormatCounter, Filenames.getFile(cfg, Filenames.DATESTRINGFORMATS));
		}

		return this;
	}

	private void write(StringCounter stringCounter, File file) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Entry<String, Long> entry : stringCounter.getCounterSortedByValue().entrySet()) {
			stringBuilder.append(entry.getValue());
			stringBuilder.append(",");
			stringBuilder.append(entry.getKey());
			stringBuilder.append(System.lineSeparator());
		}

		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			LOGGER.error("Error on writing date types counter", e);
		}
	}

}