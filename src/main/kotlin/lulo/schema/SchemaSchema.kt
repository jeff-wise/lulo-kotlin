
package lulo.schema


import effect.Nothing



/**
 * The Schema Schema.
 *
 * A schema is required to load documents that adhere to that schema.
 *
 *  __________
 *  |        |  <------ DOCUMENT 1
 *  |        |
 *  | SCHEMA |  <------ DOCUMENT 2
 *  |        |
 *  |________|  <------ DOCUMENT 3
 *
 *  But schemas themselves are documents. Therefore, in order to load schemas as documents, we
 *  need a schema schema. But the "schema schema" is itself a schema document. So we cannot load
 *  the "schema schema" without a "schema schema" to validate that the "schema schema" is actually
 *  a schema. We are stuck in a logical circle, we need to break it by hardcoding the Schema Schema
 *  into the program.
 *
 *  __________
 *  |        |  <------ SCHEMA 1
 *  | SCHEMA |  <------ SCHEMA 2
 *  | SCHEMA |  <------ SCHEMA 3
 *  |________|  <------ SCHEMA 4 (SCHEMA SCHEMA)
 *
 *      depends on itself....
 */



val types : List<SchemaType> = listOf(

    // Schema
    Product(TypeName("schema"),
            TypeLabel("The schema."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("version"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("schema_version")),
                      Nothing()),
                Field(FieldName("metadata"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("schema_metadata")),
                      Nothing()),
                Field(FieldName("description"),
                      FieldPresence.Optional,
                      Nothing(),
                      Custom(TypeName("schema_description")),
                      Nothing()),
                Field(FieldName("root_type"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("type_name")),
                      Nothing()),
                Field(FieldName("types"),
                      FieldPresence.Required,
                      Nothing(),
                      CustomList(TypeName("schema_type")),
                      Nothing()),
                Field(FieldName("constraints"),
                      FieldPresence.Required,
                      Nothing(),
                      CustomList(TypeName("constraint")),
                      Nothing())
            )),

    // Schema Version
    Primitive(TypeName("schema_version"),
              TypeLabel("The schema version."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Schema Name
    Primitive(TypeName("schema_name"),
              TypeLabel("The schema name."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Schema Author
    Product(TypeName("schema_author"),
            TypeLabel("The schema author."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("name"),
                      FieldPresence.Required,
                      Nothing(),
                      Prim(PrimValueType.String),
                      Nothing())
            )),

    // Schema Metadata
    Product(TypeName("schema_metadata"),
            TypeLabel("The schema metadata."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("name"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("schema_name")),
                      Nothing()),
                Field(FieldName("authors"),
                      FieldPresence.Required,
                      Nothing(),
                      CustomList(TypeName("schema_author")),
                      Nothing())
            )),

    // Schema Description
    Product(TypeName("schema_description"),
            TypeLabel("The schema description."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("overview"),
                      FieldPresence.Optional,
                      Nothing(),
                      Prim(PrimValueType.String),
                      Nothing())
            )),

    // Schema Type
    Sum(TypeName("schema_type"),
        TypeLabel("A schema type."),
        Nothing(),
        Nothing(),
        setOf(),
        listOf(
            Case(TypeName("product_type"),
                 Nothing()),
            Case(TypeName("sum_type"),
                    Nothing()),
            Case(TypeName("primitive_type"),
                    Nothing()),
            Case(TypeName("symbol_type"),
                    Nothing())
        )),

    // Schema Type: Product
    Product(TypeName("product_type"),
            TypeLabel("A schema type."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("name"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("type_name")),
                      Nothing()),
                Field(FieldName("label"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("type_label")),
                      Nothing()),
                Field(FieldName("description"),
                      FieldPresence.Optional,
                      Nothing(),
                      Custom(TypeName("type_description")),
                      Nothing()),
                Field(FieldName("group"),
                      FieldPresence.Optional,
                      Nothing(),
                      Custom(TypeName("type_group")),
                      Nothing()),
                Field(FieldName("constraints"),
                      FieldPresence.Optional,
                      Nothing(),
                      CustomList(TypeName("constraint_name")),
                      Nothing()),
                Field(FieldName("fields"),
                    FieldPresence.Required,
                    Nothing(),
                    CustomList(TypeName("field")),
                    Nothing())
            )),

    // Schema Type: Sum
    Product(TypeName("sum_type"),
            TypeLabel("A schema type."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("name"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("type_name")),
                      Nothing()),
                Field(FieldName("label"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("type_label")),
                      Nothing()),
                Field(FieldName("description"),
                      FieldPresence.Optional,
                      Nothing(),
                      Custom(TypeName("type_description")),
                      Nothing()),
                Field(FieldName("group"),
                      FieldPresence.Optional,
                      Nothing(),
                      Custom(TypeName("type_group")),
                      Nothing()),
                Field(FieldName("constraints"),
                      FieldPresence.Optional,
                      Nothing(),
                      CustomList(TypeName("constraint_name")),
                      Nothing()),
                Field(FieldName("cases"),
                    FieldPresence.Required,
                    Nothing(),
                    CustomList(TypeName("case")),
                    Nothing())
            )),

    // Schema Type: Primitive
    Product(TypeName("primitive_type"),
            TypeLabel("A primitive type."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("name"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("type_name")),
                      Nothing()),
                Field(FieldName("label"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("type_label")),
                      Nothing()),
                Field(FieldName("description"),
                      FieldPresence.Optional,
                      Nothing(),
                      Custom(TypeName("type_description")),
                      Nothing()),
                Field(FieldName("group"),
                      FieldPresence.Optional,
                      Nothing(),
                      Custom(TypeName("type_group")),
                      Nothing()),
                Field(FieldName("constraints"),
                      FieldPresence.Optional,
                      Nothing(),
                      CustomList(TypeName("constraint_name")),
                      Nothing()),
                Field(FieldName("base_type"),
                    FieldPresence.Required,
                    Nothing(),
                    Custom(TypeName("type_name")),
                    Nothing())
            )),

    // Schema Type: Symbol
    Product(TypeName("symbol_type"),
            TypeLabel("A symbol type."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("name"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("type_name")),
                      Nothing()),
                Field(FieldName("label"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("type_label")),
                      Nothing()),
                Field(FieldName("description"),
                      FieldPresence.Optional,
                      Nothing(),
                      Custom(TypeName("type_description")),
                      Nothing()),
                Field(FieldName("group"),
                      FieldPresence.Optional,
                      Nothing(),
                      Custom(TypeName("type_group")),
                      Nothing()),
                Field(FieldName("constraints"),
                      FieldPresence.Optional,
                      Nothing(),
                      CustomList(TypeName("constraint_name")),
                      Nothing()),
                Field(FieldName("base_type"),
                    FieldPresence.Required,
                    Nothing(),
                    Prim(PrimValueType.String),
                    Nothing())
            )),

    // Type Name
    Primitive(TypeName("type_name"),
              TypeLabel("A type name."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Type Label
    Primitive(TypeName("type_label"),
              TypeLabel("A type label."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Type Description
    Primitive(TypeName("type_description"),
              TypeLabel("A type description."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Type Group
    Primitive(TypeName("type_group"),
              TypeLabel("A type group."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Field
    Product(TypeName("field"),
            TypeLabel("A field in a product type."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("name"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("field_name")),
                      Nothing()),
                Field(FieldName("presence"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("field_presence")),
                      Nothing()),
                Field(FieldName("description"),
                      FieldPresence.Optional,
                      Nothing(),
                      Custom(TypeName("field_description")),
                      Nothing()),
                Field(FieldName("type"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("value_type")),
                      Nothing()),
                Field(FieldName("default_value"),
                    FieldPresence.Optional,
                    Nothing(),
                    Custom(TypeName("field_default_value")),
                    Nothing())
            )),

    // Field Name
    Primitive(TypeName("field_name"),
              TypeLabel("A field name."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Field Presence
    Primitive(TypeName("field_presence"),
              TypeLabel("The field presecene: required / optional."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Field Description
    Primitive(TypeName("field_description"),
              TypeLabel("The field description."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Field Default Value
    Primitive(TypeName("field_default_value"),
              TypeLabel("The field default value."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Case
    Product(TypeName("case"),
            TypeLabel("A case in a sum type."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("type"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("type_name")),
                      Nothing()),
                Field(FieldName("description"),
                    FieldPresence.Optional,
                    Nothing(),
                    Custom(TypeName("case_description")),
                    Nothing())
            )),

    // Case Description
    Primitive(TypeName("case_description"),
              TypeLabel("A case description."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Value Type
    Sum(TypeName("value_type"),
        TypeLabel("The type of a value."),
        Nothing(),
        Nothing(),
        setOf(),
        listOf(
            Case(TypeName("prim_type"),
                 Nothing()),
            Case(TypeName("prim_coll_type"),
                 Nothing()),
            Case(TypeName("custom_type"),
                 Nothing()),
            Case(TypeName("custom_coll_type"),
                 Nothing())
        )),

    // Value Type: Primitive
    Primitive(TypeName("prim_type"),
              TypeLabel("A primitive type."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("prim_value_type")),

    // Value Type: Primitive Collection
    Primitive(TypeName("prim_coll_type"),
              TypeLabel("A primitive collection type."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("prim_value_type")),

    // Value Type: Custom
    Primitive(TypeName("custom_type"),
              TypeLabel("A custom type."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("type_name")),

    // Value Type: Custom Collection
    Primitive(TypeName("custom_coll_type"),
              TypeLabel("A custom type."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("type_name")),

    // Primitive Value Type
    Primitive(TypeName("prim_value_type"),
              TypeLabel("A primitive value type."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Constraint
    Sum(TypeName("constraint"),
        TypeLabel("A constraint."),
        Nothing(),
        Nothing(),
        setOf(),
        listOf(
            Case(TypeName("constraint_string_one_of"),
                 Nothing()),
            Case(TypeName("constraint_num_greater_than"),
                 Nothing())
        )),

    // Constraint Name
    Primitive(TypeName("constraint_name"),
              TypeLabel("The constraint name."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Constraint Description
    Primitive(TypeName("constraint_description"),
              TypeLabel("The constraint description."),
              Nothing(),
              Nothing(),
              setOf(),
              TypeName("string")),

    // Constraint: String One Of
    Product(TypeName("constraint_string_one_of"),
            TypeLabel("String one of constraint."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("name"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("constraint_name")),
                      Nothing()),
                Field(FieldName("description"),
                      FieldPresence.Optional,
                      Nothing(),
                      Custom(TypeName("constraint_description")),
                      Nothing()),
                Field(FieldName("set"),
                      FieldPresence.Required,
                      Nothing(),
                      PrimList(PrimValueType.String),
                      Nothing())
            )),

    // Constraint: Num Greater Than
    Product(TypeName("constraint_num_greater_than"),
            TypeLabel("String one of constraint."),
            Nothing(),
            Nothing(),
            setOf(),
            listOf(
                Field(FieldName("name"),
                      FieldPresence.Required,
                      Nothing(),
                      Custom(TypeName("constraint_name")),
                      Nothing()),
                Field(FieldName("description"),
                      FieldPresence.Optional,
                      Nothing(),
                       Custom(TypeName("constraint_description")),
                      Nothing()),
                Field(FieldName("lower_bound"),
                      FieldPresence.Required,
                      Nothing(),
                      PrimList(PrimValueType.Number),
                      Nothing())
            ))
)


val schemaSchema = Schema(
                     SchemaVersion("1.0"),
                     SchemaMetadata(SchemaName("schema-schema"), listOf()),
                     Nothing(),
                     TypeName("schema"),
                     types,
                     listOf())


