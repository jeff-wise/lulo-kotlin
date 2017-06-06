
package data


import effect.*
import lulo.document.*
import lulo.spec.*
import lulo.value.*
import lulo.value.UnexpectedType



/**
 * Example Yaml File and Spec for an RPG.
 */
object RPG
{

    // SPECIFICATION
    // ---------------------------------------------------------------------------------------------

    // Specification > Yaml
    // ---------------------------------------------------------------------------------------------

    val yamlSpec =
        """
        version: '1.0'
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
          type: simple
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

    // Specification > Value
    // ---------------------------------------------------------------------------------------------

    val specification =
        Spec(
            SpecVersion("1.0"),
            listOf(SpecAuthor("Bob Smith")),
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
                    Simple("string")
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


    // DOCUMENTS
    // ---------------------------------------------------------------------------------------------

    // Documents > Sila
    // ---------------------------------------------------------------------------------------------

    val silaYaml =
        """
        name:  Sila
        race: elf
        class:
          name: Wizard
          uses_magic: true
        inventory:
        - type: weapon
          weapon:
            name: Staff
            damage: 10
        - type: potion
          potion:
            name: Healing
            price: 7.5
        """


    val silaDocument =
        DocDict(
            mapOf(
                "name" to DocText("Sila", DocPath(listOf(DocKeyNode("name")))),
                "race" to DocText("elf", DocPath(listOf(DocKeyNode("race")))),
                "class" to DocDict(
                    mapOf(
                        "name" to DocText("Wizard",
                                          DocPath(listOf(DocKeyNode("class"), DocKeyNode("name")))),
                        "uses_magic" to DocBoolean(true,
                                                   DocPath(listOf(DocKeyNode("class"), DocKeyNode("uses_magic"))))
                    ),
                    null,
                    DocPath(listOf(DocKeyNode("class")))
                ),
                "inventory" to DocList(
                    listOf(
                        DocDict(
                            mapOf(
                                "name" to DocText("Staff", DocPath(listOf(DocKeyNode("inventory"),
                                                                          DocIndexNode(0),
                                                                          DocKeyNode("name")))),
                                "damage" to DocNumber(10.0, DocPath(listOf(DocKeyNode("inventory"),
                                                                          DocIndexNode(0),
                                                                          DocKeyNode("damage"))))
                            ),
                            "weapon",
                            DocPath(listOf(DocKeyNode("inventory"), DocIndexNode(0)))
                        ),
                        DocDict(
                            mapOf(
                                "name" to DocText("Healing", DocPath(listOf(DocKeyNode("inventory"),
                                                                            DocIndexNode(1),
                                                                            DocKeyNode("name")))),
                                "price" to DocNumber(7.5, DocPath(listOf(DocKeyNode("inventory"),
                                                                         DocIndexNode(1),
                                                                         DocKeyNode("price"))))
                            ),
                            "potion",
                            DocPath(listOf(DocKeyNode("inventory"), DocIndexNode(1)))
                        )
                    ),
                    DocPath(listOf(DocKeyNode("inventory")))
                )
            ),
            null,
            DocPath(listOf())
        )


    val silaValue =
        Character(
            "Sila",
            "elf",
            Class("Wizard", true, Nothing()),
            listOf(
                Item.Weapon("Staff", 10)
            ,   Item.Potion("Healing", 7.5)
            )
        )


    // Documents > Sila Missing Name
    // ---------------------------------------------------------------------------------------------


    val silaMissingNameDocument =
        DocDict(
            mapOf(
                "name" to DocText("Sila", DocPath(listOf(DocKeyNode("name")))),
                "race" to DocText("elf", DocPath(listOf(DocKeyNode("race")))),
                "class" to DocDict(
                    mapOf(
                        "name" to DocText("Wizard",
                                          DocPath(listOf(DocKeyNode("class"), DocKeyNode("name")))),
                        "uses_magic" to DocBoolean(true,
                                                   DocPath(listOf(DocKeyNode("class"), DocKeyNode("uses_magic"))))
                    ),
                    null,
                    DocPath(listOf(DocKeyNode("class")))
                ),
                "inventory" to DocList(
                    listOf(
                        DocDict(
                            mapOf(
                            // This field is MISSING
                            //  "name" to DocText("Staff", DocPath(listOf(DocKeyNode("inventory"),
                            //                                            DocIndexNode(0),
                            //                                            DocKeyNode("name")))),
                                "damage" to DocNumber(10.0, DocPath(listOf(DocKeyNode("inventory"),
                                                                           DocIndexNode(0),
                                                                           DocKeyNode("damage"))))
                            ),
                            "weapon",
                            DocPath(listOf(DocKeyNode("inventory"), DocIndexNode(0)))
                        ),
                        DocDict(
                            mapOf(
                                "name" to DocText("Healing", DocPath(listOf(DocKeyNode("inventory"),
                                                                            DocIndexNode(1),
                                                                            DocKeyNode("name")))),
                                "price" to DocNumber(7.5, DocPath(listOf(DocKeyNode("inventory"),
                                                                         DocIndexNode(1),
                                                                         DocKeyNode("price"))))
                            ),
                            "potion",
                            DocPath(listOf(DocKeyNode("inventory"), DocIndexNode(1)))
                        )
                    ),
                    DocPath(listOf(DocKeyNode("inventory")))
                )
            ),
            null,
            DocPath(listOf())
        )


    // TYPES
    // ---------------------------------------------------------------------------------------------

    data class Character(val name : String,
                         val race : String,
                         val _class : Class,
                         val inventory : List<Item>)
    {

        companion object Factory
        {
            fun fromDocument(doc : SpecDoc) : ValueParser<Character> = when (doc)
            {
                is DocDict -> effApply(::Character,
                                       doc.text("name"),
                                       doc.text("race"),
                                       doc.at("class") ap { Class.fromDocument(it) },
                                       doc.list("inventory") ap { it.map { doc -> Item.fromDocument(doc) } })
                else        -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))
            }
        }
    }


    data class Class(val name : String, val usesMagic: Boolean, val healthBonus : Maybe<Int>)
    {

        companion object Factory
        {
            fun fromDocument(doc : SpecDoc) : ValueParser<Class> = when (doc)
            {
                is DocDict -> effApply(::Class,
                                       doc.text("name"),
                                       doc.boolean("uses_magic"),
                                       doc.maybeInt("health_bonus"))
                else        -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))
            }
        }
    }


    sealed class Item
    {
        companion object Factory
        {
            fun fromDocument(doc : SpecDoc) : ValueParser<Item> = when (doc)
            {
                is DocDict -> when (doc.case())
                {
                    "weapon" -> Weapon.fromDocument(doc)
                    "potion" -> Potion.fromDocument(doc)
                    else     -> effError<ValueError,RPG.Item>(UnknownCase(doc.case(), doc.path))
                }
                else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))
            }
        }


        data class Weapon(val name : String, val damage : Int) : Item()
        {
            companion object Factory
            {
                fun fromDocument(doc : SpecDoc) : ValueParser<Item> = when (doc)
                {
                    is DocDict -> effApply(::Weapon, doc.text("name"), doc.int("damage"))
                    else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))
                }
            }
        }


        data class Potion(val name : String, val price : Double) : Item()
        {
            companion object Factory
            {
                fun fromDocument(doc : SpecDoc) : ValueParser<Item> = when (doc)
                {
                    is DocDict -> effApply(::Potion, doc.text("name"), doc.double("price"))
                    else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))
                }
            }
        }

    }


}


