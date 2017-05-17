
package data


import lulo.*



/**
 * Example Yaml File and Spec for an RPG.
 */
object RPG
{

    // YAML
    // ---------------------------------------------------------------------------------------------

    val yamlSpec =
        """
        version: '1.0'
        name: RPG
        authors:
         - 'Bob Smith'
        description:
          overview_md: overview
        types:
        - name: character
          label: Character
          description: A character
          type: product
          fields:
          - name: name
            type: string_utf8
            presence: required
            description: The character's name.
          - name: religion
            type: string_utf8
            presence: optional
            description: The character's religion.
          - name: inventory
            type: list
            of: item
            presence: required
            description: The inventory.
        - name: item
          label: Item
          description: An item.
          type: sum
          cases:
          - name: weapon
            type: item_weapon
            description: A weapon.
          - name: potion
            type: item_potion
            description: A potion.
        - name: item_weapon
          label: Weapon
          description: A weapon.
          type: product
          fields:
          - name: name
            type: string_utf8
            presence: required
            description: The weapon name.
          - name: damage
            type: integer
            presence: required
            description: The weapon damage.
            constraints:
            - positive_integer
        - name: item_potion
          label: Potion
          description: A potion.
          type: product
          fields:
          - name: name
            type: string_utf8
            presence: required
          - name: price
            type: number
            presence: required
        """


    // SPEC
    // ---------------------------------------------------------------------------------------------

    val spec1 =
        Spec(
            SpecVersion("1.0"),
            listOf(SpecAuthor("Bob Smith")),
            SpecDescription("overview"),
            listOf(
                LuloType(
                    TypeData("character", "Character", "A character", null),
                    Product(
                        listOf(
                            Field(FieldName("name"), FieldPresence.REQUIRED,
                                  FieldDescription("The character's name."),
                                  Prim(PrimValueType.STRING_UTF8), setOf(), null)
                        ,   Field(FieldName("religion"), FieldPresence.OPTIONAL,
                                        FieldDescription("The character's religion."),
                                        Prim(PrimValueType.STRING_UTF8), setOf(), null)
                        ,   Field(FieldName("inventory"), FieldPresence.REQUIRED,
                                FieldDescription("The inventory."),
                                CustomList(CustomValueType("item")), setOf(), null)
                        )
                    )
                )
            ,   LuloType(
                    TypeData("item", "Item", "An item.", null),
                    Sum(
                        listOf(
                            Case(CaseName("weapon"), CaseDescription("A weapon."),
                                 Custom(CustomValueType("item_weapon")))
                        ,   Case(CaseName("potion"), CaseDescription("A potion."),
                                 Custom(CustomValueType("item_potion")))
                        )
                    )
                )
            ,   LuloType(
                    TypeData("item_weapon", "Weapon", "A weapon.", null),
                    Product(
                        listOf(
                            Field(FieldName("name"), FieldPresence.REQUIRED,
                                  FieldDescription("The weapon name."),
                                  Prim(PrimValueType.STRING_UTF8), setOf(), null)
                        ,   Field(FieldName("damage"), FieldPresence.REQUIRED,
                                FieldDescription("The weapon damage."),
                                Prim(PrimValueType.INTEGER), setOf(ConstraintName("positive_integer")), null)
                        )
                    )
                )
            ,   LuloType(
                    TypeData("item_potion", "Potion", "A potion.", null),
                    Product(
                        listOf(
                            Field(FieldName("name"), FieldPresence.REQUIRED,
                                  null,
                                  Prim(PrimValueType.STRING_UTF8), setOf(), null)
                        ,   Field(FieldName("price"), FieldPresence.REQUIRED,
                                  null,
                                  Prim(PrimValueType.NUMBER), setOf(), null)
                        )
                    )
                )
            ),
            listOf()
        )

}

