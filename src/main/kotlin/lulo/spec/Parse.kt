
package lulo.spec


import com.kispoko.culebra.*
import effect.Err
import effect.Val
import lulo.document.*



// TODO pass in lulotype, so we have complete type info for debugging
fun customTypeParser(type : ObjectType,
                     yamlValue : YamlValue,
                     cases : List<String>,
                     path : DocPath,
                     spec : Spec,
                     specDependencies: List<Spec>) : DocParser = when (type)
{
    is Product   -> productTypeParser(type, yamlValue, cases, path, spec, specDependencies)
    is Sum       -> sumTypeParser(type, yamlValue, cases, path, spec, specDependencies)
    is Primitive -> simpleTypeParser(type, yamlValue, cases, path, spec, specDependencies)
    is Symbol    -> symbolTypeParser(type, yamlValue, path)
    else         -> docError(listOf(UnknownKind(path)))
}


private fun productTypeParser(productType : Product,
                              yamlValue : YamlValue,
                              cases : List<String>,
                              path : DocPath,
                              spec : Spec,
                              specDependencies : List<Spec>) : DocParser = when (yamlValue)
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
            docResult<SpecDoc>(DocDict(docMap, cases, path))
    }
    else        -> docError(listOf(ExpectedProduct(path)))
}


private fun sumTypeParser(sumType : Sum,
                          yamlValue : YamlValue,
                          cases : List<String>,
                          path : DocPath,
                          spec : Spec,
                          specDependencies : List<Spec>) : DocParser = when (yamlValue)
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
                        val caseType = specType(caseTypeName, specDependencies.plus(spec))

                        if (caseType == null)
                        {
                            docError(listOf(TypeDoesNotExist(caseTypeName, path)))
                        }
                        else if (!sumType.hasCase(caseTypeName))
                        {
                            docError(listOf(InvalidCaseType(caseTypeName, path)))
                        }
                        else {
                            val caseYamlValueParser = yamlValue.at(caseTypeName.name)
                            when (caseYamlValueParser)
                            {
                                is Result ->
                                {
                                    val newCases = cases.plus(caseTypeName.name)
                                    customTypeParser(caseType.type, caseYamlValueParser.value, newCases,
                                            path, spec, specDependencies)
                                }
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


private fun simpleTypeParser(simpleType : Primitive,
                             yamlValue : YamlValue,
                             cases : List<String>,
                             path : DocPath,
                             spec : Spec,
                             specDependencies : List<Spec>) : DocParser
{
    val typeKind = valueKind(simpleType.baseTypeName)

    if (typeKind == ValueKind.PRIMITIVE)
    {
        return primValueParser(primValueType(simpleType.baseTypeName)!!, yamlValue, path, cases)
    }
    else if (typeKind == ValueKind.CUSTOM)
    {
        val baseTypeName = TypeName(simpleType.baseTypeName)
        val baseType = specType(baseTypeName, specDependencies.plus(spec))

        if (baseType == null)
            return docError(listOf(TypeDoesNotExist(baseTypeName, path)))
        else
            return customTypeParser(baseType.type, yamlValue, listOf(), path, spec, specDependencies)
    }
    else
    {
        return docError(listOf(UnknownKind(path)))
    }
}


private fun symbolTypeParser(symbolType : Symbol,
                             yamlValue : YamlValue,
                             path : DocPath) : DocParser = when (yamlValue)

{
    is YamlText ->
    {
        if (yamlValue.text.equals(symbolType.symbol))
            docResult<SpecDoc>(DocText(symbolType.symbol, path))
        else
            docError<SpecDoc>(listOf(ExpectedSum(path)))
    }
    else        -> docError(listOf(UnexpectedType(YamlType.TEXT, yamlType(yamlValue), path)))
}


// ---------------------------------------------------------------------------------------------
// FIELD PARSER
// ---------------------------------------------------------------------------------------------

private fun fieldParser(field : Field,
                        yamlValue: YamlValue,
                        path : DocPath,
                        spec : Spec,
                        specDependencies: List<Spec>) : DocParser = when (field.valueType)
{
    is Prim       -> primValueParser(field.valueType.type, yamlValue, path, listOf())
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
                            cases : List<String>) : DocParser =
    when (primValueType)
    {
        is PrimValueType.NUMBER  -> primNumberParser(yamlValue, path, cases)
        is PrimValueType.STRING  -> primStringParser(yamlValue, path, cases)
        is PrimValueType.BOOLEAN -> primBooleanParser(yamlValue, path, cases)
        else                     -> docError(listOf(UnknownPrimType(primValueType, path)))
    }


// Primitve Value List Parser
// ---------------------------------------------------------------------------------------------

private fun primListValueParser(primValueType : PrimValueType,
                                yamlValue : YamlValue,
                                path : DocPath) : DocParser = when (yamlValue)
{
    is YamlArray ->
    {
        val errors = mutableListOf<DocParseError>()
        val docs = mutableListOf<SpecDoc>()

        for (yamlValueElement in yamlValue.list)
        {
            val elementDocParse = primValueParser(primValueType, yamlValue, path, listOf())

            when (elementDocParse) {
                is Val -> docs.add(elementDocParse.value)
                is Err -> errors.addAll(elementDocParse.error)
            }
        }

        if (errors.size > 0)
            docError<SpecDoc>(errors)
        else
            docResult<SpecDoc>(DocList(docs, listOf(), path))
    }
    else         -> docError(listOf(UnexpectedType(YamlType.ARRAY, yamlType(yamlValue), path)))
}


// Custom Value Parser
// ---------------------------------------------------------------------------------------------

private fun customValueParser(customTypeName : TypeName,
                              yamlValue : YamlValue,
                              path : DocPath,
                              spec : Spec,
                              specDependencies: List<Spec>) : DocParser
{
    val customType = specType(customTypeName, specDependencies.plus(spec))

    if (customType == null)
        return docError(listOf(TypeDoesNotExist(customTypeName, path)))
    else
        return customTypeParser(customType.type, yamlValue, listOf(), path, spec, specDependencies)
}


// Custom Value List Parser
// ---------------------------------------------------------------------------------------------

private fun customListValueParser(customTypeName : TypeName,
                                  yamlValue : YamlValue,
                                  path : DocPath,
                                  spec : Spec,
                                  specDependencies : List<Spec>) : DocParser = when (yamlValue)
{
    is YamlArray ->
    {
        val customType = specType(customTypeName, specDependencies.plus(spec))

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
                val docParse = customTypeParser(customType.type, yamlListValue, listOf(), indexPath,
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
                docResult<SpecDoc>(DocList(docs, listOf(), path))
        }
    }
    is YamlNull -> docResult<SpecDoc>(DocList(listOf(), listOf(), path))
    else        -> docError(listOf(UnexpectedType(YamlType.ARRAY, yamlType(yamlValue), path)))
}


// ---------------------------------------------------------------------------------------------
// PRIMITIVE VALUE PARSERS
// ---------------------------------------------------------------------------------------------

// Number Parser
// ---------------------------------------------------------------------------------------------

private fun primNumberParser(yamlValue : YamlValue,
                             path : DocPath,
                             cases : List<String>) : DocParser = when (yamlValue)
{
    is YamlInteger -> docResult(DocNumber(yamlValue.number.toDouble(), cases, path))
    is YamlFloat   -> docResult(DocNumber(yamlValue.number, cases, path))
    else           -> docError(listOf(UnexpectedType(YamlType.FLOAT, yamlType(yamlValue), path)))
}


// String Parser
// ---------------------------------------------------------------------------------------------

private fun primStringParser(yamlValue : YamlValue,
                             path : DocPath,
                             cases : List<String>) : DocParser = when (yamlValue)
{
    is YamlText -> docResult(DocText(yamlValue.text, cases, path))
    else        -> docError(listOf(UnexpectedType(YamlType.TEXT, yamlType(yamlValue), path)))
}


// Boolean Parser
// ---------------------------------------------------------------------------------------------

private fun primBooleanParser(yamlValue : YamlValue,
                              path : DocPath,
                              cases : List<String>) : DocParser = when (yamlValue)
{
    is YamlBool -> docResult(DocBoolean(yamlValue.bool, cases, path))
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

