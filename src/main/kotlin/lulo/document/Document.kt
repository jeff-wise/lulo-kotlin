
package lulo.document


import effect.*
import lulo.value.*
import lulo.value.UnexpectedType



// ---------------------------------------------------------------------------------------------
// DOCUMENT
// ---------------------------------------------------------------------------------------------

/**
 * Specification Document
 */
sealed class SpecDoc(open val path : DocPath,
                     open val cases : List<String>)
{

    abstract fun nextCase() : SpecDoc

    fun case() : String? =
        if (cases.isNotEmpty())
            cases.first()
        else
            null

}


// ---------------------------------------------------------------------------------------------
// Document > Dictionary
// ---------------------------------------------------------------------------------------------

data class DocDict(val fields : Map<String,SpecDoc>,
                   override val cases : List<String>,
                   override val path : DocPath) : SpecDoc(path, cases)
{

    constructor(fields : Map<String,SpecDoc>, path : DocPath) : this(fields, listOf(), path)


    override fun nextCase() : SpecDoc = DocDict(fields, cases.drop(1), path)



    // -----------------------------------------------------------------------------------------
    // API
    // -----------------------------------------------------------------------------------------

    // At
    // -----------------------------------------------------------------------------------------

    fun at(key : String) : ValueParser<SpecDoc>
    {
        val fieldDoc = fields[key]

        return if (fieldDoc == null)
            effError(MissingKey(key, path))
        else
            effValue(fieldDoc)
    }


    fun maybeAt(key : String) : ValueParser<Maybe<SpecDoc>>
    {
        val fieldDoc = fields[key]

        return if (fieldDoc == null)
            effValue(Nothing())
        else
            effValue(Just(fieldDoc))
    }


    // List
    // -----------------------------------------------------------------------------------------

    fun list(key : String) : ValueParser<DocList>
    {
        val fieldDoc = fields[key]

        return if (fieldDoc == null) {
            effError(MissingKey(key, path))
        }
        else {
            when (fieldDoc) {
                is DocList -> effValue(fieldDoc)
                else       -> effError<ValueError,DocList>(
                                UnexpectedType(DocType.LIST, docType(fieldDoc), path))
            }
        }
    }


    fun maybeList(key : String) : ValueParser<Maybe<DocList>>
    {
        val listParser = this.list(key)

        return when (listParser)
        {
            is Val -> effValue(Just(listParser.value))
            is Err -> effValue(Nothing())
        }
    }


    // Enum
    // -----------------------------------------------------------------------------------------

    inline fun <reified A : Enum<A>> enum(key : String) : ValueParser<A>
    {
        val fieldDoc = fields[key]

        if (fieldDoc == null)
        {
            return effError(MissingKey(key, path))
        }
        else
        {
            when (fieldDoc)
            {
                is DocText ->
                {
                    try {
                        return effValue(enumValueOf<A>(fieldDoc.text))
                    }
                    catch (e : Exception) {
                        return effError(UnexpectedValue(A::class.simpleName, fieldDoc.text, path))
                    }
                }
                else       -> return effError(UnexpectedType(DocType.TEXT, docType(fieldDoc), path))
            }
        }
    }


    inline fun <reified A : Enum<A>> maybeEnum(key : String) : ValueParser<Maybe<A>>
    {
        val enumParser = this.enum<A>(key)

        when (enumParser)
        {
            is Val -> return effValue(Just(enumParser.value))
            is Err -> return effValue(Nothing())
        }
    }


    // Text
    // -----------------------------------------------------------------------------------------

    fun text(key : String) : ValueParser<String>
    {
        val fieldDoc = fields[key]

        return if (fieldDoc == null) {
            effError(MissingKey(key, path))
        }
        else {
            when (fieldDoc) {
                is DocText -> effValue(fieldDoc.text)
                else       -> effError<ValueError,String>(
                                UnexpectedType(DocType.TEXT, docType(fieldDoc), path))
            }
        }
    }


    fun maybeText(key : String) : ValueParser<Maybe<String>>
    {
        val textParser = this.text(key)

        return when (textParser)
        {
            is Val -> effValue(Just(textParser.value))
            is Err -> effValue(Nothing())
        }
    }


    // Int
    // -----------------------------------------------------------------------------------------

    fun int(key : String) : ValueParser<Int>
    {
        val fieldDoc = fields[key]

        return if (fieldDoc == null) {
            return effError(MissingKey(key, path))
        }
        else {
            when (fieldDoc) {
                is DocNumber -> effValue(fieldDoc.number.toInt())
                else         -> effError<ValueError,Int>(
                                    UnexpectedType(DocType.NUMBER, docType(fieldDoc), path))
            }
        }
    }


    fun maybeInt(key : String) : ValueParser<Maybe<Int>>
    {
        val integerParser = this.int(key)

        return when (integerParser) {
            is Val -> effValue(Just(integerParser.value))
            is Err -> effValue(Nothing())
        }
    }


    // Long
    // -----------------------------------------------------------------------------------------

    fun long(key : String) : ValueParser<Long>
    {
        val fieldDoc = fields[key]

        return if (fieldDoc == null) {
            return effError(MissingKey(key, path))
        }
        else {
            when (fieldDoc) {
                is DocNumber -> effValue(fieldDoc.number.toLong())
                else         -> effError<ValueError,Long>(
                                    UnexpectedType(DocType.NUMBER, docType(fieldDoc), path))
            }
        }
    }


    fun maybeLong(key : String) : ValueParser<Maybe<Long>>
    {
        val longParser = this.long(key)

        return when (longParser) {
            is Val -> effValue(Just(longParser.value))
            is Err -> effValue(Nothing())
        }
    }


    // Float
    // -----------------------------------------------------------------------------------------

    fun float(key : String) : ValueParser<Float>
    {
        val fieldDoc   = fields[key]

        if (fieldDoc == null)
        {
            return effError(MissingKey(key, path))
        }
        else
        {
            when (fieldDoc)
            {
                is DocNumber -> return effValue(fieldDoc.number.toFloat())
                else         -> return effError(UnexpectedType(DocType.NUMBER, docType(fieldDoc), path))
            }
        }
    }


    fun maybeFloat(key : String) : ValueParser<Maybe<Float>>
    {
        val floatParser = this.float(key)

        return when (floatParser) {
            is Val -> effValue(Just(floatParser.value))
            is Err -> effValue(Nothing())
        }
    }


    // Double
    // -----------------------------------------------------------------------------------------

    fun double(key : String) : ValueParser<Double>
    {
        val fieldDoc   = fields[key]

        return if (fieldDoc == null) {
            effError(MissingKey(key, path))
        }
        else {
            when (fieldDoc) {
                is DocNumber -> effValue(fieldDoc.number)
                else         -> effError<ValueError,Double>(
                                    UnexpectedType(DocType.NUMBER, docType(fieldDoc), path))
            }
        }
    }


    fun maybeDouble(key : String) : ValueParser<Maybe<Double>>
    {
        val doubleParser = this.double(key)

        return when (doubleParser) {
            is Val -> effValue(Just(doubleParser.value))
            is Err -> effValue(Nothing())
        }
    }


    // Boolean
    // -----------------------------------------------------------------------------------------

    fun boolean(key : String) : ValueParser<Boolean>
    {
        val fieldDoc    = fields[key]

        return if (fieldDoc == null) {
            effError(MissingKey(key, path))
        }
        else {
            when (fieldDoc) {
                is DocBoolean -> effValue(fieldDoc.boolean)
                else          -> effError<ValueError,Boolean>(
                                     UnexpectedType(DocType.BOOLEAN, docType(fieldDoc), path))
            }
        }
    }


    fun maybeBoolean(key : String) : ValueParser<Maybe<Boolean>>
    {
        val booleanParser = this.boolean(key)

        when (booleanParser)
        {
            is Val -> return effValue(Just(booleanParser.value))
            is Err -> return effValue(Nothing())
        }
    }
}


