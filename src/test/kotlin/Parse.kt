
import data.RPG
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import lulo.File as LuloFile



/**
 * Test the yaml parsing.
 */
class ParseTests : StringSpec()
{
    init
    {

        "Parses example RPG spec without errors" {
            LuloFile.specification(RPG.yamlSpec) shouldBe RPG.spec1
        }

    }
}

