package org.dice_research.opal.batch.writer;

import org.apache.jena.rdf.model.Model;

/**
 * Interface for writing RDF resources.
 *
 * @author Adrian Wilke
 */
public interface RdfWriter {

	/**
	 * Writes the given model.
	 */
	RdfWriter write(Model model);

	/**
	 * For finalizing readers, e.g. closing.
	 */
	RdfWriter finish();

}