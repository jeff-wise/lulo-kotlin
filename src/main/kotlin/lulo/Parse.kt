
package lulo


import com.kispoko.culebra.Error
import com.kispoko.culebra.Result
import com.kispoko.culebra.StringErrors
import com.kispoko.culebra.StringResult
import com.kispoko.culebra.YamlString



/**
 * Parse a Lulo yaml file
 */

object File
{

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

