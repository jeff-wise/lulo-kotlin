
package lulo


import com.kispoko.culebra.*



/**
 * Lulo Types Yaml Parsing
 */
object Yaml
{

    // SPECIFICATION
    // -----------------------------------------------------------------------------------------

    fun parseSpec(yamlValue : YamlValue) : Parser<Spec> = when (yamlValue)
    {
        is YamlDict -> parserApply5(::Spec,
                                    yamlValue.at("version") ap { x -> parseSpecVersion(x) },
                                    yamlValue.at("authors") ap { x -> parseAuthors(x) },
                                    yamlValue.at("description") ap { x -> parseSpecDescription(x) },
                                    yamlValue.at("types") ap { x -> parseTypes(x) },
                                    yamlValue.maybeAt("constraints") ap { x -> parseConstraints(x) })
        else         -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // Specification > Version
    // -----------------------------------------------------------------------------------------

    fun parseSpecVersion(yamlValue : YamlValue) : Parser<SpecVersion> = when (yamlValue)
    {
        is YamlText -> result(SpecVersion(yamlValue.text))
        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
    }

    // Specification > Author
    // -----------------------------------------------------------------------------------------

    fun parseSpecAuthor(yamlValue : YamlValue) : Parser<SpecAuthor> = when (yamlValue)
    {
        is YamlText -> result(SpecAuthor(yamlValue.text))
        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
    }

    // Specification > Authors
    // -----------------------------------------------------------------------------------------

    fun parseAuthors(yamlValue : YamlValue) : Parser<List<SpecAuthor>> = when (yamlValue)
    {
        is YamlArray -> yamlValue.map { parseSpecAuthor(it) }
        else         -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
    }

    // Specification > Description
    // -----------------------------------------------------------------------------------------

    fun parseSpecDescription(yamlValue : YamlValue) : Parser<SpecDescription> = when (yamlValue)
    {
        is YamlDict -> parserApply(::SpecDescription,
                                   yamlValue.maybeText("overview_md"))
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // LULO TYPE
    // -----------------------------------------------------------------------------------------

    // Type
    // -----------------------------------------------------------------------------------------

    fun parseType(yamlValue : YamlValue) : Parser<LuloType> = when (yamlValue)
    {
        is YamlDict -> {
            yamlValue.at("type") ap {
                when (it) {
                    is YamlText -> {
                        if (it.text == "product")
                            parseProductType(yamlValue)
                        else
                            parseSumType(yamlValue)
                    }
                    else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
                }
            }
        }
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // Type List
    // -----------------------------------------------------------------------------------------

    fun parseTypes(yamlValue : YamlValue) : Parser<List<LuloType>> = when (yamlValue)
    {
        is YamlArray -> yamlValue.map { parseType(it) }
        else         -> error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
    }

    // Type > Data
    // -----------------------------------------------------------------------------------------

    fun parseTypeData(yamlValue : YamlValue) : Parser<TypeData> = when (yamlValue)
    {
        is YamlDict -> parserApply4(::TypeData,
                                    yamlValue.text("name"),
                                    yamlValue.text("label"),
                                    yamlValue.maybeText("description"),
                                    yamlValue.maybeText("group"))
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // Type > Product
    // -----------------------------------------------------------------------------------------

    fun parseProductType(yamlValue : YamlValue) : Parser<LuloType> = when (yamlValue)
    {
        is YamlDict -> parserApply2(::LuloType,
                                    parseTypeData(yamlValue),
                                    parseProductObjectType(yamlValue))
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    fun parseProductObjectType(yamlValue : YamlValue) : Parser<ObjectType> = when (yamlValue)
    {
        is YamlDict -> parserApply(::Product,
                                   yamlValue.at("fields") ap { x -> parseFields(x) })
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // Type > Sum
    // -----------------------------------------------------------------------------------------

    fun parseSumType(yamlValue : YamlValue) : Parser<LuloType> = when (yamlValue)
    {
        is YamlDict -> parserApply2(::LuloType,
                                    parseTypeData(yamlValue),
                                    parseSumObjectType(yamlValue))
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    fun parseSumObjectType(yamlValue : YamlValue) : Parser<ObjectType> = when (yamlValue)
    {
        is YamlDict -> parserApply(::Sum,
                                   yamlValue.at("cases") ap { x -> parseCases(x) })
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // FIELD
    // -----------------------------------------------------------------------------------------

    // Field
    // -----------------------------------------------------------------------------------------

    fun parseField(yamlValue : YamlValue) : Parser<Field> = when (yamlValue)
    {
        is YamlDict -> parserApply6(::Field,
                                    yamlValue.at("name") ap { x -> parseFieldName(x) },
                                    yamlValue.at("presence") ap { x -> parseFieldPresence(x) },
                                    yamlValue.maybeAt("description") ap { x -> parseFieldDescription(x) },
                                    parseValueType(yamlValue),
                                    yamlValue.maybeAt("constraints") ap { x -> parseConstraintNames(x) },
                                    yamlValue.maybeAt("default_value") ap { x -> parseFieldDefaultValue(x) })
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // Field List
    // -----------------------------------------------------------------------------------------

    fun parseFields(yamlValue : YamlValue) : Parser<List<Field>> = when (yamlValue)
    {
        is YamlArray -> yamlValue.map { parseField(it) }
        else         -> error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
    }

    // Field > Name
    // -----------------------------------------------------------------------------------------

    fun parseFieldName(yamlValue : YamlValue) : Parser<FieldName> = when (yamlValue)
    {
        is YamlText -> result(FieldName(yamlValue.text))
        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
    }

    // Field > Presence
    // -----------------------------------------------------------------------------------------

    fun parseFieldPresence(yamlValue : YamlValue) : Parser<FieldPresence> = when (yamlValue)
    {
        is YamlText -> when (yamlValue.text) {
            "required" -> result(FieldPresence.REQUIRED)
            "optional" -> result(FieldPresence.OPTIONAL)
            else       -> error(UnexpectedStringValue(yamlValue.text))
        }
        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
    }

    // Field > Description
    // -----------------------------------------------------------------------------------------

    fun parseFieldDescription(yamlValue : YamlValue?) : Parser<FieldDescription?>
    {
        if (yamlValue == null)
            return result(null)

        when (yamlValue)
        {
            is YamlText -> return result(FieldDescription(yamlValue.text))
            else        -> return error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
        }
    }

    // Field > Default Value
    // -----------------------------------------------------------------------------------------

    fun parseFieldDefaultValue(yamlValue: YamlValue?) : Parser<FieldDefaultValue?>
    {
        if (yamlValue == null)
            return result(null)

        when (yamlValue)
        {
            is YamlText -> return result(FieldDefaultValue(yamlValue.text))
            else        -> return error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
        }
    }

    // CASE
    // -----------------------------------------------------------------------------------------

    // Case
    // -----------------------------------------------------------------------------------------

    fun parseCase(yamlValue : YamlValue) : Parser<Case> = when (yamlValue)
    {
        is YamlDict -> parserApply3(::Case,
                                    yamlValue.at("name") ap { x -> parseCaseName(x) },
                                    yamlValue.at("description") ap { x -> parseCaseDescription(x) },
                                    parseValueType(yamlValue))
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // Case List
    // -----------------------------------------------------------------------------------------

    fun parseCases(yamlValue : YamlValue) : Parser<List<Case>> = when (yamlValue)
    {
        is YamlArray -> yamlValue.map { parseCase(it) }
        else         -> error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
    }

    // Case > Name
    // -----------------------------------------------------------------------------------------

    fun parseCaseName(yamlValue : YamlValue) : Parser<CaseName> = when (yamlValue)
    {
        is YamlText -> result(CaseName(yamlValue.text))
        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
    }

    // Case > Description
    // -----------------------------------------------------------------------------------------

    fun parseCaseDescription(yamlValue : YamlValue) : Parser<CaseDescription> = when (yamlValue)
    {
        is YamlText -> result(CaseDescription(yamlValue.text))
        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
    }

    // VALUE TYPE
    // -----------------------------------------------------------------------------------------

    // Value Type
    // -----------------------------------------------------------------------------------------

    fun parseValueType(yamlValue : YamlValue) : Parser<ValueType> = when (yamlValue)
    {
        is YamlDict -> yamlValue.text("type") ap { typeString ->
            val kind = valueKind(typeString)
            if (kind == ValueKind.PRIMITIVE) {
                parsePrimType(typeString)
            }
            else if (kind == ValueKind.COLLECTION) {
                parseCollectionType(yamlValue)
            }
            else {
                parseCustomType(typeString)
            }
        }
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // Value Type > Primitive
    // -----------------------------------------------------------------------------------------

    fun parsePrimType(text : String) : Parser<ValueType>
    {
        val primValueType = primValueType(text)

        if (primValueType != null)
            return result(Prim(primValueType))
        else
            return error(UnexpectedStringValue(text))
    }

    // Value Type > Custom
    // -----------------------------------------------------------------------------------------

    fun parseCustomType(typeString : String) : Parser<ValueType> =
            result(Custom(CustomValueType(typeString)))

    // Value Type > Collection
    // -----------------------------------------------------------------------------------------

    fun parseCollectionType(yamlDict : YamlDict) : Parser<ValueType>
    {
        return yamlDict.text("of") ap { typeString ->
            val kind = valueKind(typeString)
            if (kind == ValueKind.PRIMITIVE)
                parsePrimList(typeString)
            else if (kind == ValueKind.CUSTOM)
                parseCustomList(typeString)
            else
                error(UnexpectedStringValue(typeString))
        }
    }

    // Value Type > Collection > Primitive
    // -----------------------------------------------------------------------------------------

    fun parsePrimList(typeString : String) : Parser<ValueType>
    {
        val primValueType = primValueType(typeString)
        if (primValueType != null)
            return result(PrimList(primValueType))
        else
            return error(UnexpectedStringValue(typeString))
    }

    // Value Type > Collection > Custom
    // -----------------------------------------------------------------------------------------

    fun parseCustomList(typeString : String) : Parser<ValueType> =
        result(CustomList(CustomValueType(typeString)))

    // CONSTRAINT
    // -----------------------------------------------------------------------------------------

    // Constraint
    // -----------------------------------------------------------------------------------------

    fun parseLuloConstraint(yamlValue : YamlValue) : Parser<LuloConstraint> = when (yamlValue)
    {
        is YamlDict -> parserApply2(::LuloConstraint,
                                    parseConstraintData(yamlValue),
                                    parseValueConstraint(yamlValue))
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // Constraint List
    // -----------------------------------------------------------------------------------------

    fun parseConstraints(yamlValue : YamlValue?) : Parser<List<LuloConstraint>>
    {
        if (yamlValue == null)
            return result(listOf())

        when (yamlValue)
        {
            is YamlArray -> return yamlValue.map { parseLuloConstraint(it) }
            else         -> return error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
        }
    }

    // Constraint Names
    // -----------------------------------------------------------------------------------------

    fun parseConstraintNames(yamlValue : YamlValue?) : Parser<Set<ConstraintName>>
    {
        if (yamlValue == null)
            return result(setOf())

        when (yamlValue)
        {
            is YamlArray -> return yamlValue.mapSet { x -> parseConstraintName(x) }
            else         -> return error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
        }
    }

    // Constraint > Data
    // -----------------------------------------------------------------------------------------

    fun parseConstraintData(yamlValue : YamlValue) : Parser<ConstraintData> = when (yamlValue)
    {
        is YamlDict -> parserApply2(::ConstraintData,
                                    parseConstraintName(yamlValue),
                                    parseConstraintDescription(yamlValue))
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // Constraint > Data > Name
    // -----------------------------------------------------------------------------------------

    fun parseConstraintName(yamlValue : YamlValue) : Parser<ConstraintName> = when (yamlValue)
    {
        is YamlText -> result(ConstraintName(yamlValue.text))
        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
    }

    // Constraint > Data > Description
    // -----------------------------------------------------------------------------------------

    fun parseConstraintDescription(yamlValue : YamlValue) : Parser<ConstraintDescription> = when (yamlValue)
    {
        is YamlText -> result(ConstraintDescription(yamlValue.text))
        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
    }

    // VALUE CONSTRAINT
    // -----------------------------------------------------------------------------------------

    // Value Constraint
    // -----------------------------------------------------------------------------------------

    fun parseValueConstraint(yamlValue : YamlValue) : Parser<ValueConstraint> = when (yamlValue)
    {
        is YamlDict -> yamlValue.text("type") ap {
            when (it) {
                "string_one_of"       -> yamlValue.at("parameters") ap { x -> parseStringOneOfConstraint(x) }
                "number_greater_than" -> yamlValue.at("parameters") ap { x -> parseNumGreaterThanConstraint(x) }
                else                  -> error(UnexpectedStringValue(it))
            }
        }
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }


    // Value Constraint > String One Of
    // -----------------------------------------------------------------------------------------

    fun parseStringOneOfConstraint(yamlValue: YamlValue) : Parser<ValueConstraint> = when (yamlValue)
    {
        is YamlDict -> parserApply(::StringOneOf, yamlValue.stringSet("set"))
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

    // Value Constraint > Num Greater Than
    // -----------------------------------------------------------------------------------------

    fun parseNumGreaterThanConstraint(yamlValue: YamlValue) : Parser<ValueConstraint> = when (yamlValue)
    {
        is YamlDict -> parserApply(::NumGreaterThan, yamlValue.float("greater_than"))
        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
    }

}




