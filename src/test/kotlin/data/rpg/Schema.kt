
package data.rpg


import effect.Just
import effect.Nothing
import lulo.schema.*



// CORRECT
// ---------------------------------------------------------------------------------------------

val rpgSchemaYaml =
    """
    version: '1.0'
    metadata:
        name: RPG
        authors:
         - name: 'Bob Smith'
    description:
      overview: overview
    root_type: character
    types:
    - type: product_type
      product_type:
        name: character
        label: Character
        description: A character
        fields:
        - name: name
          type:
            type: prim_type
            prim_type: string
          presence: required
          description: The character's name.
        - name: race
          type:
            type: custom_type
            custom_type: race
          presence: required
          description: The character's race.
        - name: class
          type:
            type: custom_type
            custom_type: class
          presence: required
          description: The character's class.
        - name: inventory
          type:
            type: custom_coll_type
            custom_coll_type: item
          presence: required
          description: The inventory.
    - type: primitive_type
      primitive_type:
        name: race
        label: Race
        base_type: string
        constraints:
        - race_set
    - type: product_type
      product_type:
        name: class
        label: Class
        fields:
        - name: name
          type:
            type: prim_type
            prim_type: string
          presence: required
        - name: uses_magic
          type:
            type: prim_type
            prim_type: boolean
          presence: required
        - name: health_bonus
          type:
            type: prim_type
            prim_type: number
          presence: optional
    - type: sum_type
      sum_type:
        name: item
        label: Item
        description: An item.
        cases:
        - type: weapon
          description: A weapon.
        - type: potion
          description: A potion.
    - type: product_type
      product_type:
        name: weapon
        label: Weapon
        description: A weapon.
        fields:
        - name: name
          type:
            type: prim_type
            prim_type: string
          presence: required
          description: The weapon name.
        - name: damage
          type:
            type: prim_type
            prim_type: number
          presence: required
          description: The weapon damage.
          constraints:
          - positive_integer
    - type: product_type
      product_type:
        name: potion
        label: Potion
        description: A potion.
        type: product
        fields:
        - name: name
          type:
            type: prim_type
            prim_type: string
          presence: required
        - name: price
          type:
            type: prim_type
            prim_type: number
          presence: required
    constraints:
    - type: constraint_string_one_of
      constraint_string_one_of:
        name: race_set
        description: Possible races.
        set:
        - human
        - orc
        - elf
        - dwarf
    """

val rpgSchemaObject =
    Schema(
        SchemaVersion("1.0"),
        SchemaMetadata(
            SchemaName("RPG"),
            listOf(SchemaAuthor("Bob Smith"))
        ),
        Just(SchemaDescription(Just("overview"))),
        TypeName("character"),
        listOf(
            Product(
                TypeName("character"),
                TypeLabel("Character"),
                Just(TypeDescription("A character")),
                Nothing(),
                setOf(),
                listOf(
                    Field(FieldName("name"),
                          FieldPresence.Required,
                          Just(FieldDescription("The character's name.")),
                          Prim(PrimValueType.String),
                          Nothing()),
                    Field(FieldName("race"),
                          FieldPresence.Required,
                          Just(FieldDescription("The character's race.")),
                          Custom(TypeName("race")),
                          Nothing()),
                    Field(FieldName("class"),
                          FieldPresence.Required,
                          Just(FieldDescription("The character's class.")),
                          Custom(TypeName("class")),
                          Nothing()),
                    Field(FieldName("inventory"),
                          FieldPresence.Required,
                          Just(FieldDescription("The inventory.")),
                          CustomList(TypeName("item")),
                          Nothing())
                )
            ),
            Primitive(
                TypeName("race"),
                TypeLabel("Race"),
                Nothing(),
                Nothing(),
                setOf(ConstraintName("race_set")),
                TypeName("string")
            ),
            Product(
                TypeName("class"),
                TypeLabel("Class"),
                Nothing(),
                Nothing(),
                setOf(),
                listOf(
                    Field(FieldName("name"),
                          FieldPresence.Required,
                          Nothing(),
                          Prim(PrimValueType.String),
                          Nothing()),
                    Field(FieldName("uses_magic"),
                          FieldPresence.Required,
                          Nothing(),
                          Prim(PrimValueType.Boolean),
                          Nothing()),
                    Field(FieldName("health_bonus"),
                          FieldPresence.Optional,
                          Nothing(),
                          Prim(PrimValueType.Number),
                          Nothing())
                )
            ),
            Sum(
                TypeName("item"),
                TypeLabel("Item"),
                Just(TypeDescription("An item.")),
                Nothing(),
                setOf(),
                listOf(
                    Case(TypeName("weapon"),
                         Just(CaseDescription("A weapon."))),
                    Case(TypeName("potion"),
                         Just(CaseDescription("A potion.")))
                )
            ),
            Product(
                TypeName("weapon"),
                TypeLabel("Weapon"),
                Just(TypeDescription("A weapon.")),
                Nothing(),
                setOf(),
                listOf(
                    Field(FieldName("name"),
                          FieldPresence.Required,
                          Just(FieldDescription("The weapon name.")),
                          Prim(PrimValueType.String),
                          Nothing()),
                    Field(FieldName("damage"),
                          FieldPresence.Required,
                          Just(FieldDescription("The weapon damage.")),
                          Prim(PrimValueType.Number),
                          Nothing())
                )
            ),
            Product(
                TypeName("potion"),
                TypeLabel("Potion"),
                Just(TypeDescription("A potion.")),
                Nothing(),
                setOf(),
                listOf(
                    Field(FieldName("name"),
                          FieldPresence.Required,
                          Nothing(),
                          Prim(PrimValueType.String),
                          Nothing()),
                    Field(FieldName("price"),
                          FieldPresence.Required,
                          Nothing(),
                          Prim(PrimValueType.Number),
                          Nothing())
                )
            )
        ),
        listOf(
            StringOneOf(
                ConstraintName("race_set"),
                ConstraintDescription("Possible races."),
                setOf(
                    "human",
                    "orc",
                    "elf",
                    "dwarf"
                )
            )
        )
    )


