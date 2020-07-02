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

	private File file = null;
	private Lang lang = null;
	private FileOutputStream fileOutputStream = null;

	public RdfFileWriter setFile(File file) {
		this.file = file;
		return this;
	}

	public RdfFileWriter setLang(Lang lang) {
		this.lang = lang;
		return this;
	}

	public File getFile() {
		return file;
	}

	public Lang getLang() {
		return lang;
	}

	@Override
	public RdfWriter write(Model model) {
		if (fileOutputStream == null) {
			initialize();
		}

		RDFDataMgr.write(fileOutputStream, model, lang);

		return this;
	}

	private void initialize() {
		if (file == null) {
			throw new RuntimeException("No file specified");
			
		} else if (!file.exists()) {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Could not create: " + file.getAbsolutePath());
			}
			
		} else if (!file.canWrite()) {
			throw new RuntimeException("Can not write: " + file.getAbsolutePath());
		}

		if (lang == null) {
			throw new RuntimeException("No language specified");
		}

		try {
			fileOutputStream = new FileOutputStream(file, true);
		} catch (FileNotFoundException e) {
			// Is already handeled above
			throw new RuntimeException(e);
		}
	}

	@Override
	public RdfWriter finish() {
		LOGGER.info("Wrote: " + file.getAbsolutePath() + " " + file.length() / 1000000 + " MB");
		try {
			fileOutputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return this;
	}

}