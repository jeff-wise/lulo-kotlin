
package lulo.document

import com.kispoko.culebra.*
import effect.*
import lulo.*
import lulo.value.*
import lulo.value.UnexpectedType



/**
 * Specification Document
 */


sealed class SpecDoc(open val path : DocPath)


data class DocDict(val fields : Map<String, SpecDoc>,
                   val case : String?,
                   override val path : DocPath) : SpecDoc(path)
{

    fun case() : String?
    {
        return case
    }


    fun at(key : String) : ValueParser<SpecDoc>
    {
        val fieldDoc = fields[key]

        if (fieldDoc == null)
        {
            return Err(MissingKey(key), path)
        }
        else
        {
            return Val(fieldDoc, path)
        }
    }


    fun list(key : String) : ValueParser<DocList>
    {
        val fieldDoc = fields[key]

        if (fieldDoc == null)
        {
            return Err(MissingKey(key), path)
        }
        else
        {
            when (fieldDoc)
            {
                is DocList -> return Val(fieldDoc, path)
                else       -> return Err(UnexpectedType(DocType.LIST, docType(fieldDoc)), path)
            }
        }
    }


    fun text(key : String) : ValueParser<String>
    {
        val fieldDoc = fields[key]

        if (fieldDoc == null)
        {
            return Err(MissingKey(key), path)
        }
        else
        {
            when (fieldDoc)
            {
                is DocText -> return Val(fieldDoc.text, path)
                else       -> return Err(UnexpectedType(DocType.TEXT, docType(fieldDoc)), path)
            }
        }
    }


    fun integer(key : String) : ValueParser<Long>
    {
        val fieldDoc = fields[key]

        if (fieldDoc == null)
        {
            return Err(MissingKey(key), path)
        }
        else
        {
            when (fieldDoc)
            {
                is DocInteger -> return Val(fieldDoc.integer, path)
                else           -> return Err(UnexpectedType(DocType.INTEGER, docType(fieldDoc)), path)
            }
        }
    }


    fun maybeInteger(key : String) : ValueParser<Long?>
    {
        val fieldDoc = fields[key]

        if (fieldDoc == null)
        {
            return Val(null, path)
        }
        else
        {
            when (fieldDoc)
            {
                is DocInteger -> return Val(fieldDoc.integer, path)
                else          -> return Err(UnexpectedType(DocType.INTEGER, docType(fieldDoc)), path)
            }
        }
    }


    fun double(key : String) : ValueParser<Double>
    {
        val fieldDoc   = fields[key]

        if (fieldDoc == null)
        {
            return Err(MissingKey(key), path)
        }
        else
        {
            when (fieldDoc)
            {
                is DocNumber -> return Val(fieldDoc.number, path)
                else          -> return Err(UnexpectedType(DocType.NUMBER, docType(fieldDoc)), path)
            }
        }
    }


    fun boolean(key : String) : ValueParser<Boolean>
    {
        val fieldDoc    = fields[key]

        if (fieldDoc == null)
        {
            return Err(MissingKey(key), path)
        }
        else
        {
            when (fieldDoc)
            {
                is DocBoolean -> return Val(fieldDoc.boolean, path)
                else          -> return Err(UnexpectedType(DocType.BOOLEAN, docType(fieldDoc)), path)
            }
        }
    }
}


data class DocList(val docs : List<SpecDoc>,
                   override val path : DocPath) : SpecDoc(path)
{

    fun <T> map(f : (SpecDoc) -> ValueParser<T>) : ValueParser<List<T>>
    {
        val results = mutableListOf<T>()

        docs.forEach { doc ->

            val valueParser = f(doc)

            when (valueParser) {
                is Val -> results.add(valueParser.value)
                is Err  -> {
                    return Err(valueParser.error, path)
                }
            }
        }

        return Val(results, path)
    }

}


data class DocText(val text : String, override val path : DocPath) : SpecDoc(path)
data class DocInteger(val integer : Long, override val path : DocPath) : SpecDoc(path)
data class DocNumber(val number : Double, override val path : DocPath) : SpecDoc(path)
data class DocBoolean(val boolean: Boolean, override val path : DocPath) : SpecDoc(path)


enum class DocType
{
    DICT,
    LIST,
    TEXT,
    INTEGER,
    NUMBER,
    BOOLEAN
}


fun docType(doc : SpecDoc) : DocType = when (doc)
{
    is DocDict    -> DocType.DICT
    is DocList    -> DocType.LIST
    is DocText    -> DocType.TEXT
    is DocInteger -> DocType.INTEGER
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
                                       Unexpected Type\n" +
                                           expected: $expected\n
                                           found: $found\n
                                           path: $path
                                        """
}

data class UnknownPrimType(val primValueType : PrimValueType,
                           override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = """
                                       Unknown Primitive Type\n" +
                                           type: $primValueType
                                           path: $path
                                       """
}

data class TypeDoesNotExist(val typeName : TypeName, override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = """
                                       Type Does Not Exist\n" +
                                           type: $typeName
                                           path: $path
                                       """
}


data class DocPath(val nodes : List<DocNode>) : Monoid<DocPath>
{

    infix fun withLocation(node : DocNode) : DocPath =
        DocPath(this.nodes.plus(node))


    override fun mappend(path: DocPath) : DocPath = path

//    {
//        val locations = mutableListOf<DocParseLocation>()
//
//        for (location1 in this.locations) {
//            when (location1) {
//                is DocKeyLocation   -> locations.add(location1)
//                is DocIndexLocation -> locations.add(location1)
//            }
//        }
//
//        for (location2 in path.locations) {
//            when (location2) {
//                is DocKeyLocation   -> locations.add(location2)
//                is DocIndexLocation -> locations.add(location2)
//            }
//        }
//
//        return DocPath(locations)
//    }


    override fun toString(): String
    {
        var pathString = ""

        var sep = ""
        for (node in this.nodes) {
            pathString += sep
            pathString += node.toString()
            sep = " -> "
        }

        return pathString;
    }

}

sealed class DocNode


data class DocKeyNode(val key : String) : DocNode()
{
    override fun toString(): String
    {
        return "[key: $key]"
    }
}

data class DocIndexNode(val index : Int) : DocNode()
{
    override fun toString(): String
    {
        return "[index: $index]"
    }
}

object DocNullNode : DocNode()
{
    override fun toString(): String = ""
}


