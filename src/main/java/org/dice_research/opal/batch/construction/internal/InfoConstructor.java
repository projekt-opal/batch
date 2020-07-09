package org.dice_research.opal.batch.construction.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.jar.Manifest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.configuration.Filenames;
import org.dice_research.opal.batch.construction.AbstractConstructor;
import org.dice_research.opal.batch.construction.Constructor;
import org.dice_research.opal.batch.model.ModelTripleCounter;
import org.dice_research.opal.common.interfaces.ModelProcessor;

public class InfoConstructor extends AbstractConstructor {

	private static final Logger LOGGER = LogManager.getLogger();

	private long startTime;
	private long endTime;
	private ModelTripleCounter modelTripleCounter;

	@Override
	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors) {
		processors.add(createModelProcessor(cfg));
		return true;
	}

	@Override
	public ModelProcessor createModelProcessor(Cfg cfg) {
		startTime = System.currentTimeMillis();
		modelTripleCounter = new ModelTripleCounter();
		return modelTripleCounter;
	}

	@Override
	public Constructor finish(Cfg cfg) {
		endTime = System.currentTimeMillis();

		StringBuilder stringBuilder = new StringBuilder();
		if (cfg.has(CfgKeys.INFO)) {
			stringBuilder.append("Info:                ");
			stringBuilder.append(cfg.get(CfgKeys.INFO));
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append(System.lineSeparator());
		}
		stringBuilder.append("Start:               " + new Date(startTime).toString());
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("Runtime (seconds):   " + 1f * (endTime - startTime) / 1000);
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("Processed datasets:  " + modelTripleCounter.models);
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("Datasets per second: " + 1f * modelTripleCounter.models / ((endTime - startTime) / 1000));
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("Processed triples:   " + modelTripleCounter.triples);
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("Processor:           https://github.com/projekt-opal/batch");
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append("Version:             " + getVersionName());
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append(System.lineSeparator());

		stringBuilder.append("Configuration:");
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append(cfg.toString());

		try (FileOutputStream fos = new FileOutputStream(Filenames.getFile(cfg, Filenames.INFO))) {
			fos.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			LOGGER.error("Error on writing info file", e);
		}

		return super.finish(cfg);
	}

	private String getVersionNameFromManifest() {
		// TODO: Not tested
		// https://www.triology.de/blog/versionsnamen-mit-maven-auslesen-des-versionsnamens
		InputStream manifestStream = getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
		if (manifestStream != null) {
			try {
				return new Manifest(manifestStream).getMainAttributes().getValue("versionName");
			} catch (IOException e) {
				return "unknown";
			}
		} else {
			return "unknown";
		}
	}

	private String getVersionName() {
		// https://stackoverflow.com/a/41791885
		if ((new File("pom.xml")).exists()) {
			try {
				return new MavenXpp3Reader().read(new FileReader("pom.xml")).getVersion();
			} catch (IOException | XmlPullParserException e) {
				return "unknown";
			}
		} else {
			return getVersionNameFromManifest();
		}
	}

}