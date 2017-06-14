
package lulo.value


import effect.*
import lulo.document.*



/**
 * Value
 */


typealias ValueParser<A> = Eff<ValueError, Identity, A>



sealed class ValueError(open val path : DocPath)

data class UnexpectedType(val expected : DocType,
                          val found : DocType,
                          override val path : DocPath) : ValueError(path)
{
    override fun toString(): String =
            """
            Unexpected Type:
                Expected : $expected
                Found : $found
                Path : $path
            """
}

data class MissingKey(val key : String, override val path : DocPath) : ValueError(path)
{
    override fun toString(): String =
            """
            Missing Key: $key
            Path : $path
            """
}

data class UnknownCase(val caseName : String?, override val path : DocPath) : ValueError(path)
{
    override fun toString(): String =
            """
            Unknown Case:
                Case : $caseName
                Path : $path
            """
}

data class UnexpectedValue(val typeName : String?,
                           val value : String,
                           override val path : DocPath) : ValueError(path)
{
    override fun toString(): String  =
            """
            Unknown EnumValue
                Of Type: $typeName
                Value: $value
                Path: $path
            """
}

//
//fun <E> errorMessage(valueErr : Err<ValueError,DocLog,E>) : String =
//        """
//        Value Parser Error:
//            Error: ${valueErr.error}
//            Path: ${valueErr.env}
//        """


fun <A> valueResult(valueResult : A) : Eff<ValueError, DocLog, A> =
        Val(valueResult, DocLogEmpty())

fun <A> valueError(valueResult : A) : Eff<ValueError, DocLog, A> =
        Val(valueResult, DocLogEmpty())

