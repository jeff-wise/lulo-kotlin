
package lulo.document


import com.kispoko.culebra.*
import effect.*
import lulo.spec.PrimValueType
import lulo.spec.Sum
import lulo.spec.TypeName
import lulo.value.*
import lulo.value.UnexpectedType



/**
 * Specification Document
 */
sealed class SpecDoc(open val path : DocPath,
                     open val case : String?)


data class DocDict(val fields : Map<String,SpecDoc>,
                   override val case : String?,
                   override val path : DocPath) : SpecDoc(path, case)
{

    constructor(fields : Map<String,SpecDoc>, path : DocPath) : this(fields, null, path)

    fun case() : String?
    {
        return case
    }


    fun at(key : String) : ValueParser<SpecDoc>
    {
        val fieldDoc = fields[key]

        if (fieldDoc == null)
        {
            return effError(MissingKey(key, path))
        }
        else
        {
            return effValue(fieldDoc)
        }
    }


    fun maybeAt(key : String) : ValueParser<Maybe<SpecDoc>>
    {
        val fieldDoc = fields[key]

        if (fieldDoc == null)
        {
            return effValue(Nothing())
        }
        else
        {
            return effValue(Just(fieldDoc))
        }
    }


    fun list(key : String) : ValueParser<DocList>
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
                is DocList -> return effValue(fieldDoc)
                else       -> return effError(UnexpectedType(DocType.LIST, docType(fieldDoc), path))
            }
        }
    }


    fun maybeList(key : String) : ValueParser<Maybe<DocList>>
    {
        val listParser = this.list(key)

        when (listParser)
        {
            is Val -> return effValue(Just(listParser.value))
            is Err -> return effValue(Nothing())
        }
    }


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


    fun text(key : String) : ValueParser<String>
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
                is DocText -> return effValue(fieldDoc.text)
                else       -> return effError(UnexpectedType(DocType.TEXT, docType(fieldDoc), path))
            }
        }
    }


    fun maybeText(key : String) : ValueParser<Maybe<String>>
    {
        val textParser = this.text(key)

        when (textParser)
        {
            is Val -> return effValue(Just(textParser.value))
            is Err -> return effValue(Nothing())
        }

    }


    fun int(key : String) : ValueParser<Int>
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
                is DocNumber -> return effValue(fieldDoc.number.toInt())
                else         -> return effError(UnexpectedType(DocType.NUMBER, docType(fieldDoc), path))
            }
        }
    }


    fun long(key : String) : ValueParser<Long>
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
                is DocNumber -> return effValue(fieldDoc.number.toLong())
                else         -> return effError(UnexpectedType(DocType.NUMBER, docType(fieldDoc), path))
            }
        }
    }


    fun maybeInt(key : String) : ValueParser<Maybe<Int>>
    {
        val integerParser = this.int(key)

        when (integerParser)
        {
            is Val -> return effValue(Just(integerParser.value))
            is Err -> return effValue(Nothing())
        }
    }


    fun double(key : String) : ValueParser<Double>
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
                is DocNumber -> return effValue(fieldDoc.number)
                else         -> return effError(UnexpectedType(DocType.NUMBER, docType(fieldDoc), path))
            }
        }
    }


    fun maybeDouble(key : String) : ValueParser<Maybe<Double>>
    {
        val integerParser = this.double(key)

        when (integerParser)
        {
            is Val -> return effValue(Just(integerParser.value))
            is Err -> return effValue(Nothing())
        }
    }


    fun boolean(key : String) : ValueParser<Boolean>
    {
        val fieldDoc    = fields[key]

        if (fieldDoc == null)
        {
            return effError(MissingKey(key, path))
        }
        else
        {
            when (fieldDoc)
            {
                is DocBoolean -> return effValue(fieldDoc.boolean)
                else          -> return effError(UnexpectedType(DocType.BOOLEAN, docType(fieldDoc), path))
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


data class DocList(val docs : List<SpecDoc>,
                   override val case : String?,
                   override val path : DocPath) : SpecDoc(path, case)
{

    constructor(docs : List<SpecDoc>, path : DocPath) : this(docs, null, path)


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


data class DocText(val text : String,
                   override val case : String?,
                   override val path : DocPath) : SpecDoc(path, case)
{
    constructor(text : String, path : DocPath) : this(text, null, path)
}

data class DocNumber(val number : Double,
                     override val case : String?,
                     override val path : DocPath) : SpecDoc(path, case)
{
    constructor(number : Double, path : DocPath) : this(number, null, path)
}


data class DocBoolean(val boolean: Boolean,
                      override val case : String?,
                      override val path : DocPath) : SpecDoc(path, case)
{
    constructor(boolean : Boolean, path : DocPath) : this(boolean, null, path)
}



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



typealias DocParse = Eff<List<DocParseError>, Identity, SpecDoc>


fun <A> docError(docErrors : List<DocParseError>) : Eff<List<DocParseError>, Identity, A> =
        Err(docErrors, Identity())

fun <A> docResult(docResult : A) : Eff<List<DocParseError>, Identity, A> =
        Val(docResult, Identity())



sealed class DocParseError(open val path : DocPath)

data class ExpectedProduct(override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = "Expected Product Type\n    path: $path"
}

data class ExpectedSum(override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = "Expected Sum Type\n    path: $path"
}

data class UnknownKind(override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = "Unknown Kind\n    path: $path"
}

data class MissingField(val fieldName : String, override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = "Missing Field\n    field: $fieldName\n    path: $path"
}

data class InvalidCaseType(val caseTypeName : TypeName,
                           override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  =
            """
            Invalid Case Type:
                Case Type: ${caseTypeName.name}
            """

}

data class YamlError(val yamlError : ParseError, override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = "Yaml Error\n    yaml: $yamlError\n    path: $path"
}

data class YamlStringError(val yamlStringParseErrors : List<StringParseError>,
                           override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = "Yaml Parse Error\n    yaml: $yamlStringParseErrors\n    path: $path"
}

data class UnexpectedType(val expected : YamlType,
                          val found : YamlType,
                          override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = """
                                       Unexpected Type
                                           expected: $expected\n
                                           found: $found\n
                                           path: $path
                                        """
}

data class UnknownPrimType(val primValueType : PrimValueType,
                           override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = """
                                       Unknown Primitive Type
                                           type: $primValueType
                                           path: $path
                                       """
}

data class TypeDoesNotExist(val typeName : TypeName, override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = """
                                       Type Does Not Exist
                                           type: $typeName
                                           path: $path
                                       """
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


