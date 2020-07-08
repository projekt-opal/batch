package org.dice_research.opal.batch.construction.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.configuration.Filenames;
import org.dice_research.opal.batch.construction.AbstractConstructor;
import org.dice_research.opal.batch.construction.Constructor;
import org.dice_research.opal.batch.model.Statistics;
import org.dice_research.opal.common.interfaces.ModelProcessor;

public class StatisticsConstructor extends AbstractConstructor {

	private static final Logger LOGGER = LogManager.getLogger();

	private long startTime;
	private long endTime;
	private Statistics statistics;

	@Override
	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors) {
		processors.add(createModelProcessor(cfg));
		return true;
	}

	@Override
	public ModelProcessor createModelProcessor(Cfg cfg) {
		startTime = System.currentTimeMillis();
		statistics = new Statistics();
		return statistics;
	}

	@Override
	public Constructor finish(Cfg cfg) {
		endTime = System.currentTimeMillis();

		StringBuilder stringBuilder = new StringBuilder();
		if (cfg.has(CfgKeys.INFO)) {
			stringBuilder.append("Info:                ");
			stringBuilder.append(cfg.get(CfgKeys.INFO));
			stringBuilder.append(System.lineSeparator());
		}
		stringBuilder.append("Start:               " + new Date(startTime).toString());
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("Runtime (seconds):   " + 1f * (endTime - startTime) / 1000);
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("Processor:           https://github.com/projekt-opal/batch");
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("Processed datasets:  " + statistics.models);
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("Datasets per second: " + 1f * statistics.models / ((endTime - startTime) / 1000));
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("Processed triples:   " + statistics.triples);
		stringBuilder.append(System.lineSeparator());

		File file = new File(cfg.get(CfgKeys.IO_OUTPUT_DIRECTORY), Filenames.STATISTICS);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			LOGGER.error("Error on writing statistics", e);
		}

		return super.finish(cfg);
	}

}