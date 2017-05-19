
package data


import effect.Err
import effect.effApply2
import effect.effApply3
import lulo.*
import lulo.document.*
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
         - 'Bob Smith'
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
            type: string_utf8
            presence: required
            description: The character's name.
          - name: class
            type: class
            presence: required
            description: The character's class.
          - name: inventory
            type: list
            of: item
            presence: required
            description: The inventory.
        - name: class
          label: Class
          type: product
          fields:
          - name: name
            type: string_utf8
            presence: required
          - name: uses_magic
            type: boolean
            presence: required
          - name: health_bonus
            type: integer
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
            type: string_utf8
            presence: required
            description: The weapon name.
          - name: damage
            type: integer
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
            type: string_utf8
            presence: required
          - name: price
            type: number
            presence: required
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
                    TypeData(TypeName("character"), "Character", "A character", null),
                    Product(
                        listOf(
                            Field(FieldName("name"), FieldPresence.REQUIRED,
                                  FieldDescription("The character's name."),
                                  Prim(PrimValueType.STRING_UTF8), setOf(), null)
                        ,   Field(FieldName("class"), FieldPresence.REQUIRED,
                                        FieldDescription("The character's class."),
                                        Custom(CustomValueType("class")), setOf(), null)
                        ,   Field(FieldName("inventory"), FieldPresence.REQUIRED,
                                FieldDescription("The inventory."),
                                CustomList(CustomValueType("item")), setOf(), null)
                        )
                    )
                )
            ,   LuloType(
                    TypeData(TypeName("class"), "Class", null, null),
                    Product(
                        listOf(
                            Field(FieldName("name"), FieldPresence.REQUIRED,
                                  null,
                                  Prim(PrimValueType.STRING_UTF8), setOf(), null)
                        ,   Field(FieldName("uses_magic"), FieldPresence.REQUIRED,
                                  null,
                                  Prim(PrimValueType.BOOLEAN), setOf(), null)
                        ,   Field(FieldName("health_bonus"), FieldPresence.OPTIONAL,
                                  null,
                                  Prim(PrimValueType.INTEGER), setOf(), null)
                        )
                    )
                )
            ,   LuloType(
                    TypeData(TypeName("item"), "Item", "An item.", null),
                    Sum(
                        listOf(
                            Case(CaseDescription("A weapon."),
                                 Custom(CustomValueType("weapon")))
                        ,   Case(CaseDescription("A potion."),
                                 Custom(CustomValueType("potion")))
                        )
                    )
                )
            ,   LuloType(
                    TypeData(TypeName("weapon"), "Weapon", "A weapon.", null),
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
                    TypeData(TypeName("potion"), "Potion", "A potion.", null),
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


    // DOCUMENTS
    // ---------------------------------------------------------------------------------------------

    // Documents > Sila
    // ---------------------------------------------------------------------------------------------

    val silaYaml =
        """
        name:  Sila
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
                                "damage" to DocInteger(10, DocPath(listOf(DocKeyNode("inventory"),
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
            Class("Wizard", true, null),
            listOf(
                Item.Weapon("Staff", 10)
            ,   Item.Potion("Healing", 7.5)
            )
        )


    // TYPES
    // ---------------------------------------------------------------------------------------------

    data class Character(val name : String,
                         val _class : Class,
                         val inventory : List<Item>)
    {

        companion object Factory
        {
            fun fromDocument(doc : SpecDoc) : ValueParser<Character> = when (doc)
            {
                is DocDict -> effApply3(::Character,
                                         doc.text("name"),
                                         doc.at("class") ap { Class.fromDocument(it) },
                                         doc.list("inventory") ap { it.map { doc -> Item.fromDocument(doc) } })
                else        -> Err(UnexpectedType(DocType.DICT, docType(doc)), doc.path)
            }
        }
    }


    data class Class(val name : String, val usesMagic: Boolean, val healthBonus : Long?)
    {

        companion object Factory
        {
            fun fromDocument(doc : SpecDoc) : ValueParser<Class> = when (doc)
            {
                is DocDict -> effApply3(::Class,
                                         doc.text("name"),
                                         doc.boolean("uses_magic"),
                                         doc.maybeInteger("health_bonus"))
                else        -> Err(UnexpectedType(DocType.DICT, docType(doc)), doc.path)
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
                    else     -> Err<ValueError,DocPath,RPG.Item>(UnknownCase(doc.case()), doc.path)
                }
                else       -> Err(UnexpectedType(DocType.DICT, docType(doc)), doc.path)
            }
        }


        data class Weapon(val name : String, val damage : Long) : Item()
        {
            companion object Factory
            {
                fun fromDocument(doc : SpecDoc) : ValueParser<Item> = when (doc)
                {
                    is DocDict -> effApply2(::Weapon, doc.text("name"), doc.integer("damage"))
                    else       -> Err(UnexpectedType(DocType.DICT, docType(doc)), doc.path)
                }
            }
        }


        data class Potion(val name : String, val price : Double) : Item()
        {
            companion object Factory
            {
                fun fromDocument(doc : SpecDoc) : ValueParser<Item> = when (doc)
                {
                    is DocDict -> effApply2(::Potion, doc.text("name"), doc.double("price"))
                    else       -> Err(UnexpectedType(DocType.DICT, docType(doc)), doc.path)
                }
            }
        }

    }


}


