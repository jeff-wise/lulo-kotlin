
package lulo.document


import com.kispoko.culebra.StringParseError
import com.kispoko.culebra.YamlParseError
import com.kispoko.culebra.YamlType
import effect.Eff
import effect.Err
import effect.Identity
import effect.Val
import lulo.spec.PrimValueType
import lulo.spec.TypeName



// ---------------------------------------------------------------------------------------------
// DOCUMENT PARSER
// ---------------------------------------------------------------------------------------------

typealias DocParser = Eff<List<DocParseError>, Identity, SpecDoc>


fun <A> docError(docErrors : List<DocParseError>) : Eff<List<DocParseError>, Identity, A> =
        Err(docErrors, Identity())


fun <A> docResult(docResult : A) : Eff<List<DocParseError>, Identity, A> =
        Val(docResult, Identity())


// ---------------------------------------------------------------------------------------------
// PARSE ERROR
// ---------------------------------------------------------------------------------------------

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


data class YamlError(val yamlError : YamlParseError, override val path : DocPath) : DocParseError(path)
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


data class UnexpectedSymbol(val expectedSymbol : String,
                            override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = """
           |Unknown Primitive Type
           |   type: $expectedSymbol
           |   path: $path
           |"""
}


data class TypeDoesNotExist(val typeName : TypeName, override val path : DocPath) : DocParseError(path)
{
    override fun toString(): String  = """
                                       Type Does Not Exist
                                           type: $typeName
                                           path: $path
                                       """
}

