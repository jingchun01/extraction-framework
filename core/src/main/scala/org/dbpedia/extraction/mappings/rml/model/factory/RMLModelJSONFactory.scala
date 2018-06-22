package org.dbpedia.extraction.mappings.rml.model.factory

import java.io.StringReader

import com.fasterxml.jackson.databind.JsonNode
import org.apache.jena.rdf.model.ModelFactory
import org.dbpedia.extraction.mappings.rml.load.RMLInferencer
import org.dbpedia.extraction.mappings.rml.model.RMLModel
import org.dbpedia.extraction.util.Language

/**
  * Created by wmaroy on 21.07.17.
  */
class RMLModelJSONFactory(mappingNode: JsonNode) {

  private lazy val dump = mappingNode.get("dump").asText()
  private lazy val name = mappingNode.get("name").asText()
  private lazy val language = mappingNode.get("language").asText()
  private lazy val base = RMLModel.createBase(name, language)

  def create(inferenced: Boolean = false): RMLModel = {

    val model = if (inferenced) {

      val inferencedDump = RMLInferencer.loadDumpAsString(Language(language), dump, name)
      ModelFactory.createDefaultModel().read(new StringReader(inferencedDump), base, "TURTLE")
    } else {
      ModelFactory.createDefaultModel().read(new StringReader(dump), base, "TURTLE")
    }

    new RMLModel(model, name, base, language)
  }


}
