
package lulo.document

import com.kispoko.culebra.*
import effect.Eff
import effect.Monoid
import lulo.*
import lulo.value.ValueParser


/**
 * Specification Document
 */


sealed class SpecDoc


data class SpecDict(val fields : Map<String, SpecDoc>) : SpecDoc()
{

    fun text(key : String) : ValueParser<String>
    {

    }


    fun integer(key : String) : ValueParser<Integer>
    {

    }


    fun double(key : String) : ValueParser<Double>
    {

    }

}

data class SpecList(val fields : List<SpecDoc>) : SpecDoc()
data class SpecText(val text : String) : SpecDoc()
data class SpecInteger(val integer : Long) : SpecDoc()
data class SpecNumber(val number : Double) : SpecDoc()


enum class DocType
{
    DICT,
    LIST,
    TEXT,
    INTEGER,
    NUMBER
}


fun docType(doc : SpecDoc) : DocType = when (doc)
{
    is SpecDict    -> DocType.DICT
    is SpecList    -> DocType.LIST
    is SpecText    -> DocType.TEXT
    is SpecInteger -> DocType.INTEGER
    is SpecNumber  -> DocType.NUMBER
}


typealias SpecObjectParser<A> = Eff<SpecObjectParseError, SpecObjectParsePath, A>


sealed class SpecObjectParseError

data class UnknownCase(val caseName : String) : SpecObjectParseError()
data class FieldDoesNotExist(val key : String) : SpecObjectParseError()



data class SpecObjectParsePath(val locations : List<SpecObjectParseLocation>) : Monoid<SpecObjectParsePath>
{
    override fun mappend(path: SpecObjectParsePath) : SpecObjectParsePath
    {
        val locations = mutableListOf<SpecObjectParseLocation>()

        for (location1 in this.locations) {
            when (location1) {
                is SpecObjectKeyLocation -> locations.add(location1)
                is SpecObjectIndexLocation -> locations.add(location1)
            }
        }

        for (location2 in path.locations) {
            when (location2) {
                is SpecObjectKeyLocation   -> locations.add(location2)
                is SpecObjectIndexLocation -> locations.add(location2)
            }
        }

        return SpecObjectParsePath(locations)
    }
}


sealed class SpecObjectParseLocation

data class SpecObjectKeyLocation(val key : String) : SpecObjectParseLocation()
data class SpecObjectIndexLocation(val index : Int) : SpecObjectParseLocation()




sealed class DocParse

data class DocResult(val value : SpecDoc) : DocParse()
data class DocErrors(val errors : List<DocParseError>) : DocParse()


sealed class DocParseError(open val path : DocParsePath)

data class ExpectedProduct(override val path : DocParsePath) : DocParseError(path)
data class UnknownKind(override val path : DocParsePath) : DocParseError(path)
data class MissingField(val fieldName : String, override val path : DocParsePath) : DocParseError(path)
data class YamlError(val yamlError : ParseError, override val path : DocParsePath) : DocParseError(path)
data class YamlStringError(val yamlStringParseErrors : List<StringParseError>,
                           override val path : DocParsePath) : DocParseError(path)
data class UnexpectedType(val expected : YamlType,
                          val found : YamlType,
                          override val path : DocParsePath) : DocParseError(path)
data class UnknownPrimType(val primValueType : PrimValueType,
                           override val path : DocParsePath) : DocParseError(path)
data class TypeDoesNotExist(val typeName : TypeName, override val path : DocParsePath) : DocParseError(path)


data class DocParsePath(val locations : List<DocParseLocation>)
{

    infix fun withLocation(location : DocParseLocation) : DocParsePath =
        DocParsePath(this.locations.plus(location))

}

sealed class DocParseLocation

data class DocKeyLocation(val key : String) : DocParseLocation()
data class DocIndexLocation(val index : Int) : DocParseLocation()


