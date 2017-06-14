
package lulo.spec


import com.kispoko.culebra.*
import com.kispoko.culebra.StringErrors
import com.kispoko.culebra.StringResult
import effect.Err
import effect.Val
import lulo.document.*



// ---------------------------------------------------------------------------------------------
// SPECIFICATION
// ---------------------------------------------------------------------------------------------

/**
 * Specification
 */
data class Spec(val version : SpecVersion,
                val authors : List<SpecAuthor>,
                val description : SpecDescription?,
                val rootTypeName : TypeName,
                val types : List<LuloType>,
                val constraints : List<LuloConstraint>)
{

    val typeByName = types.associateBy { it.data.name }


    fun hasType(typeName: TypeName) : Boolean =
        typeByName.containsKey(typeName)

    fun typeWithName(typeName: TypeName) : LuloType? = typeByName[typeName]


    fun document(yamlString : String) : SpecDoc? =
        this.document(yamlString, listOf())


    fun document(yamlString : String, specDependencies : List<Spec>) : SpecDoc?
    {
        val docParse = parseDocument(yamlString, specDependencies)
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


    fun parseDocument(yamlString : String, specDependencies: List<Spec>) : DocParse
    {
        val stringParse = YamlString.parse(yamlString)
        val path = DocPath(listOf())

        when (stringParse)
        {
            is StringResult ->
            {
                val rootType = specType(this.rootTypeName, specDependencies.plus(this))
                if (rootType == null)
                    return docError(listOf(TypeDoesNotExist(this.rootTypeName, path)))
                else
                    return customTypeParser(rootType.type, stringParse.value, null, path, this, specDependencies)
            }
            is StringErrors ->
            {
                return docError(listOf(YamlStringError(stringParse.errors, path)))
            }
        }


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
                    val group : String?,
                    val constraints: Set<ConstraintName>)


// Type > Data > Name
// -----------------------------------------------------------------------------

data class TypeName(val name : String)


// Type > Object Type
// -----------------------------------------------------------------------------

sealed class ObjectType


// Type > Object Type > Product
// -----------------------------------------------------------------------------

data class Product(val fields : List<Field>) : ObjectType()


// Type > Object Type > Sum
// -----------------------------------------------------------------------------

data class Sum(val cases : List<Case>) : ObjectType()


// Type > Object Type > Simple
// -----------------------------------------------------------------------------

data class Simple(val baseTypeName : String) : ObjectType()


// FIELD
// -----------------------------------------------------------------------------

data class Field(val name : FieldName,
                 val presence : FieldPresence,
                 val description : FieldDescription?,
                 val valueType : ValueType,
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

data class Case(val type : ValueType,
                val description : CaseDescription?)


// Case > Description
// ---------------------------------------------------------------------------------------------

data class CaseDescription(val text : String)


// ---------------------------------------------------------------------------------------------
// VALUE TYPE
// ---------------------------------------------------------------------------------------------

sealed class ValueType

data class Prim(val type : PrimValueType) : ValueType()
data class PrimList(val type : PrimValueType) : ValueType()
data class Custom(val name : TypeName) : ValueType()
data class CustomList(val name : TypeName) : ValueType()


// Value Type > Primitive
// ---------------------------------------------------------------------------------------------

sealed class PrimValueType
{
    object ANY     : PrimValueType()
    object NUMBER  : PrimValueType()
    object STRING  : PrimValueType()
    object BOOLEAN : PrimValueType()
}



fun primValueType(text : String) : PrimValueType? = when (text)
{
    "any"     -> PrimValueType.ANY
    "number"  -> PrimValueType.NUMBER
    "string"  -> PrimValueType.STRING
    "boolean" -> PrimValueType.BOOLEAN
    else      -> null
}


enum class ValueKind
{
    PRIMITIVE,
    COLLECTION,
    CUSTOM
}


fun valueKind(typeString: String) : ValueKind =
    if (primValueType(typeString) != null)
    {
        ValueKind.PRIMITIVE
    }
    else if (typeString.equals("list"))
    {
        ValueKind.COLLECTION
    }
    else
    {
        ValueKind.CUSTOM
    }


// ---------------------------------------------------------------------------------------------
// LULO CONSTRAINT
// ---------------------------------------------------------------------------------------------

data class LuloConstraint(val data : ConstraintData,
                          val constraint : ValueConstraint)

// Lulo Cosntraint > Data
// -----------------------------------------------------------------------------

data class ConstraintData(val name : ConstraintName,
                          val description : ConstraintDescription?)

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

