package org.dice_research.opal.batch.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RdfFileWriter implements RdfWriter {

	private static final Logger LOGGER = LogManager.getLogger();

	public File directory;
	public Lang lang;
	public String title;
	public int maxModels;

	File file;
	private FileOutputStream fileOutputStream = null;
	private int modelCounter = 0;
	private int fileCounter = 1;

	@Override
	public RdfWriter write(Model model) {

		if (fileOutputStream == null) {
			file = new File(directory, title + "-" + fileCounter + "." + lang.getFileExtensions().get(0));
			if (file.exists()) {
				file.delete();
			}
			try {
				fileOutputStream = new FileOutputStream(file, true);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		RDFDataMgr.write(fileOutputStream, model, lang);
		modelCounter++;

		if (modelCounter == maxModels) {
			finish();
		}

		return this;
	}

	@Override
	public RdfWriter finish() {
		if (file != null) {
			LOGGER.info("Wrote: " + file.getAbsolutePath() + " (" + modelCounter + " datasets)");
			try {
				fileOutputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		file = null;
		fileOutputStream = null;
		modelCounter = 0;
		fileCounter++;

		return this;
	}

}