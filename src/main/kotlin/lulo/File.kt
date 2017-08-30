
package lulo


import com.kispoko.culebra.YamlString
import lulo.schema.Schema
import java.io.InputStream



/**
 * Parse a Lulo yaml file
 */
//
//object File
//{
//
//    fun specification(yamlInputStream : InputStream) : SpecResult
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
//
//
//    fun specification(yamlString : String) : Schema?
//    {
//        val stringParse = YamlString.parse(yamlString)
//        when (stringParse) {
//            is StringResult -> {
//                val specParser = Yaml.parseSpec(stringParse.value)
//                when (specParser) {
//                    is Result -> return specParser.value
//                    is Error  -> {
//                        System.out.println(specParser.toString())
//                        return null
//                    }
//                }
//            }
//            is StringErrors -> {
//                for (error in stringParse.errors) {
//                    System.out.println(error.toString())
//                }
//                return null
//            }
//        }
//    }
//
//
//}

