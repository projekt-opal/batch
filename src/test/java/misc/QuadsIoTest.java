package misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests reading N-Quads.
 * 
 * Maybe an alternative:
 * https://github.com/SmartDataAnalytics/SparqlIntegrate/blob/21417cfe7b4f6ce1fe9cd79825f774f231f829d4/README-NGS.md
 *
 * @author Adrian Wilke
 */
public class QuadsIoTest {
	
	private static final boolean SKIP_TEST = true;

	/**
	 * {@link RDFDataMgr} provides load and read methods for Graph, Model, Dataset,
	 * DatasetGraph. This test writes and reads a N-Quads file.
	 */
	@Test
	public void testReadNquads() throws IOException {

		// Does not test functionality
		Assume.assumeFalse("Development test (Jena Framework)", SKIP_TEST);

		// Create model
		Model model = ModelFactory.createDefaultModel();
		Resource s = ResourceFactory.createResource("http://example.org/s");
		Property p = ResourceFactory.createProperty("http://example.org/p");
		Resource o = ResourceFactory.createResource("http://example.org/o");
		model.add(s, p, o);

		// Create a dataset with a named graph.
		Dataset dataset = DatasetFactory.create();
		String graphName = "http://graphname";
		dataset.addNamedModel(graphName, model);

		// Tmp file
		File file = File.createTempFile(QuadsIoTest.class.getName(), ".txt");
		file.deleteOnExit();
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		FileInputStream fileInputStream = new FileInputStream(file);

		// Write graph
		RDFDataMgr.write(fileOutputStream, dataset, Lang.NQUADS);

		// Read named graph into model
		Model newModel;

		// Both work. Both produce a warning "Illegal reflective access".
		if (Boolean.FALSE) {
			String uri = file.toURI().toString();
			Dataset newDataset = RDFDataMgr.loadDataset(uri, Lang.NQUADS);
			newModel = newDataset.getNamedModel(graphName);
		} else {
			Dataset newDataset = DatasetFactory.create();
			RDFDataMgr.read(newDataset, fileInputStream, Lang.NQUADS);
			newModel = newDataset.getNamedModel(graphName);
		}

		Assert.assertEquals(1, newModel.size());
	}
}