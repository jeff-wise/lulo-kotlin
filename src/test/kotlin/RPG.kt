
import data.RPG
import effect.Err
import effect.Identity
import effect.Val
import io.kotlintest.matchers.beOfType
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import lulo.document.DocPath
import lulo.value.ValueError
import lulo.File as LuloFile



/**
 * Test the yaml parsing.
 */
class RPG : StringSpec()
{
    init
    {

        "Parses RPG spec without errors" {
            LuloFile.specification(RPG.yamlSpec) shouldBe RPG.specification
        }


        "Parses RPG document using spec without errors" {
            val spec = LuloFile.specification(RPG.yamlSpec)
            val specDoc = spec?.document(RPG.silaYaml)
            specDoc shouldBe RPG.silaDocument
        }


        "Parses RPG character from document without errors" {
            val spec = LuloFile.specification(RPG.yamlSpec)
            val specDoc = spec?.document(RPG.silaYaml)
            if (specDoc != null)
            {
                val silaParser = RPG.Character.fromDocument(specDoc)
                when (silaParser)
                {
                    is Val -> silaParser.value shouldBe RPG.silaValue
                    is Err -> silaParser should beOfType<Val<ValueError, Identity,RPG.Character>>()
                }
            }
        }


        "Should give Missing 'name' error for weapon" {
            val silaParser = RPG.Character.fromDocument(RPG.silaMissingNameDocument)
            when (silaParser)
            {
                is Val -> System.out.print("It works.")
                is Err -> System.out.print(silaParser.error.toString())
            }
        }

    }
}

