
package lulo


import effect.Err
import effect.Val
import lulo.document.DocParseError
import lulo.schema.Schema
import lulo.schema.schemaSchema
import lulo.value.ValueError



// ---------------------------------------------------------------------------------------------
// SCHEMA PARSE
// ---------------------------------------------------------------------------------------------


fun parseSchema(yamlString : String) : SchemaParseResult
{
    val schemaDoc = schemaSchema.parseDocument(yamlString)
    when (schemaDoc)
    {
        is Val ->
        {
            val schema = Schema.fromDocument(schemaDoc.value)
            return when (schema)
            {
                is Val -> SchemaParseValue(schema.value)
                is Err -> SchemaInternalError(schema.error)
            }
        }
        is Err ->
        {
            return SchemaFormatError(schemaDoc.error)
        }
    }

}

//
//fun specification(yamlInputStream : InputStream) : SpecResult
//    {
//        val stringParse = YamlString.parse(yamlInputStream)
//        when (stringParse)
//        {
//            is StringResult ->
//            {
//                val specParser = Yaml.parseSpec(stringParse.value)
//                when (specParser)
//                {
//                    is Result -> return SpecValue(specParser.value)
//                    is Error  -> {
//                        return SpecError(specParser.toString())
//                    }
//                }
//            }
//            is StringErrors -> {
//                var errorString = ""
//                for (error in stringParse.errors) {
//                    errorString += error.toString() + "\n"
//                }
//                return SpecError(errorString)
//            }
//        }
//    }


// ---------------------------------------------------------------------------------------------
// SCHEMA PARSE RESULT
// ---------------------------------------------------------------------------------------------

sealed class SchemaParseResult


data class SchemaParseValue(val schema : Schema) : SchemaParseResult()


sealed class SchemaParseError : SchemaParseResult()


data class SchemaFormatError(val docParseError : List<DocParseError>) : SchemaParseError()

data class SchemaInternalError(val valueError : ValueError) : SchemaParseError()

