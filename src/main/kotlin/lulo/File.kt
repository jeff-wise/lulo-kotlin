
package lulo


import com.kispoko.culebra.Error
import com.kispoko.culebra.Result
import com.kispoko.culebra.StringErrors
import com.kispoko.culebra.StringResult
import com.kispoko.culebra.YamlString
import lulo.spec.Spec
import lulo.spec.Yaml
import java.io.InputStream



/**
 * Parse a Lulo yaml file
 */

object File
{

    fun specification(yamlInputStream : InputStream) : SpecResult
    {
        val stringParse = YamlString.parse(yamlInputStream)
        when (stringParse)
        {
            is StringResult ->
            {
                val specParser = Yaml.parseSpec(stringParse.value)
                when (specParser)
                {
                    is Result -> return SpecValue(specParser.value)
                    is Error  -> {
                        return SpecError(specParser.toString())
                    }
                }
            }
            is StringErrors -> {
                var errorString = ""
                for (error in stringParse.errors) {
                    errorString += error.toString() + "\n"
                }
                return SpecError(errorString)
            }
        }
    }


    fun specification(yamlString : String) : Spec?
    {
        val stringParse = YamlString.parse(yamlString)
        when (stringParse) {
            is StringResult -> {
                val specParser = Yaml.parseSpec(stringParse.value)
                when (specParser) {
                    is Result -> return specParser.value
                    is Error  -> {
                        System.out.println(specParser.toString())
                        return null
                    }
                }
            }
            is StringErrors -> {
                for (error in stringParse.errors) {
                    System.out.println(error.toString())
                }
                return null
            }
        }
    }


}


sealed class SpecResult

data class SpecValue(val spec : Spec) : SpecResult()
data class SpecError(val error : String) : SpecResult()
