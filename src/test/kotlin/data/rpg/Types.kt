
package data.rpg


import effect.*
import lulo.document.*
import lulo.value.UnexpectedType
import lulo.value.UnknownCase
import lulo.value.ValueError
import lulo.value.ValueParser



data class Character(val name : String,
                     val race : String,
                     val _class : Class,
                     val inventory : List<Item>) : ToDocument
{

    // CONSTRUCTORS

    companion object Factory
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Character> = when (doc)
        {
            is DocDict -> effApply(::Character,
                                   doc.text("name"),
                                   doc.text("race"),
                                   doc.at("class") ap { Class.fromDocument(it) },
                                   doc.list("inventory") ap { it.map { doc -> Item.fromDocument(doc) } })
            else        -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))
        }
    }

    // TO DOCUMENT

    override fun toDocument() : SchemaDoc = DocDict(
        mapOf(
            "name" to DocText(this.name),
            "race" to DocText(this.race),
            "class" to this._class.toDocument(),
            "inventory" to DocList(this.inventory.map { it.toDocument() })
        ))
}


data class Class(val name : String,
                 val usesMagic: Boolean,
                 val healthBonus : Maybe<Int>) : ToDocument
{

    // CONSTRUCTORS

    companion object Factory
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Class> = when (doc)
        {
            is DocDict -> effApply(::Class,
                                   doc.text("name"),
                                   doc.boolean("uses_magic"),
                                   doc.maybeInt("health_bonus"))
            else        -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))
        }
    }

    // TO DOCUMENT

    override fun toDocument() : SchemaDoc =
        DocDict(
            mapOf(
                "name" to DocText(this.name),
                "uses_magic" to DocBoolean(this.usesMagic)
            ))
        .maybeMerge(this.healthBonus.ap {
            Just(Pair("health_bonus", DocNumber(it.toDouble()))) })
}


sealed class Item : ToDocument
{

    // CONSTRUCTORS

    companion object Factory
    {
        fun fromDocument(doc : SchemaDoc) : ValueParser<Item> = when (doc)
        {
            is DocDict -> when (doc.case())
            {
                "weapon" -> Weapon.fromDocument(doc)
                "potion" -> Potion.fromDocument(doc)
                else     -> effError<ValueError,Item>(UnknownCase(doc.case(), doc.path))
            }
            else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))
        }
    }

    // WEAPON
    // -----------------------------------------------------------------------------------------

    data class Weapon(val name : String, val damage : Int) : Item()
    {

        // CONSTRUCTORS

        companion object Factory
        {
            fun fromDocument(doc : SchemaDoc) : ValueParser<Item> = when (doc)
            {
                is DocDict -> effApply(::Weapon, doc.text("name"), doc.int("damage"))
                else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))
            }
        }

        // TO DOCUMENT

        override fun toDocument() : SchemaDoc = DocDict(
            mapOf(
                "name" to DocText(this.name),
                "damage" to DocNumber(this.damage.toDouble())
            )
        )

    }


    // POTION
    // -----------------------------------------------------------------------------------------

    data class Potion(val name : String, val price : Double) : Item()
    {

        // CONSTRUCTORS

        companion object Factory
        {
            fun fromDocument(doc : SchemaDoc) : ValueParser<Item> = when (doc)
            {
                is DocDict -> effApply(::Potion, doc.text("name"), doc.double("price"))
                else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))
            }
        }

        // TO DOCUMENT

        override fun toDocument() : SchemaDoc = DocDict(
            mapOf(
                "name" to DocText(this.name),
                "price" to DocNumber(this.price)
            )
        )

    }

}

