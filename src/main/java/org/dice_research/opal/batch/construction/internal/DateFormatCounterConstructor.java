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

	private StringCounter stringFormatCounter;
	private StringCounter datatypeCounter;

	@Override
	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors) {
		return super.addModelProcessor(cfg, processors, CfgKeys.RUN_DATE_COUNTER);
	}

	@Override
	public ModelProcessor createModelProcessor(Cfg cfg) {
		stringFormatCounter = new StringCounter();
		datatypeCounter = new StringCounter();
		return new DateFormatCounter(stringFormatCounter, datatypeCounter);
	}

	@Override
	public Constructor finish(Cfg cfg) {

		if (cfg.getBoolean(CfgKeys.RUN_DATE_COUNTER)) {

			StringBuilder stringBuilder = new StringBuilder();
			for (Entry<String, Long> entry : stringFormatCounter.getCounterSortedByValue().entrySet()) {
				stringBuilder.append(entry.getValue());
				stringBuilder.append(",");
				stringBuilder.append(entry.getKey());
				stringBuilder.append(System.lineSeparator());
			}

			File file = new File(cfg.get(CfgKeys.IO_OUTPUT_DIRECTORY), Filenames.DATEFORMATS);
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
			} catch (Exception e) {
				LOGGER.error("Error on writing date format counter", e);
			}

			stringBuilder = new StringBuilder();
			for (Entry<String, Long> entry : datatypeCounter.getCounterSortedByValue().entrySet()) {
				stringBuilder.append(entry.getValue());
				stringBuilder.append(",");
				stringBuilder.append(entry.getKey());
				stringBuilder.append(System.lineSeparator());
			}

			file = new File(cfg.get(CfgKeys.IO_OUTPUT_DIRECTORY), Filenames.DATETYPES);
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
			} catch (Exception e) {
				LOGGER.error("Error on writing date types counter", e);
			}
		}

		return this;
	}

}