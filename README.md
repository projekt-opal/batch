# OPAL batch

This component reads RDF files and applies the following OPAL components:

- [Catfish](https://github.com/projekt-opal/catfish) (data-cleaner-service)
- [Civet](https://github.com/projekt-opal/civet) (quality-metrics-service)
- [Metadata-Refinement](https://github.com/projekt-opal/metadata-refinement) (opal-confirm-conversion-service)


## How to use

* Download the latest [release](https://github.com/projekt-opal/batch/releases).
* Create a copy of the [default.properties](default.properties) file and edit the copy.  
At least, set `io.input` and `io.outputDirectory`.
* Finally, run it by `java -jar opal-batch.jar default.properties`.


## Get data to process

Data should be in a [RDF serialization format](https://en.wikipedia.org/wiki/Resource_Description_Framework#Serialization_formats)
and using the [Data Catalog Vocabulary (DCAT)](https://www.w3.org/TR/vocab-dcat-2/) vocabulary.

You can find ready-to go input data at the Hobbitdata Server.
Data from open data portals is available in the directories [OPAL/processed_datasets](https://hobbitdata.informatik.uni-leipzig.de/OPAL/processed_datasets/) and [OPAL/SourceGraphs](https://hobbitdata.informatik.uni-leipzig.de/OPAL/SourceGraphs/).


## Credits

[Data Science Group (DICE)](https://dice-research.org/) at [Paderborn University](https://www.uni-paderborn.de/)

This work has been supported by the German Federal Ministry of Transport and Digital Infrastructure (BMVI) in the project [Open Data Portal Germany (OPAL)](http://projekt-opal.de/) (funding code 19F2028A).

