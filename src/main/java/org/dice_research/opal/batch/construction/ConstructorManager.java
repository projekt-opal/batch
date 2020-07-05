package org.dice_research.opal.batch.construction;

import java.util.LinkedList;
import java.util.List;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.construction.internal.StatisticsConstructor;
import org.dice_research.opal.batch.construction.internal.ThemeCounterConstructor;
import org.dice_research.opal.batch.construction.opal.CatfishConstructor;
import org.dice_research.opal.batch.construction.opal.CivetConstructor;
import org.dice_research.opal.batch.construction.opal.GeoDataConstructor;
import org.dice_research.opal.batch.construction.opal.LanguageDetectionConstructor;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Manages constructors.
 *
 * @author Adrian Wilke
 */
public class ConstructorManager {

	public static ConstructorManager create() {
		ConstructorManager constructorManager = new ConstructorManager();
		constructorManager.addConstructor(new StatisticsConstructor());

		// Default OPAL components

		constructorManager.addConstructor(new CatfishConstructor());
		constructorManager.addConstructor(new LanguageDetectionConstructor());
		constructorManager.addConstructor(new GeoDataConstructor());
		constructorManager.addConstructor(new CivetConstructor());

		// Internal components

		constructorManager.addConstructor(new ThemeCounterConstructor());

		return constructorManager;
	}

	private List<Constructor> constructors = new LinkedList<>();
	private List<ModelProcessor> modelProcessors = new LinkedList<>();

	public ConstructorManager addConstructor(Constructor constructor) {
		constructors.add(constructor);
		return this;
	}

	public List<Constructor> getConstructors() {
		return constructors;
	}

	/**
	 * Creates a list of new model processor objects.
	 */
	public ConstructorManager createModelProcessors(Cfg cfg) {
		for (Constructor constructor : constructors) {
			constructor.addModelProcessor(cfg, modelProcessors);
		}
		return this;
	}

	public ConstructorManager addModelProcessor(ModelProcessor modelProcessor) {
		modelProcessors.add(modelProcessor);
		return this;
	}

	public List<ModelProcessor> getModelProcessors() {
		return modelProcessors;
	}

}