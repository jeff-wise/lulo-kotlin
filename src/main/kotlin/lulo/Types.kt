
package lulo


import com.kispoko.culebra.*
import com.kispoko.culebra.StringErrors
import com.kispoko.culebra.StringResult
import effect.Err
import effect.Val
import lulo.document.*



/**
 * Lulo Types
 *
 * REALLY UGLY CODE. WORKS FOR NOW
 */

// SPECIFICATION
// -----------------------------------------------------------------------------

data class Spec(val version : SpecVersion,
                val authors : List<SpecAuthor>,
                val description : SpecDescription?,
                val rootTypeName : TypeName,
                val types : List<LuloType>,
                val constraints : List<LuloConstraint>)
{

    val typeByName = types.associateBy { it.data.name }


    fun document(yamlString : String) : SpecDoc?
    {
        val docParse = documentParse(yamlString)
        System.out.println("Spec parse document")
        when (docParse)
        {
            is Val -> return docParse.value
            is Err -> docParse.error.forEachIndexed { index, docParseError ->
                System.out.println("Error (${index + 1})")
                System.out.println(docParseError.toString())
                return null
            }
        }
        return null
    }


    fun documentParse(yamlString : String) : DocParse
    {
        val stringParse = YamlString.parse(yamlString)
        val path = DocPath(listOf())

        when (stringParse)
        {
            is StringResult ->
            {
                val rootType = typeByName[this.rootTypeName]
                if (rootType == null)
                    return docError(listOf(TypeDoesNotExist(this.rootTypeName, path)))
                else
                    return parseDocument(rootType.type, null, stringParse.value, path)
            }
            is StringErrors ->
            {
                return docError(listOf(YamlStringError(stringParse.errors, path)))
            }
        }
    }


    private fun parseDocument(type : ObjectType,
                              case : String?,
                              yamlValue : YamlValue,
                              path : DocPath) : DocParse = when (type)
    {
        is Product -> when (yamlValue)
        {
            is YamlDict ->
            {
                val docMap = hashMapOf<String,SpecDoc>()
                val errors = mutableListOf<DocParseError>()

                for (field in type.fields)
                {
                    val fieldYamlValue = yamlValue.at(field.name.name)
                    when (fieldYamlValue)
                    {
                        is Result ->
                        {
                            val fieldPath = path withLocation DocKeyNode(field.name.name)
                            val fieldParse = parseField(field, fieldYamlValue.value, fieldPath)

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
        is Sum -> when (yamlValue)
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
                                val caseType = typeByName[caseTypeName]
                                if (caseType == null) {
                                    docError(listOf(TypeDoesNotExist(caseTypeName, path)))
                                }
                                else {
                                    val caseYamlValueParser = yamlValue.at(caseTypeName.name)
                                    when (caseYamlValueParser)
                                    {
                                        is Result -> parseDocument(caseType.type, caseTypeName.name,
                                                                   caseYamlValueParser.value, path)
                                        is Error  ->
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
                    is Error  ->
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
        else   ->
        {
            docError(listOf(UnknownKind(path)))
        }

    }


    private fun parseField(field : Field,
                           yamlValue: YamlValue,
                           path : DocPath) : DocParse = when (field.valueType)
    {
        is Prim ->
        {
            val primValueType = field.valueType.type
            if (primValueType == PrimValueType.INTEGER)
                parseInteger(yamlValue, path)
            else if (primValueType == PrimValueType.NUMBER)
                parseNumber(yamlValue, path)
            else if (primValueType == PrimValueType.STRING_ALPHANUM_UNDERSCORE)
                parseString(yamlValue, path)
            else if (primValueType == PrimValueType.STRING_UTF8)
                parseString(yamlValue, path)
            else if (primValueType == PrimValueType.BOOLEAN)
                parseBoolean(yamlValue, path)
            else
                docError(listOf(UnknownPrimType(primValueType, path)))
        }
        is PrimList ->
        {
            when (yamlValue)
            {
                is YamlArray ->
                {
                    val errors = mutableListOf<DocParseError>()
                    val docs = mutableListOf<SpecDoc>()
                    val primValueType = field.valueType.type

                    for (yamlValueElement in yamlValue.list)
                    {
                        val elementDocParse =
                            if (primValueType == PrimValueType.INTEGER)
                                parseInteger(yamlValue, path)
                            else if (primValueType == PrimValueType.NUMBER)
                                parseNumber(yamlValue, path)
                            else if (primValueType == PrimValueType.STRING_ALPHANUM_UNDERSCORE)
                                parseString(yamlValue, path)
                            else if (primValueType == PrimValueType.STRING_UTF8)
                                parseString(yamlValue, path)
                            else if (primValueType == PrimValueType.BOOLEAN)
                                parseBoolean(yamlValue, path)
                            else
                                docError(listOf(UnknownPrimType(primValueType, path)))

                        when (elementDocParse) {
                            is Val -> docs.add(elementDocParse.value)
                            is Err -> errors.addAll(elementDocParse.error)
                        }
                    }

                    if (errors.size > 0)
                        docError<SpecDoc>(errors)
                    else
                        docResult<SpecDoc>(DocList(docs, path))
                }
                else         -> docError(listOf(UnexpectedType(YamlType.ARRAY, yamlType(yamlValue), path)))
            }
        }
        is Custom ->
        {
            val fieldTypeName = TypeName(field.valueType.name.name)
            val customType = typeByName[fieldTypeName]
            if (customType == null)
                docError(listOf(TypeDoesNotExist(fieldTypeName, path)))
            else
                parseDocument(customType.type, null, yamlValue, path)
        }
        is CustomList ->
        {
            when (yamlValue)
            {
                is YamlArray ->
                {
                    val customTypeName = TypeName(field.valueType.name.name)
                    val customType = typeByName[customTypeName]

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
                            val docParse = parseDocument(customType.type, null, yamlListValue, indexPath)

                            when (docParse)
                            {
                                is Val -> docs.add(docParse.value)
                                is Err -> errors.addAll(docParse.error)
                            }
                        }

                        if (errors.size > 0)
                            docError<SpecDoc>(errors)
                        else
                            docResult<SpecDoc>(DocList(docs, path))
                    }
                }
                else         -> docError(listOf(UnexpectedType(YamlType.ARRAY, yamlType(yamlValue), path)))
            }
        }
    }


    private fun parseInteger(yamlValue : YamlValue, path : DocPath) : DocParse = when (yamlValue)
    {
        is YamlInteger -> docResult(DocInteger(yamlValue.number, path))
        else           -> docError(listOf(UnexpectedType(YamlType.INTEGER, yamlType(yamlValue), path)))
    }


    private fun parseNumber(yamlValue : YamlValue, path : DocPath) : DocParse = when (yamlValue)
    {
        is YamlFloat -> docResult(DocNumber(yamlValue.number, path))
        else         -> docError(listOf(UnexpectedType(YamlType.FLOAT, yamlType(yamlValue), path)))
    }


    private fun parseString(yamlValue : YamlValue, path : DocPath) : DocParse = when (yamlValue)
    {
        is YamlText -> docResult(DocText(yamlValue.text, path))
        else        -> docError(listOf(UnexpectedType(YamlType.TEXT, yamlType(yamlValue), path)))
    }


    private fun parseBoolean(yamlValue : YamlValue, path : DocPath) : DocParse = when (yamlValue)
    {
        is YamlBool -> docResult(DocBoolean(yamlValue.bool, path))
        else        -> docError(listOf(UnexpectedType(YamlType.BOOL, yamlType(yamlValue), path)))
    }

}



    // Specification > Version
    // -----------------------------------------------------------------------------

data class SpecVersion(val versionString: String)

// Specification > Author
// -----------------------------------------------------------------------------

data class SpecAuthor(val name: String)

// Specification > Description
// -----------------------------------------------------------------------------

data class SpecDescription(val overview_md: String?)


// LULO TYPE
// -----------------------------------------------------------------------------

data class LuloType(val data: TypeData, val type : ObjectType)

// Type > Data
// -----------------------------------------------------------------------------

data class TypeData(val name : TypeName,
                    val label : String,
                    val description : String?,
                    val group : String?)

// Type > Data > Name
// -----------------------------------------------------------------------------

data class TypeName(val name : String)

// Type > Object Type
// -----------------------------------------------------------------------------

sealed class ObjectType

// Type > Object Type > lulo.Product
// -----------------------------------------------------------------------------

data class Product(val fields : List<Field>) : ObjectType()


// Type > Object Type > lulo.Sum
// -----------------------------------------------------------------------------

data class Sum(val cases : List<Case>) : ObjectType()


// FIELD
// -----------------------------------------------------------------------------

data class Field(val name : FieldName,
                 val presence : FieldPresence,
                 val description : FieldDescription?,
                 val valueType : ValueType,
                 val constraints: Set<ConstraintName>,
                 val defaultValue : FieldDefaultValue?)

// Field > Name
// -----------------------------------------------------------------------------

data class FieldName(val name : String)

// Field > Presence
// -----------------------------------------------------------------------------

enum class FieldPresence {
    REQUIRED,
    OPTIONAL
}

// Field > Description
// -----------------------------------------------------------------------------

data class FieldDescription(val description : String)

// Field > Default Value
// -----------------------------------------------------------------------------

data class FieldDefaultValue(val value : String)

// CASE
// -----------------------------------------------------------------------------

data class Case(val description : CaseDescription,
                val type : ValueType)

// lulo.Case > Name
// -----------------------------------------------------------------------------

data class CaseName(val text : String)

// lulo.Case > Description
// -----------------------------------------------------------------------------

data class CaseDescription(val text : String)


// VALUE TYPE
// -----------------------------------------------------------------------------

sealed class ValueType

data class Prim(val type : PrimValueType) : ValueType()
data class PrimList(val type : PrimValueType) : ValueType()
data class Custom(val name : CustomValueType) : ValueType()
data class CustomList(val name : CustomValueType) : ValueType()

// Value Type > lulo.Prim Value Type
// -----------------------------------------------------------------------------

enum class PrimValueType {
    ANY,
    INTEGER,
    NUMBER,
    STRING_ALPHANUM_UNDERSCORE,
    STRING_UTF8,
    BOOLEAN
}


fun primValueType(text : String) : PrimValueType? = when (text)
{
    "any"                        -> PrimValueType.ANY
    "integer"                    -> PrimValueType.INTEGER
    "number"                     -> PrimValueType.NUMBER
    "string_alphanum_underscore" -> PrimValueType.STRING_ALPHANUM_UNDERSCORE
    "string_utf8"                -> PrimValueType.STRING_UTF8
    "boolean"                    -> PrimValueType.BOOLEAN
    else                         -> null
}


enum class ValueKind {
    PRIMITIVE,
    COLLECTION,
    CUSTOM
}


fun valueKind(typeString: String) : ValueKind = when (typeString)
{
    "any"                        -> ValueKind.PRIMITIVE
    "integer"                    -> ValueKind.PRIMITIVE
    "number"                     -> ValueKind.PRIMITIVE
    "string_alphanum_underscore" -> ValueKind.PRIMITIVE
    "string_utf8"                -> ValueKind.PRIMITIVE
    "boolean"                    -> ValueKind.PRIMITIVE
    "list"                       -> ValueKind.COLLECTION
    else                         -> ValueKind.CUSTOM
}


// Value Type > Custom Value Type
// -----------------------------------------------------------------------------

data class CustomValueType(val name : String)


// LULO CONSTRAINT
// -----------------------------------------------------------------------------

data class LuloConstraint(val data : ConstraintData,
                          val constraint : ValueConstraint)

// Lulo Cosntraint > Data
// -----------------------------------------------------------------------------

data class ConstraintData(val name : ConstraintName,
                          val description : ConstraintDescription)

// Lulo Cosntraint > Data > Name
// -----------------------------------------------------------------------------

data class ConstraintName(val text : String)

// Lulo Cosntraint > Data > Description
// -----------------------------------------------------------------------------

data class ConstraintDescription(val text : String?)


// VALUE CONSTRAINT
// -----------------------------------------------------------------------------

sealed class ValueConstraint

data class StringOneOf(val set : Set<String>) : ValueConstraint()
data class NumGreaterThan(val lowerBound : Double) : ValueConstraint()

