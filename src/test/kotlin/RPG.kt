
import data.rpg.*
import effect.Err
import effect.Identity
import effect.Val
import io.kotlintest.matchers.beOfType
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.ShouldSpec
import lulo.document.*
import lulo.spec.TypeName
import lulo.value.ValueError
import lulo.File as LuloFile



/**
 * Test the yaml parsing.
 */
class RPG : ShouldSpec()
{
    init
    {

        "Specification Parsing" {

            should("Parses RPG spec without errors") {
                LuloFile.specification(rpgSpecYaml) shouldBe rpgSpecObject
            }

        }


        "Document Parsing" {

            should("Parses RPG document using spec without errors") {
                val spec = LuloFile.specification(rpgSpecYaml)
                val specDoc = spec?.document(silaDocYaml)
                specDoc shouldBe silaDocObject
            }

            should("Should give Missing Field 'name' error for weapon") {
                val spec = LuloFile.specification(rpgSpecYaml)
                val specDocParse = spec?.parseDocument(silaMissingWeaponNameDocYaml)
                when (specDocParse) {
                    is Val -> specDocParse should beOfType<Err<ValueError,Identity,Character>>()
                    is Err ->
                    {
                        val errors = listOf(MissingField("name",
                                                         DocPath(listOf(DocKeyNode("inventory"),
                                                                        DocIndexNode(0)))))
                        specDocParse.error shouldBe errors
                    }
                }
            }

            should("Should give Invalid Case Type error") {
                val spec = LuloFile.specification(rpgSpecYaml)
                val specDocParse = spec?.parseDocument(silaWrongCaseTypeDocYaml)
                when (specDocParse) {
                    is Val -> specDocParse should beOfType<Err<ValueError,Identity,Character>>()
                    is Err ->
                    {
                        val errors = listOf(InvalidCaseType(TypeName("race"),
                                                            DocPath(listOf(DocKeyNode("inventory"),
                                                                           DocIndexNode(0)))))
                        specDocParse.error shouldBe errors
                    }
                }
            }

        }


        "Value Parsing" {

            should("Parses RPG character from document without errors") {
                val spec = LuloFile.specification(rpgSpecYaml)
                val specDoc = spec?.document(silaDocYaml)
                if (specDoc != null)
                {
                    val silaParser = Character.fromDocument(specDoc)
                    when (silaParser)
                    {
                        is Val -> silaParser.value shouldBe silaValue
                        is Err -> silaParser should beOfType<Val<ValueError,Identity,Character>>()
                    }
                }
            }

        }



    }
}

