
package lulo.spec


import com.kispoko.culebra.*
import effect.Err
import effect.Val
import lulo.document.*



fun customTypeParser(type : ObjectType,
                     yamlValue : YamlValue,
                     case : String?,
                     path : DocPath,
                     spec : Spec,
                     specDependencies: List<Spec>) : DocParse = when (type)
{
    is Product -> productTypeParser(type, yamlValue, case, path, spec, specDependencies)
    is Sum     -> sumTypeParser(yamlValue, path, spec, specDependencies)
    is Simple  -> simpleTypeParser(type, yamlValue, case, path, spec, specDependencies)
    else       -> docError(listOf(UnknownKind(path)))
}


private fun productTypeParser(productType : Product,
                              yamlValue : YamlValue,
                              case : String?,
                              path : DocPath,
                              spec : Spec,
                              specDependencies : List<Spec>) : DocParse = when (yamlValue)
{
    is YamlDict ->
    {
        val docMap = hashMapOf<String, SpecDoc>()
        val errors = mutableListOf<DocParseError>()

        for (field in productType.fields)
        {
            val fieldYamlValue = yamlValue.at(field.name.name)
            when (fieldYamlValue)
            {
                is Result ->
                {
                    val fieldPath = path withLocation DocKeyNode(field.name.name)
                    val fieldParse = fieldParser(field, fieldYamlValue.value, fieldPath, spec, specDependencies)

                    when (fieldParse)
                    {
                        is Val -> docMap.put(field.name.name, fieldParse.value)
                        is Err -> errors.addAll(fieldParse.error)
                    }
                }
                is Error ->
                {
                    val fieldYamlError = fieldYamlValue.error
                    when (fieldYamlError) {
                        is KeyDoesNotExist ->
                        {
                            if (field.presence == FieldPresence.REQUIRED)
                                errors.add(MissingField(field.name.name, path))
                        }
                        else               -> errors.add(YamlError(fieldYamlError, path))
                    }
                }
            }
        }

        if (errors.size > 0)
            docError<SpecDoc>(errors)
        else
            docResult<SpecDoc>(DocDict(docMap, case, path))
    }
    else        -> docError(listOf(ExpectedProduct(path)))
}


private fun sumTypeParser(yamlValue : YamlValue,
                          path : DocPath,
                          spec : Spec,
                          specDependencies : List<Spec>) : DocParse = when (yamlValue)
{
    is YamlDict ->
    {
        val typeYamlValueParser = yamlValue.at("type")
        when (typeYamlValueParser)
        {
            is Result ->
            {
                val typeYamlValue = typeYamlValueParser.value
                when (typeYamlValue)
                {
                    is YamlText ->
                    {
                        val caseTypeName = TypeName(typeYamlValue.text)
                        val caseType = specType(caseTypeName, listOf(spec))

                        if (caseType == null)
                        {
                            docError(listOf(TypeDoesNotExist(caseTypeName, path)))
                        }
                        else {
                            val caseYamlValueParser = yamlValue.at(caseTypeName.name)
                            when (caseYamlValueParser)
                            {
                                is Result -> customTypeParser(caseType.type, caseYamlValueParser.value, caseTypeName.name,
                                                           path, spec, specDependencies)
                                is Error ->
                                {
                                    val fieldYamlError = caseYamlValueParser.error
                                    when (fieldYamlError) {
                                        is KeyDoesNotExist ->
                                            docError(listOf(MissingField(caseTypeName.name, path)))
                                        else               ->
                                            docError(listOf(YamlError(fieldYamlError, path)))
                                    }
                                }
                            }
                        }
                    }
                    else        -> docError(listOf(UnexpectedType(YamlType.TEXT,
                                                                  yamlType(typeYamlValue),
                                                                  path)))
                }
            }
            is Error ->
            {
                val fieldYamlError = typeYamlValueParser.error
                when (fieldYamlError) {
                    is KeyDoesNotExist -> docError(listOf(MissingField("type", path)))
                    else               -> docError(listOf(YamlError(fieldYamlError, path)))
                }
            }
        }

    }
    else        -> docError(listOf(ExpectedSum(path)))
}


private fun simpleTypeParser(simpleType : Simple,
                             yamlValue : YamlValue,
                             case : String?,
                             path : DocPath,
                             spec : Spec,
                             specDependencies : List<Spec>) : DocParse
{
    val typeKind = valueKind(simpleType.baseTypeName)

    if (typeKind == ValueKind.PRIMITIVE)
    {
        return primValueParser(primValueType(simpleType.baseTypeName)!!, yamlValue, path, case)
    }
    else if (typeKind == ValueKind.CUSTOM)
    {
        val baseTypeName = TypeName(simpleType.baseTypeName)
        val baseType = specType(baseTypeName, listOf(spec))

        if (baseType == null)
            return docError(listOf(TypeDoesNotExist(baseTypeName, path)))
        else
            return customTypeParser(baseType.type, yamlValue, null, path, spec, specDependencies)
    }
    else
    {
        return docError(listOf(UnknownKind(path)))
    }
}


// ---------------------------------------------------------------------------------------------
// FIELD PARSER
// ---------------------------------------------------------------------------------------------

private fun fieldParser(field : Field,
                        yamlValue: YamlValue,
                        path : DocPath,
                        spec : Spec,
                        specDependencies: List<Spec>) : DocParse = when (field.valueType)
{
    is Prim       -> primValueParser(field.valueType.type, yamlValue, path, null)
    is PrimList   -> primListValueParser(field.valueType.type, yamlValue, path)
    is Custom     -> customValueParser(field.valueType.name, yamlValue, path,
                                       spec, specDependencies)
    is CustomList -> customListValueParser(field.valueType.name, yamlValue, path,
                                           spec, specDependencies)
}


