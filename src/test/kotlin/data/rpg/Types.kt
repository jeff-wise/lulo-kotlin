
package data.rpg


import effect.Maybe
import effect.effApply
import effect.effError
import lulo.document.DocDict
import lulo.document.DocType
import lulo.document.SchemaDoc
import lulo.document.docType
import lulo.value.UnexpectedType
import lulo.value.UnknownCase
import lulo.value.ValueError
import lulo.value.ValueParser



data class Character(val name : String,
                     val race : String,
                     val _class : Class,
                     val inventory : List<Item>)
{

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
}


data class Class(val name : String, val usesMagic: Boolean, val healthBonus : Maybe<Int>)
{

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
}


sealed class Item
{
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


    data class Weapon(val name : String, val damage : Int) : Item()
    {
        companion object Factory
        {
            fun fromDocument(doc : SchemaDoc) : ValueParser<Item> = when (doc)
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
            fun fromDocument(doc : SchemaDoc) : ValueParser<Item> = when (doc)
            {
                is DocDict -> effApply(::Potion, doc.text("name"), doc.double("price"))
                else       -> effError(UnexpectedType(DocType.DICT, docType(doc), doc.path))
            }
        }
    }

}

