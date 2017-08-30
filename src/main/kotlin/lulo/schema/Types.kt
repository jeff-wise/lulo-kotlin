
package lulo.schema


import com.kispoko.culebra.*
import effect.*
import lulo.document.*
import lulo.document.YamlStringError
import lulo.value.UnexpectedType
import lulo.value.UnknownCase
import lulo.value.ValueError
import lulo.value.ValueParser



// ---------------------------------------------------------------------------------------------
// SPECIFICATION
// ---------------------------------------------------------------------------------------------

/**
 * Specification
 */
data class Schema(val version : SchemaVersion,
                  val metadata : SchemaMetadata,
                  val description : Maybe<SchemaDescription>,
                  val rootTypeName : TypeName,
                  val types : List<SchemaType>,
                  val constraints : List<Constraint>)
{

    // -----------------------------------------------------------------------------------------
    // INIT
    // -----------------------------------------------------------------------------------------

    val typeByName = types.associateBy { it.name }


    // -----------------------------------------------------------------------------------------
    // CONSTRUCTORS
    // -----------------------------------------------------------------------------------------

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Schema> = when (doc)
        {
            is DocDict ->
            {
                apply(::Schema,
                      // Version
                      doc.at("version").apply { SchemaVersion.fromDocument(it) },
                      // Metadata
                      doc.at("metadata").apply { SchemaMetadata.fromDocument(it) },
                      // Description
                      split(doc.maybeAt("description"),
                            effValue<ValueError,Maybe<SchemaDescription>>(Nothing()),
                            { apply(::Just, SchemaDescription.fromDocument(it)) }),
                      // Root Type Name
                      doc.at("root_type").apply { TypeName.fromDocument(it) },
                      // Types
                      doc.list("types").apply {
                          it.map { SchemaType.fromDocument(it) } },
                      // Constraints
                      doc.list("constraints").apply {
                          it.map { Constraint.fromDocument(it) } }
                      )
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }


    // -----------------------------------------------------------------------------------------
    // TYPES
    // -----------------------------------------------------------------------------------------

    fun hasType(typeName: TypeName) : Boolean =
        typeByName.containsKey(typeName)


    fun typeWithName(typeName: TypeName) : SchemaType? = typeByName[typeName]


    // -----------------------------------------------------------------------------------------
    // PARSE DOCUMENT
    // -----------------------------------------------------------------------------------------

    fun document(yamlString : String) : SchemaDoc? =
        this.document(yamlString, listOf())


    fun document(yamlString : String, specDependencies : List<Schema>) : SchemaDoc?
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


    fun parseDocument(yamlString : String) : DocParser = this.parseDocument(yamlString, listOf())


    fun parseDocument(yamlString : String, specDependencies: List<Schema>) : DocParser
    {
        val stringParse = YamlString.parse(yamlString)
        val path = DocPath(listOf())

        when (stringParse)
        {
            is YamlParseValue ->
            {
                val rootType = specType(this.rootTypeName, specDependencies.plus(this))
                return if (rootType == null)
                    docError(listOf(TypeDoesNotExist(this.rootTypeName, path)))
                else
                    customTypeParser(rootType,
                                     stringParse.value,
                                     listOf(),
                                     path,
                                     this,
                                    specDependencies)
            }
            is YamlParseErrors ->
            {
                return docError(listOf(YamlStringError(stringParse.errors, path)))
            }
        }


    }


}


// Schema > Version
// -----------------------------------------------------------------------------

data class SchemaVersion(val versionString : String)
{

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<SchemaVersion> = when (doc)
        {
            is DocText -> effValue(SchemaVersion(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }

}


// Schema > Metadata
// -----------------------------------------------------------------------------

data class SchemaMetadata(val name : SchemaName, val authors : List<SchemaAuthor>)
{

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<SchemaMetadata> = when (doc)
        {
            is DocDict ->
            {
                apply(::SchemaMetadata,
                      // Name
                      doc.at("name").apply { SchemaName.fromDocument(it) },
                      // Authors
                      doc.list("authors").apply { it.map { SchemaAuthor.fromDocument(it) }  }
                      )
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }

}


// Schema > Metadata > Name
// -----------------------------------------------------------------------------

data class SchemaName(val value : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<SchemaName> = when (doc)
        {
            is DocText -> effValue(SchemaName(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// Schema > Metadata > Author
// -----------------------------------------------------------------------------

data class SchemaAuthor(val name : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<SchemaAuthor> = when (doc)
        {
            is DocDict -> apply(::SchemaAuthor, doc.text("name"))
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }
}


// Schema > Description
// -----------------------------------------------------------------------------

data class SchemaDescription(val overview_md : Maybe<String>)
{

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<SchemaDescription> = when (doc)
        {
            is DocDict ->
            {
                apply(::SchemaDescription,
                      // Overview Markdown
                      doc.maybeText("overview")
                      )
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }

}


// ---------------------------------------------------------------------------------------------
// SCHEMA TYPE
// ---------------------------------------------------------------------------------------------

@Suppress("UNCHECKED_CAST")
sealed class SchemaType(open val name : TypeName,
                        open val label : TypeLabel,
                        open val description : Maybe<TypeDescription>,
                        open val group : Maybe<TypeGroup>,
                        open val constraints : Set<ConstraintName>)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<SchemaType> = when (doc.case())
        {
            "product_type"   -> Product.fromDocument(doc) as ValueParser<SchemaType>
            "sum_type"       -> Sum.fromDocument(doc) as ValueParser<SchemaType>
            "primitive_type" -> Primitive.fromDocument(doc) as ValueParser<SchemaType>
            "symbol_type"    -> Symbol.fromDocument(doc) as ValueParser<SchemaType>
            else             -> effError(UnknownCase(doc.case(), doc.path))
        }
    }
}


// Type > Name
// ---------------------------------------------------------------------------------------------

data class TypeName(val name : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<TypeName> = when (doc)
        {
            is DocText -> effValue(TypeName(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// Type > Label
// ---------------------------------------------------------------------------------------------

data class TypeLabel(val name : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<TypeLabel> = when (doc)
        {
            is DocText -> effValue(TypeLabel(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// Type > Description
// ---------------------------------------------------------------------------------------------

data class TypeDescription(val name : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<TypeDescription> = when (doc)
        {
            is DocText -> effValue(TypeDescription(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// Type > Group
// ---------------------------------------------------------------------------------------------

data class TypeGroup(val name : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<TypeGroup> = when (doc)
        {
            is DocText -> effValue(TypeGroup(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// Type : Product
// ---------------------------------------------------------------------------------------------

data class Product(override val name : TypeName,
                   override val label : TypeLabel,
                   override val description : Maybe<TypeDescription>,
                   override val group : Maybe<TypeGroup>,
                   override val constraints : Set<ConstraintName>,
                   val fields : List<Field>)
                    : SchemaType(name, label, description, group, constraints)
{

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Product> = when (doc)
        {
            is DocDict ->
            {
                apply(::Product,
                      // Name
                      doc.at("name").apply { TypeName.fromDocument(it) },
                      // Label
                      doc.at("label").apply { TypeLabel.fromDocument(it) },
                      // Description
                      split(doc.maybeAt("description"),
                            effValue<ValueError,Maybe<TypeDescription>>(Nothing()),
                            { apply(::Just, TypeDescription.fromDocument(it)) }),
                      // Group
                      split(doc.maybeAt("group"),
                            effValue<ValueError,Maybe<TypeGroup>>(Nothing()),
                            { apply(::Just, TypeGroup.fromDocument(it)) }),
                      // Constraints
                      split(doc.maybeList("constraints"),
                            effValue(setOf()),
                            { it.mapSet { ConstraintName.fromDocument(it) } }),
                      // Fields
                      doc.list("fields").apply {
                          it.map { Field.fromDocument(it) } }
                      )
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }

}


// Type : Sum
// ---------------------------------------------------------------------------------------------

data class Sum(override val name : TypeName,
               override val label : TypeLabel,
               override val description : Maybe<TypeDescription>,
               override val group : Maybe<TypeGroup>,
               override val constraints : Set<ConstraintName>,
               val cases : List<Case>)
                : SchemaType(name, label, description, group, constraints)
{

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Sum> = when (doc)
        {
            is DocDict ->
            {
                apply(::Sum,
                      // Name
                      doc.at("name").apply { TypeName.fromDocument(it) },
                      // Label
                      doc.at("label").apply { TypeLabel.fromDocument(it) },
                      // Description
                      split(doc.maybeAt("description"),
                            effValue<ValueError,Maybe<TypeDescription>>(Nothing()),
                            { apply(::Just, TypeDescription.fromDocument(it)) }),
                      // Group
                      split(doc.maybeAt("group"),
                              effValue<ValueError,Maybe<TypeGroup>>(Nothing()),
                              { apply(::Just, TypeGroup.fromDocument(it)) }),
                      // Constraints
                      split(doc.maybeList("constraints"),
                            effValue(setOf()),
                            { it.mapSet { ConstraintName.fromDocument(it) } }),
                      // Cases
                      doc.list("cases").apply {
                          it.map { Case.fromDocument(it) } }
                      )
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }



    fun hasCase(caseTypeName : TypeName) : Boolean =
        this.cases.filter { it.type == caseTypeName }.isNotEmpty()
//
//        for (case in this.cases)
//        {
//            val caseType = case.type
//            when (caseType) {
//                is Custom -> if (caseType.name == caseTypeName) return true
//            }
//        }
//
//        return false
//    }


}


// Type : Symbol
// -----------------------------------------------------------------------------

data class Symbol(override val name : TypeName,
                  override val label : TypeLabel,
                  override val description : Maybe<TypeDescription>,
                  override val group : Maybe<TypeGroup>,
                  override val constraints : Set<ConstraintName>,
                  val symbol : String)
                   : SchemaType(name, label, description, group, constraints)
{

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Symbol> = when (doc)
        {
            is DocDict ->
            {
                apply(::Symbol,
                      // Name
                      doc.at("name").apply { TypeName.fromDocument(it) },
                      // Label
                      doc.at("label").apply { TypeLabel.fromDocument(it) },
                      // Description
                      split(doc.maybeAt("description"),
                            effValue<ValueError,Maybe<TypeDescription>>(Nothing()),
                            { apply(::Just, TypeDescription.fromDocument(it)) }),
                      // Group
                      split(doc.maybeAt("group"),
                              effValue<ValueError,Maybe<TypeGroup>>(Nothing()),
                              { apply(::Just, TypeGroup.fromDocument(it)) }),
                      // Constraints
                      split(doc.maybeList("constraints"),
                            effValue(setOf()),
                            { it.mapSet { ConstraintName.fromDocument(it) } }),
                      // Symbol
                      doc.text("symbol")
                      )
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }

}


// Type > Object Type > Primitive
// -----------------------------------------------------------------------------

data class Primitive(override val name : TypeName,
                     override val label : TypeLabel,
                     override val description : Maybe<TypeDescription>,
                     override val group : Maybe<TypeGroup>,
                     override val constraints : Set<ConstraintName>,
                     val baseTypeName : TypeName)
                      : SchemaType(name, label, description, group, constraints)
{

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Primitive> = when (doc)
        {
            is DocDict ->
            {
                apply(::Primitive,
                      // Name
                      doc.at("name").apply { TypeName.fromDocument(it) },
                      // Label
                      doc.at("label").apply { TypeLabel.fromDocument(it) },
                      // Description
                      split(doc.maybeAt("description"),
                            effValue<ValueError,Maybe<TypeDescription>>(Nothing()),
                            { apply(::Just, TypeDescription.fromDocument(it)) }),
                      // Group
                      split(doc.maybeAt("group"),
                              effValue<ValueError,Maybe<TypeGroup>>(Nothing()),
                              { apply(::Just, TypeGroup.fromDocument(it)) }),
                      // Constraints
                      split(doc.maybeList("constraints"),
                            effValue(setOf()),
                            { it.mapSet { ConstraintName.fromDocument(it) } }),
                      // Base Type Name
                      doc.at("base_type").apply { TypeName.fromDocument(it) }
                      )
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }

}


// ---------------------------------------------------------------------------------------------
// FIELD
// ---------------------------------------------------------------------------------------------

data class Field(val name : FieldName,
                 val presence : FieldPresence,
                 val description : Maybe<FieldDescription>,
                 val valueType : ValueType,
                 val defaultValue : Maybe<FieldDefaultValue>)
{

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Field> = when (doc)
        {
            is DocDict ->
            {
                apply(::Field,
                      // Name
                      doc.at("name").apply { FieldName.fromDocument(it) },
                      // Presence
                      doc.at("presence").apply { FieldPresence.fromDocument(it) },
                      // Description
                      split(doc.maybeAt("description"),
                            effValue<ValueError,Maybe<FieldDescription>>(Nothing()),
                            { apply(::Just, FieldDescription.fromDocument(it)) }),
                      // Value Type
                      doc.at("type").apply { ValueType.fromDocument(it) },
                      // Default Value
                      split(doc.maybeAt("default_value"),
                            effValue<ValueError,Maybe<FieldDefaultValue>>(Nothing()),
                            { apply(::Just, FieldDefaultValue.fromDocument(it)) })
                      )
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }
}


// Field > Name
// ---------------------------------------------------------------------------------------------

data class FieldName(val name : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<FieldName> = when (doc)
        {
            is DocText -> effValue(FieldName(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// Field > Presence
// -----------------------------------------------------------------------------

sealed class FieldPresence
{

    object Required : FieldPresence()


    object Optional : FieldPresence()


    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<FieldPresence> = when (doc)
        {
            is DocText -> when (doc.text)
            {
                "required" -> effValue<ValueError,FieldPresence>(FieldPresence.Required)
                "optional" -> effValue<ValueError,FieldPresence>(FieldPresence.Optional)
                else       -> effError<ValueError,FieldPresence>(UnknownCase(doc.text, doc.path))
            }
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// Field > Description
// -----------------------------------------------------------------------------

data class FieldDescription(val description : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<FieldDescription> = when (doc)
        {
            is DocText -> effValue(FieldDescription(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// Field > Default Value
// -----------------------------------------------------------------------------

data class FieldDefaultValue(val value : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<FieldDefaultValue> = when (doc)
        {
            is DocText -> effValue(FieldDefaultValue(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// ---------------------------------------------------------------------------------------------
// CASE
// ---------------------------------------------------------------------------------------------

data class Case(val type : TypeName,
                val description : Maybe<CaseDescription>)
{

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Case> = when (doc)
        {
            is DocDict ->
            {
                apply(::Case,
                      // Type
                      doc.at("type").apply { TypeName.fromDocument(it) },
                      // Description
                      split(doc.maybeAt("description"),
                            effValue<ValueError,Maybe<CaseDescription>>(Nothing()),
                            { apply(::Just, CaseDescription.fromDocument(it)) })
                    )
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }

}


// Case > Description
// ---------------------------------------------------------------------------------------------

data class CaseDescription(val text : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<CaseDescription> = when (doc)
        {
            is DocText -> effValue(CaseDescription(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// ---------------------------------------------------------------------------------------------
// VALUE TYPE
// ---------------------------------------------------------------------------------------------

@Suppress("UNCHECKED_CAST")
sealed class ValueType
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<ValueType> = when (doc.case())
        {
            "prim_type"        -> Prim.fromDocument(doc) as ValueParser<ValueType>
            "prim_coll_type"   -> PrimList.fromDocument(doc) as ValueParser<ValueType>
            "custom_type"      -> Custom.fromDocument(doc) as ValueParser<ValueType>
            "custom_coll_type" -> CustomList.fromDocument(doc) as ValueParser<ValueType>
            else               -> effError(UnknownCase(doc.case(), doc.path))
        }
    }
}


// Value Type : Prim
// ---------------------------------------------------------------------------------------------

data class Prim(val type : PrimValueType) : ValueType()
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Prim> =
                apply(::Prim, PrimValueType.fromDocument(doc))
    }

}


// Value Type : Prim List
// ---------------------------------------------------------------------------------------------

data class PrimList(val type : PrimValueType) : ValueType()
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<PrimList> =
                apply(::PrimList, PrimValueType.fromDocument(doc))
    }
}


// Value Type : Custom
// ---------------------------------------------------------------------------------------------

data class Custom(val name : TypeName) : ValueType()
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Custom> =
                apply(::Custom, TypeName.fromDocument(doc))
    }
}


// Value Type : Custom List
// ---------------------------------------------------------------------------------------------

data class CustomList(val name : TypeName) : ValueType()
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<CustomList> =
                apply(::CustomList, TypeName.fromDocument(doc))
    }
}


// Value Type > Primitive
// ---------------------------------------------------------------------------------------------

sealed class PrimValueType
{

    object Any     : PrimValueType()


    object Number  : PrimValueType()


    object String  : PrimValueType()


    object Boolean : PrimValueType()


    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<PrimValueType> = when (doc)
        {
            is DocText -> when (doc.text)
            {
                "any"     -> effValue<ValueError,PrimValueType>(PrimValueType.Any)
                "number"  -> effValue<ValueError,PrimValueType>(PrimValueType.Number)
                "string"  -> effValue<ValueError,PrimValueType>(PrimValueType.String)
                "boolean" -> effValue<ValueError,PrimValueType>(PrimValueType.Boolean)
                else      -> effError<ValueError,PrimValueType>(UnknownCase(doc.text, doc.path))
            }
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}



fun primValueType(typeName : TypeName) : PrimValueType? = when (typeName.name)
{
    "any"     -> PrimValueType.Any
    "number"  -> PrimValueType.Number
    "string"  -> PrimValueType.String
    "boolean" -> PrimValueType.Boolean
    else      -> null
}


// Value Kind
// ---------------------------------------------------------------------------------------------

sealed class ValueKind
{
    object Primitive : ValueKind()

    object Collection : ValueKind()

    object Custom : ValueKind()
}


fun isPrimitiveType(typeName : TypeName) : Boolean = primValueType(typeName) != null


//fun valueKind(typeString : String) : ValueKind = when
//{
//    primValueType(typeString) != null -> ValueKind.Primitive
//    typeString.equals("list")         -> ValueKind.Collection
//    else                              -> ValueKind.Custom
//}


// ---------------------------------------------------------------------------------------------
// CONSTRAINT
// ---------------------------------------------------------------------------------------------

@Suppress("UNCHECKED_CAST")
sealed class Constraint(open val name : ConstraintName,
                        open val description : ConstraintDescription)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Constraint> = when (doc.case())
        {
            "constraint_string_one_of"    -> StringOneOf.fromDocument(doc) as ValueParser<Constraint>
            "constraint_num_greater_than" -> NumGreaterThan.fromDocument(doc) as ValueParser<Constraint>
            else               -> effError(UnknownCase(doc.case(), doc.path))
        }
    }
}


// Cosntraint > Name
// -----------------------------------------------------------------------------

data class ConstraintName(val text : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<ConstraintName> = when (doc)
        {
            is DocText -> effValue(ConstraintName(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// Cosntraint > Description
// -----------------------------------------------------------------------------

data class ConstraintDescription(val text : String)
{
    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<ConstraintDescription> = when (doc)
        {
            is DocText -> effValue(ConstraintDescription(doc.text))
            else       -> effError(UnexpectedType(DocType.TEXT, docType(doc), doc.path))

        }
    }
}


// Cosntraint : String One Of
// -----------------------------------------------------------------------------

data class StringOneOf(override val name : ConstraintName,
                       override val description : ConstraintDescription,
                       val set : Set<String>)
                        : Constraint(name, description)
{

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<StringOneOf> = when (doc)
        {
            is DocDict ->
            {
                apply(::StringOneOf,
                      // Name
                      doc.at("name").apply { ConstraintName.fromDocument(it) },
                      // Description
                      doc.at("description").apply { ConstraintDescription.fromDocument(it) },
                      // Set
                      doc.list("set") ap { effApply({ it.toSet() }, it.stringList() )}
                      )
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }

}



data class NumGreaterThan(override val name : ConstraintName,
                          override val description : ConstraintDescription,
                          val lowerBound : Double)
                            : Constraint(name, description)
{

    companion object
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<NumGreaterThan> = when (doc)
        {
            is DocDict ->
            {
                apply(::NumGreaterThan,
                      // Name
                      doc.at("name").apply { ConstraintName.fromDocument(it) },
                      // Description
                      doc.at("description").apply { ConstraintDescription.fromDocument(it) },
                      // Set
                      doc.double("set")
                      )
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))

        }
    }

}

