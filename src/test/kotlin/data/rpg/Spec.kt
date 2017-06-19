
package data.rpg


import lulo.spec.*



// CORRECT
// ---------------------------------------------------------------------------------------------

val rpgSpecYaml =
    """
    version: '1.0'
    metadata:
        name: RPG
        authors:
         - name: 'Bob Smith'
    description:
      overview_md: overview
    root_type: character
    types:
    - name: character
      label: Character
      description: A character
      type: product
      fields:
      - name: name
        type: string
        presence: required
        description: The character's name.
      - name: race
        type: race
        presence: required
        description: The character's race.
      - name: class
        type: class
        presence: required
        description: The character's class.
      - name: inventory
        type: list
        of: item
        presence: required
        description: The inventory.
    - name: race
      label: Race
      type: primitive
      base_type: string
      constraints:
      - race_set
    - name: class
      label: Class
      type: product
      fields:
      - name: name
        type: string
        presence: required
      - name: uses_magic
        type: boolean
        presence: required
      - name: health_bonus
        type: number
        presence: optional
    - name: item
      label: Item
      description: An item.
      type: sum
      cases:
      - type: weapon
        description: A weapon.
      - type: potion
        description: A potion.
    - name: weapon
      label: Weapon
      description: A weapon.
      type: product
      fields:
      - name: name
        type: string
        presence: required
        description: The weapon name.
      - name: damage
        type: number
        presence: required
        description: The weapon damage.
        constraints:
        - positive_integer
    - name: potion
      label: Potion
      description: A potion.
      type: product
      fields:
      - name: name
        type: string
        presence: required
      - name: price
        type: number
        presence: required
    constraints:
    - name: race_set
      type: string_one_of
      parameters:
        set:
        - human
        - orc
        - elf
        - dwarf
    """

val rpgSpecObject =
    Spec(
        SpecVersion("1.0"),
        SpecMetadata(
            SpecName("RPG"),
            listOf(SpecAuthor("Bob Smith"))
        ),
        SpecDescription("overview"),
        TypeName("character"),
        listOf(
            LuloType(
                TypeData(TypeName("character"), "Character", "A character", null, setOf()),
                Product(
                    listOf(
                        Field(FieldName("name"), FieldPresence.REQUIRED,
                              FieldDescription("The character's name."),
                              Prim(PrimValueType.STRING), null)
                    ,   Field(FieldName("race"), FieldPresence.REQUIRED,
                            FieldDescription("The character's race."),
                            Custom(TypeName("race")), null)
                    ,   Field(FieldName("class"), FieldPresence.REQUIRED,
                                    FieldDescription("The character's class."),
                                    Custom(TypeName("class")), null)
                    ,   Field(FieldName("inventory"), FieldPresence.REQUIRED,
                            FieldDescription("The inventory."),
                            CustomList(TypeName("item")), null)
                    )
                )
            )
        ,   LuloType(
                TypeData(TypeName("race"), "Race", null, null, setOf(ConstraintName("race_set"))),
                Primitive("string")
            )
        ,   LuloType(
                TypeData(TypeName("class"), "Class", null, null, setOf()),
                Product(
                    listOf(
                        Field(FieldName("name"), FieldPresence.REQUIRED,
                              null,
                              Prim(PrimValueType.STRING), null)
                    ,   Field(FieldName("uses_magic"), FieldPresence.REQUIRED,
                              null,
                              Prim(PrimValueType.BOOLEAN), null)
                    ,   Field(FieldName("health_bonus"), FieldPresence.OPTIONAL,
                              null,
                              Prim(PrimValueType.NUMBER), null)
                    )
                )
            )
        ,   LuloType(
                TypeData(TypeName("item"), "Item", "An item.", null, setOf()),
                Sum(
                    listOf(
                        Case(Custom(TypeName("weapon")),
                             CaseDescription("A weapon."))
                    ,   Case(Custom(TypeName("potion")),
                             CaseDescription("A potion."))
                    )
                )
            )
        ,   LuloType(
                TypeData(TypeName("weapon"), "Weapon", "A weapon.", null, setOf()),
                Product(
                    listOf(
                        Field(FieldName("name"), FieldPresence.REQUIRED,
                              FieldDescription("The weapon name."),
                              Prim(PrimValueType.STRING), null)
                    ,   Field(FieldName("damage"), FieldPresence.REQUIRED,
                            FieldDescription("The weapon damage."),
                            Prim(PrimValueType.NUMBER), null)
                    )
                )
            )
        ,   LuloType(
                TypeData(TypeName("potion"), "Potion", "A potion.", null, setOf()),
                Product(
                    listOf(
                        Field(FieldName("name"), FieldPresence.REQUIRED,
                              null,
                              Prim(PrimValueType.STRING), null)
                    ,   Field(FieldName("price"), FieldPresence.REQUIRED,
                              null,
                              Prim(PrimValueType.NUMBER), null)
                    )
                )
            )
        ),
        listOf(
            LuloConstraint(
                ConstraintData(ConstraintName("race_set"), null),
                StringOneOf(
                    setOf(
                        "human",
                        "orc",
                        "elf",
                        "dwarf"
                    )
                )
            )
        )
    )


