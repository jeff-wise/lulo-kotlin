
package data


import effect.effApply2
import lulo.*
import lulo.document.*
import lulo.value.UnexpectedType
import lulo.value.ValueParser
import lulo.value.valueError


/**
 * Example Yaml File and Spec for an RPG.
 */

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
        type: string_utf8
        presence: optional
        description: The character's class.
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

val spec1 =
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
                    ,   Field(FieldName("class"), FieldPresence.OPTIONAL,
                                    FieldDescription("The character's class."),
                                    Prim(PrimValueType.STRING_UTF8), setOf(), null)
                    ,   Field(FieldName("inventory"), FieldPresence.REQUIRED,
                            FieldDescription("The inventory."),
                            CustomList(CustomValueType("item")), setOf(), null)
                    )
                )
            )
        ,   LuloType(
                TypeData(TypeName("item"), "Item", "An item.", null),
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
                TypeData(TypeName("item_weapon"), "Weapon", "A weapon.", null),
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
                TypeData(TypeName("item_potion"), "Potion", "A potion.", null),
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
    class: Wizard
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


//    val silaValue =



// TYPES
// ---------------------------------------------------------------------------------------------

class Character(val name : String,
                val religion : String,
                val inventory : List<Item>)
{

    companion object Factory
    {
        fun fromDocument(doc : SpecDoc) : Character = when (doc)
        {
            is SpecDict -> Character(doc.text("name"),
                                     doc.text("religion"),
                                     doc.map("inventory", {Item.fromDocument(it)}))
            else        ->
        }
    }
}


sealed class Item
{
    companion object Factory
    {
        fun fromDocument(doc : Document) : Item = when (doc.case())
        {
            "weapon" -> Weapon.fromDocument(doc)
            "potion" -> Potion.fromDocument(doc)
            else     -> throw DocumentParseException(UnknownCase(doc.case()))
        }
    }


    data class Weapon(val name : String, val damage : Int) : Item()
    {
        companion object Factory
        {
            fun fromDocument(doc : Document) : Weapon =
                    Weapon(doc.text("name"), doc.integer("damage"))
        }
    }


    data class Potion(val name : String, val price : Double) : Item()
    {

        companion object Factory
        {
            fun fromDocument(doc : SpecDoc) : ValueParser<Potion> = when (doc)
            {
                is SpecDict -> effApply2(::Potion, doc.text("name"), doc.double("price"))
                else        -> valueError(UnexpectedType(DocType.DICT, docType(doc)))
            }
        }
    }

}


