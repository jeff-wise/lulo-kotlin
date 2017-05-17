
package lulo



/**
 * Lulo Types
 */

// SPECIFICATION
// -----------------------------------------------------------------------------

data class Spec(val version: SpecVersion,
                val authors: List<SpecAuthor>,
                val description: SpecDescription?,
                val types: List<LuloType>,
                val constraints: List<LuloConstraint>)


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

data class TypeData(val name : String,
                    val label : String,
                    val description : String?,
                    val group : String?)


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

data class Case(val name : CaseName,
                val description : CaseDescription,
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

