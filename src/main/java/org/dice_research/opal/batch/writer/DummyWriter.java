package org.dice_research.opal.batch.writer;

import org.apache.jena.rdf.model.Model;

public class DummyWriter implements RdfWriter {

	@Override
	public RdfWriter write(Model model) {
		return this;
	}

	@Override
	public RdfWriter finish() {
		return this;
	}

}
