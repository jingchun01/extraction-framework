package org.dbpedia.extraction.mappings.rml.translate.mapper

import org.dbpedia.extraction.mappings.ConstantMapping
import org.dbpedia.extraction.mappings.rml.model.RMLTranslationModel
import org.dbpedia.extraction.mappings.rml.model.resource.{RMLLiteral, RMLPredicateObjectMap, RMLTriplesMap, RMLUri}
import org.dbpedia.extraction.mappings.rml.translate.dbf.DbfFunction
import org.dbpedia.extraction.ontology.RdfNamespace

import scala.language.reflectiveCalls

/**
  * Creates RML Mapping from Constant Mappings and adds the triples to the given model
  */
class ConstantRMLMapper(rmlModel: RMLTranslationModel, mapping: ConstantMapping) {

  def mapToModel(): List[RMLPredicateObjectMap] = {
    addConstantMapping()
  }

  def addConstantMapping(): List[RMLPredicateObjectMap] = {
    val uniqueUri = rmlModel.wikiTitle.resourceIri
    addConstantMappingToTriplesMap(uniqueUri, rmlModel.triplesMap)
  }

  def addIndependentConstantMapping(uri: String): List[RMLPredicateObjectMap] = {
    val constantMappingUri = RMLUri(uri + "/ConstantMapping/" + TemplateRMLMapper.constantCount)
    TemplateRMLMapper.increaseConstantCount()

    val constantPom = rmlModel.rmlFactory.createRMLPredicateObjectMap(constantMappingUri)
    addConstantValuePredicateObjectMap(constantPom)
    List(constantPom)
  }

  def addConstantMappingToTriplesMap(uri: String, triplesMap: RMLTriplesMap): List[RMLPredicateObjectMap] = {
    val constantMappingUri = RMLUri(uri + "/ConstantMapping/" + TemplateRMLMapper.constantCount)
    TemplateRMLMapper.increaseConstantCount()

    val constantPom = triplesMap.addPredicateObjectMap(constantMappingUri)
    addConstantValuePredicateObjectMap(constantPom)
    List(constantPom)
  }

  private def addConstantValuePredicateObjectMap(constantPom: RMLPredicateObjectMap) = {
    constantPom.addPredicate(RMLUri(mapping.ontologyProperty.uri))

    if (mapping.datatype == null) {

      if (mapping.isObjectProperty) {
        constantPom.addObject(RMLUri(mapping.value))
      } else {
        constantPom.addObject(new RMLLiteral(mapping.value))
      }
    } else {

      addUnitToPredicateObjectMap(constantPom)
    }
  }


  private def addUnitToPredicateObjectMap(constantPom: RMLPredicateObjectMap) = {
    val functionTermMapUri = constantPom.uri.extend("/FunctionTermMap")
    val functionTermMap = constantPom.addFunctionTermMap(functionTermMapUri)
    val functionValueUri = functionTermMapUri.extend("/FunctionValue")
    val functionValue = functionTermMap.addFunctionValue(functionValueUri)
    functionValue.addLogicalSource(rmlModel.logicalSource)
    functionValue.addSubjectMap(rmlModel.functionSubjectMap)

    val executePomUri = functionValueUri.extend("/ExecutePOM")
    val executePom = functionValue.addPredicateObjectMap(executePomUri)
    executePom.addPredicate(RMLUri(RdfNamespace.FNO.namespace + "executes"))
    val ExecuteObjectMapUri = executePomUri.extend("/ObjectMap")
    executePom.addObjectMap(ExecuteObjectMapUri).addConstant(RMLUri(RdfNamespace.DBF.namespace + DbfFunction.unitFunction.name))

    addParameterFunction(DbfFunction.unitFunction.unitParameter, functionValue)
    addParameterFunction(DbfFunction.unitFunction.valueParameter, functionValue)

  }

  private def addParameterFunction(param: String, functionValue: RMLTriplesMap) = {
    val parameterPomUri = functionValue.uri.extend("/" + param + "ParameterPOM")
    val parameterPom = functionValue.addPredicateObjectMap(parameterPomUri)
    parameterPom.addPredicate(RMLUri(RdfNamespace.DBF.namespace + param + "Parameter"))
    val parameterObjectMapUri = parameterPomUri.extend("/ObjectMap")
    parameterPom.addObjectMap(parameterObjectMapUri).addRMLReference(new RMLLiteral(getParameterValue(param)))
  }

  private def getParameterValue(param: String): String = {
    param match {
      case "unitParameter" => mapping.datatype.name
      case "valueParameter" => mapping.value
    }
  }

}
