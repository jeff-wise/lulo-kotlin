
import com.kispoko.culebra.encodeYaml
import data.rpg.*
import effect.Eff
import effect.Err
import effect.Identity
import effect.Val
import io.kotlintest.matchers.beOfType
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.ShouldSpec
import lulo.*
import lulo.document.*
import lulo.schema.TypeName
import lulo.value.ValueError



/**
 * Test the yaml parsing.
 */
class RPG : ShouldSpec()
{
    init
    {

        "Specification Parsing" {

            should("Parses RPG schema without errors") {
                val schema = parseSchema(rpgSchemaYaml)
                when (schema) {
                    is SchemaParseValue -> schema.schema shouldBe rpgSchemaObject
                    is SchemaParseError -> schema should beOfType<SchemaParseValue>()
                }

            }

        }


        "Document Parsing" {

            should("Parses RPG document using schema without errors") {
                val schema = parseSchema(rpgSchemaYaml)
                when (schema) {
                    is SchemaParseValue -> {
                        val schemaDoc = schema.schema.document(silaDocYaml)
                        schemaDoc shouldBe silaDocObject
                    }
                    is SchemaParseError -> schema should beOfType<SchemaParseValue>()
                }
            }

            should("Should give Missing Field 'name' error for weapon") {
                val schema = parseSchema(rpgSchemaYaml)

                when (schema) {
                    is SchemaParseValue -> {
                        val schemaDocParser = schema.schema.parseDocument(silaMissingWeaponNameDocYaml)
                        when (schemaDocParser) {
                            is Val ->
                                schemaDocParser should beOfType<Err<List<DocParseError>,Identity,SchemaDoc>>()
                            is Err -> {
                                val errors = listOf(MissingField("name",
                                                             DocPath(listOf(DocKeyNode("inventory"),
                                                                     DocIndexNode(0)))))
                                schemaDocParser.error shouldBe errors
                            }
                        }
                    }
                }
            }

            should("Should give Invalid Case Type error") {
                val schema = parseSchema(rpgSchemaYaml)
                when (schema) {
                    is SchemaParseValue -> {
                        val schemaDocParser = schema.schema.parseDocument(silaWrongCaseTypeDocYaml)
                        when (schemaDocParser) {
                            is Val ->
                                schemaDocParser should beOfType<Err<List<DocParseError>,Identity,SchemaDoc>>()
                            is Err -> {

                                val errors = listOf(InvalidCaseType(TypeName("race"),
                                                        DocPath(listOf(DocKeyNode("inventory"),
                                                                DocIndexNode(0)))))
                                schemaDocParser.error shouldBe errors
                            }
                        }
                    }
                }

            }


        }


        "Value Parsing" {

            should("Parses RPG character from document without errors") {
                val schema = parseSchema(rpgSchemaYaml)
                when (schema) {
                    is SchemaParseValue -> {
                        val schemaDocParser = schema.schema.parseDocument(silaDocYaml)
                        when (schemaDocParser)
                        {
                            is Val ->
                            {
                                val silaParser = Character.fromDocument(schemaDocParser.value)
                                when (silaParser)
                                {
                                    is Val -> silaParser.value shouldBe silaValue
                                    is Err -> silaParser should beOfType<Val<ValueError,Identity,Character>>()
                                }
                            }
                            is Err -> schemaDocParser should beOfType<Val<List<DocParseError>, Identity, SchemaDoc>>()
                        }
                    }
                    is SchemaParseError -> schema should beOfType<SchemaParseValue>()
                }
            }

        }


        "Document Generating" {

            should("Generate yaml string then parse back to original doc") {
                val docYamlString = encodeYaml(silaDocObject)
                val schema = parseSchema(rpgSchemaYaml)
                when (schema) {
                    is SchemaParseValue -> {
                        val schemaDoc = schema.schema.document(docYamlString)
                        schemaDoc shouldBe silaDocObject
                    }
                    is SchemaParseError -> schema should beOfType<SchemaParseValue>()
                }
                // System.out.println(docYamlString)
            }
        }

    }
}