// ---------------------------------------------------------------------------------------------
// Document > List
// ---------------------------------------------------------------------------------------------

data class DocList(val docs : List<SpecDoc>,
                   override val cases : List<String>,
                   override val path : DocPath) : SpecDoc(path, cases)
{

    constructor(docs : List<SpecDoc>, path : DocPath) : this(docs, listOf(), path)


    override fun nextCase() : SpecDoc = DocList(docs, cases.drop(1), path)


    fun <T> map(f : (SpecDoc) -> ValueParser<T>) : ValueParser<List<T>>
    {
        val results = mutableListOf<T>()


        docs.forEach { doc ->

            val valueParser = f(doc)

            when (valueParser) {
                is Val -> results.add(valueParser.value)
                is Err  -> {
                    return effError(valueParser.error)
                }
            }
        }

        return effValue(results)
    }


    fun <T> mapMut(f : (SpecDoc) -> ValueParser<T>) : ValueParser<MutableList<T>>
    {
        val results = mutableListOf<T>()


        docs.forEach { doc ->

            val valueParser = f(doc)

            when (valueParser) {
                is Val -> results.add(valueParser.value)
                is Err  -> {
                    return effError(valueParser.error)
                }
            }
        }

        return effValue(results)
    }


    fun <T> mapIndexed(f : (SpecDoc, Int) -> ValueParser<T>) : ValueParser<MutableList<T>>
    {
        val results = mutableListOf<T>()


        docs.forEachIndexed { index, doc ->

            val valueParser = f(doc, index)

            when (valueParser) {
                is Val -> results.add(valueParser.value)
                is Err  -> {
                    return effError(valueParser.error)
                }
            }
        }

        return effValue(results)
    }


    fun <T> mapIndexedMut(f : (SpecDoc, Int) -> ValueParser<T>) : ValueParser<MutableList<T>>
    {
        val results = mutableListOf<T>()


        docs.forEachIndexed { index, doc ->

            val valueParser = f(doc, index)

            when (valueParser) {
                is Val -> results.add(valueParser.value)
                is Err  -> {
                    return effError(valueParser.error)
                }
            }
        }

        return effValue(results)
    }


    fun <T> mapSet(f : (SpecDoc) -> ValueParser<T>) : ValueParser<Set<T>>
    {
        val results = mutableSetOf<T>()

        docs.forEach { doc ->

            val valueParser = f(doc)

            when (valueParser) {
                is Val -> results.add(valueParser.value)
                is Err  -> {
                    return effError(valueParser.error)
                }
            }
        }

        return effValue(results)
    }


    fun <T> mapSetMut(f : (SpecDoc) -> ValueParser<T>) : ValueParser<MutableSet<T>>
    {
        val results = mutableSetOf<T>()

        docs.forEach { doc ->

            val valueParser = f(doc)

            when (valueParser) {
                is Val -> results.add(valueParser.value)
                is Err  -> {
                    return effError(valueParser.error)
                }
            }
        }

        return effValue(results)
    }


    fun <T> mapHashSet(f : (SpecDoc) -> ValueParser<T>) : ValueParser<HashSet<T>>
    {
        val results = hashSetOf<T>()

        docs.forEach { doc ->

            val valueParser = f(doc)

            when (valueParser) {
                is Val -> results.add(valueParser.value)
                is Err  -> {
                    return effError(valueParser.error)
                }
            }
        }

        return effValue(results)
    }


    fun <T> mapArrayList(f : (SpecDoc) -> ValueParser<T>) : ValueParser<ArrayList<T>>
    {
        val results = arrayListOf<T>()

        docs.forEach { doc ->

            val valueParser = f(doc)

            when (valueParser) {
                is Val -> results.add(valueParser.value)
                is Err  -> {
                    return effError(valueParser.error)
                }
            }
        }

        return effValue(results)
    }


    fun stringList() : ValueParser<List<String>>
    {
        val strings = mutableListOf<String>()

        for (doc in docs)
        {
            when (doc)
            {
                is DocText -> strings.add(doc.text)
                else       -> return effError(UnexpectedType(DocType.TEXT, docType(doc), path))
            }
        }

        return effValue(strings)
    }


    inline fun <reified A : Enum<A>> enumList() : ValueParser<List<A>>
    {
        val enums = mutableListOf<A>()

        for (doc in docs)
        {
            when (doc)
            {
                is DocText ->
                {
                    try {
                        enums.add(enumValueOf<A>(doc.text))
                    }
                    catch (e : Exception) {
                        return effError(UnexpectedValue(A::class.simpleName, doc.text, path))
                    }
                }
                else       -> return effError(UnexpectedType(DocType.TEXT, docType(doc), path))
            }
        }

        return effValue(enums)
    }


}


