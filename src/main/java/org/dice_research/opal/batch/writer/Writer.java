package org.dice_research.opal.batch.writer;

import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Interface for writing RDF resources.
 *
 * @author Adrian Wilke
 */
public interface Writer extends ModelProcessor {

	/**
	 * For finalizing readers, e.g. closing.
	 */
	Writer finish();

}