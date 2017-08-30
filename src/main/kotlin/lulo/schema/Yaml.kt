
package lulo.schema


import com.kispoko.culebra.*

//
///**
// * Lulo Types Yaml Parsing
// */
//object Yaml
//{
//
//    // SPECIFICATION
//    // -----------------------------------------------------------------------------------------
//
//    fun parseSpec(yamlValue : YamlValue) : Parser<Schema> = when (yamlValue)
//    {
//        is YamlDict -> parserApply6(::Schema,
//                                    yamlValue.at("version") ap { x -> parseSpecVersion(x) },
//                                    yamlValue.at("metadata") ap { parseSpecMetadata(it) },
//                                    yamlValue.at("description") ap { x -> parseSpecDescription(x) },
//                                    yamlValue.at("root_type") ap { x -> parseTypeName(x) },
//                                    yamlValue.at("types") ap { x -> parseTypes(x) },
//                                    yamlValue.maybeAt("constraints") ap { x -> parseConstraints(x) })
//        else         -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Specification > Version
//    // -----------------------------------------------------------------------------------------
//
//    fun parseSpecVersion(yamlValue : YamlValue) : Parser<SchemaVersion> = when (yamlValue)
//    {
//        is YamlText -> result(SchemaVersion(yamlValue.text))
//        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//    }
//
//    // Specification > Metadata
//    // -----------------------------------------------------------------------------------------
//
//    fun parseSpecMetadata(yamlValue : YamlValue) : Parser<SchemaMetadata> = when (yamlValue)
//    {
//        is YamlDict -> parserApply2(::SchemaMetadata,
//                                    yamlValue.at("name") ap { parseSpecName(it) },
//                                    yamlValue.at("authors") ap { parseAuthors(it) })
//        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//    }
//
//    // Specification > Metadata > Name
//    // -----------------------------------------------------------------------------------------
//
//    fun parseSpecName(yamlValue : YamlValue) : Parser<SchemaName> = when (yamlValue)
//    {
//        is YamlText -> result(SchemaName(yamlValue.text))
//        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//    }
//
//    // Specification > Metadata > Author
//    // -----------------------------------------------------------------------------------------
//
//    fun parseSpecAuthor(yamlValue : YamlValue) : Parser<SchemaAuthor> = when (yamlValue)
//    {
//        is YamlDict -> parserApply(::SchemaAuthor, yamlValue.text("name"))
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Specification > Metadata > Authors
//    // -----------------------------------------------------------------------------------------
//
//    fun parseAuthors(yamlValue : YamlValue) : Parser<List<SchemaAuthor>> = when (yamlValue)
//    {
//        is YamlArray -> yamlValue.map { parseSpecAuthor(it) }
//        else         -> error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
//    }
//
//    // Specification > Description
//    // -----------------------------------------------------------------------------------------
//
//    fun parseSpecDescription(yamlValue : YamlValue) : Parser<SchemaDescription> = when (yamlValue)
//    {
//        is YamlDict -> parserApply(::SchemaDescription,
//                                   yamlValue.maybeText("overview_md"))
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//
//    // LULO TYPE
//    // -----------------------------------------------------------------------------------------
//
//    // Type
//    // -----------------------------------------------------------------------------------------
//
//    fun parseType(yamlValue : YamlValue) : Parser<LuloType> = when (yamlValue)
//    {
//        is YamlDict -> {
//            yamlValue.at("type") ap {
//                when (it) {
//                    is YamlText -> {
//                        when (it.text)
//                        {
//                            "product"   -> parseProductType(yamlValue)
//                            "sum"       -> parseSumType(yamlValue)
//                            "primitive" -> parseSimpleType(yamlValue)
//                            "symbol"    -> parseSymbolType(yamlValue)
//                            else        -> error(UnexpectedStringValue(it.text))
//                        }
//                    }
//                    else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//                }
//            }
//        }
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Type List
//    // -----------------------------------------------------------------------------------------
//
//    fun parseTypes(yamlValue : YamlValue) : Parser<List<LuloType>> = when (yamlValue)
//    {
//        is YamlArray -> yamlValue.map { parseType(it) }
//        else         -> error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
//    }
//
//    // Type > Data
//    // -----------------------------------------------------------------------------------------
//
//    fun parseTypeData(yamlValue : YamlValue) : Parser<TypeData> = when (yamlValue)
//    {
//        is YamlDict -> parserApply5(::TypeData,
//                                    yamlValue.at("name") ap { parseTypeName(it) },
//                                    yamlValue.text("label"),
//                                    yamlValue.maybeText("description"),
//                                    yamlValue.maybeText("group"),
//                                    yamlValue.maybeAt("constraints") ap { x -> parseConstraintNames(x) })
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Type > Data > Name
//    // -----------------------------------------------------------------------------------------
//
//    fun parseTypeName(yamlValue : YamlValue) : Parser<TypeName> = when (yamlValue)
//    {
//        is YamlText -> result(TypeName(yamlValue.text))
//        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//    }
//
//    // Type > Primitive
//    // -----------------------------------------------------------------------------------------
//
//    fun parseSimpleType(yamlValue : YamlValue) : Parser<LuloType> = when (yamlValue)
//    {
//        is YamlDict -> parserApply2(::LuloType,
//                                    parseTypeData(yamlValue),
//                                    parseSimpleObjectType(yamlValue))
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    fun parseSimpleObjectType(yamlValue : YamlValue) : Parser<ObjectType> = when (yamlValue)
//    {
//        is YamlDict -> parserApply(::Primitive, yamlValue.text("base_type"))
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Type > Product
//    // -----------------------------------------------------------------------------------------
//
//    fun parseProductType(yamlValue : YamlValue) : Parser<LuloType> = when (yamlValue)
//    {
//        is YamlDict -> parserApply2(::LuloType,
//                                    parseTypeData(yamlValue),
//                                    parseProductObjectType(yamlValue))
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    fun parseProductObjectType(yamlValue : YamlValue) : Parser<ObjectType> = when (yamlValue)
//    {
//        is YamlDict -> parserApply(::Product,
//                                   yamlValue.at("fields") ap { x -> parseFields(x) })
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Type > Sum
//    // -----------------------------------------------------------------------------------------
//
//    fun parseSumType(yamlValue : YamlValue) : Parser<LuloType> = when (yamlValue)
//    {
//        is YamlDict -> parserApply2(::LuloType,
//                                    parseTypeData(yamlValue),
//                                    parseSumObjectType(yamlValue))
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    fun parseSumObjectType(yamlValue : YamlValue) : Parser<ObjectType> = when (yamlValue)
//    {
//        is YamlDict -> parserApply(::Sum,
//                                   yamlValue.at("cases") ap { x -> parseCases(x) })
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//
//    // Type > Symbol
//    // -----------------------------------------------------------------------------------------
//
//    fun parseSymbolType(yamlValue : YamlValue) : Parser<LuloType> = when (yamlValue)
//    {
//        is YamlDict -> parserApply2(::LuloType,
//                                    parseTypeData(yamlValue),
//                                    parseSymbolObjectType(yamlValue))
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    fun parseSymbolObjectType(yamlValue : YamlValue) : Parser<ObjectType> = when (yamlValue)
//    {
//        is YamlDict -> parserApply(::Symbol, yamlValue.text("symbol"))
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//
//    // FIELD
//    // -----------------------------------------------------------------------------------------
//
//    // Field
//    // -----------------------------------------------------------------------------------------
//
//    fun parseField(yamlValue : YamlValue) : Parser<Field> = when (yamlValue)
//    {
//        is YamlDict -> parserApply5(::Field,
//                                    yamlValue.at("name") ap { x -> parseFieldName(x) },
//                                    yamlValue.at("presence") ap { x -> parseFieldPresence(x) },
//                                    yamlValue.maybeAt("description") ap { x -> parseFieldDescription(x) },
//                                    parseValueType(yamlValue),
//                                    yamlValue.maybeAt("default_value") ap { x -> parseFieldDefaultValue(x) })
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Field List
//    // -----------------------------------------------------------------------------------------
//
//    fun parseFields(yamlValue : YamlValue) : Parser<List<Field>> = when (yamlValue)
//    {
//        is YamlArray -> yamlValue.map { parseField(it) }
//        else         -> error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
//    }
//
//    // Field > Name
//    // -----------------------------------------------------------------------------------------
//
//    fun parseFieldName(yamlValue : YamlValue) : Parser<FieldName> = when (yamlValue)
//    {
//        is YamlText -> result(FieldName(yamlValue.text))
//        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//    }
//
//    // Field > Presence
//    // -----------------------------------------------------------------------------------------
//
//    fun parseFieldPresence(yamlValue : YamlValue) : Parser<FieldPresence> = when (yamlValue)
//    {
//        is YamlText -> when (yamlValue.text) {
//            "required" -> result(FieldPresence.REQUIRED)
//            "optional" -> result(FieldPresence.OPTIONAL)
//            else       -> error(UnexpectedStringValue(yamlValue.text))
//        }
//        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//    }
//
//    // Field > Description
//    // -----------------------------------------------------------------------------------------
//
//    fun parseFieldDescription(yamlValue : YamlValue?) : Parser<FieldDescription?>
//    {
//        if (yamlValue == null)
//            return result(null)
//
//        when (yamlValue)
//        {
//            is YamlText -> return result(FieldDescription(yamlValue.text))
//            else        -> return error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//        }
//    }
//
//    // Field > Default Value
//    // -----------------------------------------------------------------------------------------
//
//    fun parseFieldDefaultValue(yamlValue: YamlValue?) : Parser<FieldDefaultValue?>
//    {
//        if (yamlValue == null)
//            return result(null)
//
//        when (yamlValue)
//        {
//            is YamlText -> return result(FieldDefaultValue(yamlValue.text))
//            else        -> return error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//        }
//    }
//
//    // CASE
//    // -----------------------------------------------------------------------------------------
//
//    // Case
//    // -----------------------------------------------------------------------------------------
//
//    fun parseCase(yamlValue : YamlValue) : Parser<Case> = when (yamlValue)
//    {
//        is YamlDict -> parserApply2(::Case,
//                                    parseValueType(yamlValue),
//                                    yamlValue.maybeAt("description") ap { x -> parseCaseDescription(x) })
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Case List
//    // -----------------------------------------------------------------------------------------
//
//    fun parseCases(yamlValue : YamlValue) : Parser<List<Case>> = when (yamlValue)
//    {
//        is YamlArray -> yamlValue.map { parseCase(it) }
//        else         -> error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
//    }
//
//    // Case > Description
//    // -----------------------------------------------------------------------------------------
//
//    fun parseCaseDescription(yamlValue : YamlValue?) : Parser<CaseDescription?>
//    {
//        if (yamlValue == null)
//            return result(null)
//
//        when (yamlValue)
//        {
//            is YamlText -> return result(CaseDescription(yamlValue.text))
//            else        -> return error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//        }
//    }
//
//
//    // VALUE TYPE
//    // -----------------------------------------------------------------------------------------
//
//    // Value Type
//    // -----------------------------------------------------------------------------------------
//
//    fun parseValueType(yamlValue : YamlValue) : Parser<ValueType> = when (yamlValue)
//    {
//        is YamlDict -> yamlValue.text("type") ap { typeString ->
//            val kind = valueKind(typeString)
//            if (kind == ValueKind.PRIMITIVE) {
//                parsePrimType(typeString)
//            }
//            else if (kind == ValueKind.COLLECTION) {
//                parseCollectionType(yamlValue)
//            }
//            else {
//                parseCustomType(typeString)
//            }
//        }
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Value Type > Primitive
//    // -----------------------------------------------------------------------------------------
//
//    fun parsePrimType(text : String) : Parser<ValueType>
//    {
//        val primValueType = primValueType(text)
//
//        if (primValueType != null)
//            return result(Prim(primValueType))
//        else
//            return error(UnexpectedStringValue(text))
//    }
//
//    // Value Type > Custom
//    // -----------------------------------------------------------------------------------------
//
//    fun parseCustomType(typeString : String) : Parser<ValueType> =
//            result(Custom(TypeName(typeString)))
//
//    // Value Type > Collection
//    // -----------------------------------------------------------------------------------------
//
//    fun parseCollectionType(yamlDict : YamlDict) : Parser<ValueType>
//    {
//        return yamlDict.text("of") ap { typeString ->
//            val kind = valueKind(typeString)
//            if (kind == ValueKind.PRIMITIVE)
//                parsePrimList(typeString)
//            else if (kind == ValueKind.CUSTOM)
//                parseCustomList(typeString)
//            else
//                error(UnexpectedStringValue(typeString))
//        }
//    }
//
//    // Value Type > Collection > Primitive
//    // -----------------------------------------------------------------------------------------
//
//    fun parsePrimList(typeString : String) : Parser<ValueType>
//    {
//        val primValueType = primValueType(typeString)
//        if (primValueType != null)
//            return result(PrimList(primValueType))
//        else
//            return error(UnexpectedStringValue(typeString))
//    }
//
//    // Value Type > Collection > Custom
//    // -----------------------------------------------------------------------------------------
//
//    fun parseCustomList(typeString : String) : Parser<ValueType> =
//        result(CustomList(TypeName(typeString)))
//
//    // CONSTRAINT
//    // -----------------------------------------------------------------------------------------
//
//    // Constraint
//    // -----------------------------------------------------------------------------------------
//
//    fun parseLuloConstraint(yamlValue : YamlValue) : Parser<LuloConstraint> = when (yamlValue)
//    {
//        is YamlDict -> parserApply2(::LuloConstraint,
//                                    parseConstraintData(yamlValue),
//                                    parseValueConstraint(yamlValue))
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Constraint List
//    // -----------------------------------------------------------------------------------------
//
//    fun parseConstraints(yamlValue : YamlValue?) : Parser<List<LuloConstraint>>
//    {
//        if (yamlValue == null)
//            return result(listOf())
//
//        when (yamlValue)
//        {
//            is YamlArray -> return yamlValue.map { parseLuloConstraint(it) }
//            else         -> return error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
//        }
//    }
//
//    // Constraint Names
//    // -----------------------------------------------------------------------------------------
//
//    fun parseConstraintNames(yamlValue : YamlValue?) : Parser<Set<ConstraintName>>
//    {
//        if (yamlValue == null)
//            return result(setOf())
//
//        when (yamlValue)
//        {
//            is YamlArray -> return yamlValue.mapSet { x -> parseConstraintName(x) }
//            else         -> return error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
//        }
//    }
//
//    // Constraint > Data
//    // -----------------------------------------------------------------------------------------
//
//    fun parseConstraintData(yamlValue : YamlValue) : Parser<ConstraintData> = when (yamlValue)
//    {
//        is YamlDict -> parserApply2(::ConstraintData,
//                                    yamlValue.at("name") ap { parseConstraintName(it) },
//                                    yamlValue.maybeAt("description") ap { parseConstraintDescription(it) } )
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Constraint > Data > Name
//    // -----------------------------------------------------------------------------------------
//
//    fun parseConstraintName(yamlValue : YamlValue) : Parser<ConstraintName> = when (yamlValue)
//    {
//        is YamlText -> result(ConstraintName(yamlValue.text))
//        else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//    }
//
//    // Constraint > Data > Description
//    // -----------------------------------------------------------------------------------------
//
//    fun parseConstraintDescription(yamlValue : YamlValue?) : Parser<ConstraintDescription?>
//    {
//        if (yamlValue == null)
//            return result(null)
//
//        when (yamlValue)
//        {
//            is YamlText -> return result(ConstraintDescription(yamlValue.text))
//            else        -> return error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue)))
//        }
//    }
//
//    // VALUE CONSTRAINT
//    // -----------------------------------------------------------------------------------------
//
//    // Value Constraint
//    // -----------------------------------------------------------------------------------------
//
//    fun parseValueConstraint(yamlValue : YamlValue) : Parser<ValueConstraint> = when (yamlValue)
//    {
//        is YamlDict -> yamlValue.text("type") ap {
//            when (it) {
//                "string_one_of"       -> yamlValue.at("parameters") ap { x -> parseStringOneOfConstraint(x) }
//                "number_greater_than" -> yamlValue.at("parameters") ap { x -> parseNumGreaterThanConstraint(x) }
//                else                  -> error(UnexpectedStringValue(it))
//            }
//        }
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//
//    // Value Constraint > String One Of
//    // -----------------------------------------------------------------------------------------
//
//    fun parseStringOneOfConstraint(yamlValue: YamlValue) : Parser<ValueConstraint> = when (yamlValue)
//    {
//        is YamlDict -> parserApply(::StringOneOf, yamlValue.stringSet("set"))
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//    // Value Constraint > Num Greater Than
//    // -----------------------------------------------------------------------------------------
//
//    fun parseNumGreaterThanConstraint(yamlValue: YamlValue) : Parser<ValueConstraint> = when (yamlValue)
//    {
//        is YamlDict -> parserApply(::NumGreaterThan, yamlValue.float("greater_than"))
//        else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue)))
//    }
//
//}
//