// ---------------------------------------------------------------------------------------------
// Document > Text
// ---------------------------------------------------------------------------------------------

data class DocText(val text : String,
                   override val cases : List<String>,
                   override val path : DocPath) : SpecDoc(path, cases)
{
    constructor(text : String, path : DocPath) : this(text, listOf(), path)

    override fun nextCase() : SpecDoc = DocText(text, cases.drop(1), path)

}


// ---------------------------------------------------------------------------------------------
// Document > Number
// ---------------------------------------------------------------------------------------------

data class DocNumber(val number : Double,
                     override val cases : List<String>,
                     override val path : DocPath) : SpecDoc(path, cases)
{
    constructor(number : Double, path : DocPath) : this(number, listOf(), path)

    override fun nextCase() : SpecDoc = DocNumber(number, cases.drop(1), path)
}


// ---------------------------------------------------------------------------------------------
// Document > Boolean
// ---------------------------------------------------------------------------------------------

data class DocBoolean(val boolean : Boolean,
                      override val cases : List<String>,
                      override val path : DocPath) : SpecDoc(path, cases)
{
    constructor(boolean : Boolean, path : DocPath) : this(boolean, listOf(), path)

    override fun nextCase() : SpecDoc = DocBoolean(boolean, cases.drop(1), path)
}


