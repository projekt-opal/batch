package org.dice_research.opal.batch.reader;

/**
 * Interface for reading RDF resources. Implementations return
 * {@link RdfReaderResult} objects containing data related to a DCAT Dataset.
 *
 * @author Adrian Wilke
 */
public interface RdfReader {

	/**
	 * Checks if another model is available.
	 */
	public boolean hasNext();

	/**
	 * Gets a model and dataset URI containing data related to a DCAT Dataset.
	 */
	public RdfReaderResult next();

}