// ---------------------------------------------------------------------------------------------
// VALUE PARSERS
// ---------------------------------------------------------------------------------------------

// Primitve Value Parser
// ---------------------------------------------------------------------------------------------

private fun primValueParser(primValueType: PrimValueType,
                            yamlValue : YamlValue,
                            path : DocPath,
                            case : String?) : DocParse =
    when (primValueType)
    {
        is PrimValueType.NUMBER  -> primNumberParser(yamlValue, path, case)
        is PrimValueType.STRING  -> primStringParser(yamlValue, path, case)
        is PrimValueType.BOOLEAN -> primBooleanParser(yamlValue, path, case)
        else                     -> docError(listOf(UnknownPrimType(primValueType, path)))
    }


// Primitve Value List Parser
// ---------------------------------------------------------------------------------------------

private fun primListValueParser(primValueType : PrimValueType,
                                yamlValue : YamlValue,
                                path : DocPath) : DocParse = when (yamlValue)
{
    is YamlArray ->
    {
        val errors = mutableListOf<DocParseError>()
        val docs = mutableListOf<SpecDoc>()

        for (yamlValueElement in yamlValue.list)
        {
            val elementDocParse = primValueParser(primValueType, yamlValue, path, null)

            when (elementDocParse) {
                is Val -> docs.add(elementDocParse.value)
                is Err -> errors.addAll(elementDocParse.error)
            }
        }

        if (errors.size > 0)
            docError<SpecDoc>(errors)
        else
            docResult<SpecDoc>(DocList(docs, null, path))
    }
    else         -> docError(listOf(UnexpectedType(YamlType.ARRAY, yamlType(yamlValue), path)))
}


// Custom Value Parser
// ---------------------------------------------------------------------------------------------

private fun customValueParser(customTypeName : TypeName,
                              yamlValue : YamlValue,
                              path : DocPath,
                              spec : Spec,
                              specDependencies: List<Spec>) : DocParse
{
    val customType = specType(customTypeName, listOf(spec))

    if (customType == null)
        return docError(listOf(TypeDoesNotExist(customTypeName, path)))
    else
        return customTypeParser(customType.type, yamlValue, null, path, spec, specDependencies)
}


// Custom Value List Parser
// ---------------------------------------------------------------------------------------------

private fun customListValueParser(customTypeName : TypeName,
                                  yamlValue : YamlValue,
                                  path : DocPath,
                                  spec : Spec,
                                  specDependencies : List<Spec>) : DocParse = when (yamlValue)
{
    is YamlArray ->
    {
        val customType = specType(customTypeName, listOf(spec))

        if (customType == null)
        {
            docError(listOf(TypeDoesNotExist(customTypeName, path)))
        }
        else
        {
            val errors = mutableListOf<DocParseError>()
            val docs = mutableListOf<SpecDoc>()

            yamlValue.list.forEachIndexed { index, yamlListValue ->

                val indexPath = path withLocation DocIndexNode(index)
                val docParse = customTypeParser(customType.type, yamlListValue, null, indexPath,
                                        spec, specDependencies)

                when (docParse)
                {
                    is Val -> docs.add(docParse.value)
                    is Err -> errors.addAll(docParse.error)
                }
            }

            if (errors.size > 0)
                docError<SpecDoc>(errors)
            else
                docResult<SpecDoc>(DocList(docs, null, path))
        }
    }
    is YamlNull -> docResult<SpecDoc>(DocList(listOf(), null, path))
    else        -> docError(listOf(UnexpectedType(YamlType.ARRAY, yamlType(yamlValue), path)))
}


// ---------------------------------------------------------------------------------------------
// PRIMITIVE VALUE PARSERS
// ---------------------------------------------------------------------------------------------

// Number Parser
// ---------------------------------------------------------------------------------------------

private fun primNumberParser(yamlValue : YamlValue,
                             path : DocPath,
                             case : String?) : DocParse = when (yamlValue)
{
    is YamlInteger -> docResult(DocNumber(yamlValue.number.toDouble(), case, path))
    is YamlFloat   -> docResult(DocNumber(yamlValue.number, case, path))
    else           -> docError(listOf(UnexpectedType(YamlType.FLOAT, yamlType(yamlValue), path)))
}


// String Parser
// ---------------------------------------------------------------------------------------------

private fun primStringParser(yamlValue : YamlValue,
                             path : DocPath,
                             case : String?) : DocParse = when (yamlValue)
{
    is YamlText -> docResult(DocText(yamlValue.text, case, path))
    else        -> docError(listOf(UnexpectedType(YamlType.TEXT, yamlType(yamlValue), path)))
}


// Boolean Parser
// ---------------------------------------------------------------------------------------------

private fun primBooleanParser(yamlValue : YamlValue,
                              path : DocPath,
                              case : String?) : DocParse = when (yamlValue)
{
    is YamlBool -> docResult(DocBoolean(yamlValue.bool, case, path))
    else        -> docError(listOf(UnexpectedType(YamlType.BOOL, yamlType(yamlValue), path)))
}


// ---------------------------------------------------------------------------------------------
// HELPERS
// ---------------------------------------------------------------------------------------------

/**
 * Search for a custom type definition in the spec being parsed or in one of its dependencies.
 */
fun specType(typeName : TypeName, specs : List<Spec>) : LuloType?
{
    System.out.print("specs size:" + specs.size)
    return specs.firstOrNull { it.hasType(typeName) }
                    ?.typeWithName(typeName)
}