// ---------------------------------------------------------------------------------------------
// DOCUMENT TYPE
// ---------------------------------------------------------------------------------------------

enum class DocType
{
    DICT,
    LIST,
    TEXT,
    NUMBER,
    BOOLEAN
}


fun docType(doc : SpecDoc) : DocType = when (doc)
{
    is DocDict    -> DocType.DICT
    is DocList    -> DocType.LIST
    is DocText    -> DocType.TEXT
    is DocNumber  -> DocType.NUMBER
    is DocBoolean -> DocType.BOOLEAN
}



sealed class DocLog : Monoid<DocLog>
{
    override fun mappend(otherLog: DocLog): DocLog = when (this)
    {
        is DocLogError -> this
        else ->           otherLog
    }
}

data class DocLogError(val path : DocPath) : DocLog()


class DocLogEmpty : DocLog()


data class DocPath(val nodes : List<DocNode>)
{

    infix fun withLocation(node : DocNode) : DocPath =
        DocPath(nodes.plus(node))

    override fun toString(): String
    {
        var pathString = ""

        for (node in this.nodes) {
            pathString += node.toString()
        }

        return pathString;
    }

}

sealed class DocNode


data class DocKeyNode(val key : String) : DocNode()
{
    override fun toString(): String = "." + key
}

data class DocIndexNode(val index : Int) : DocNode()
{
    override fun toString(): String
    {
        return "[$index]"
    }
}

object DocNullNode : DocNode()
{
    override fun toString(): String = ""
}


