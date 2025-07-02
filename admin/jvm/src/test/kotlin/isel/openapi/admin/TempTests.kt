package isel.openapi.admin

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.jknack.handlebars.Handlebars
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaValidatorsConfig
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import isel.openapi.admin.parsing.model.*
import kotlin.test.Test


class TempTests {

    @Test
    fun `json schema validator`() {
        //val mapper = ObjectMapper()

        val factory: JsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)

        val bodyType = """
                    {
                        "type": "integer"
                    }                    
                    """.trimIndent()

        val schema: JsonSchema = factory.getSchema(bodyType)

        val mapper = ObjectMapper()

        val dataNode: JsonNode = mapper.readTree("123")

        val errors: Set<ValidationMessage> = schema.validate(dataNode)

        println(String("ola".toByteArray(), Charsets.UTF_8))

        val valMsg = ValidationMessage.builder()
            .message("Invalid JSON format: ")
            .build()

        val node = valMsg.instanceNode?.asText()

        println(node)

        for (message in errors) {
            println("Message - ${message.message}")
           /* println("Type - ${message.type}")
            println("MessageKey - ${message.messageKey}")
            println("isValid - ${message.isValid}")
            println("SchemaNode - ${message.schemaNode}")
            println("Arguments - ${message.arguments.joinToString(", ")}")
            println("Code - ${message.code}")
            println("Details - ${message.details}")
            println("Error - ${message.error}")
            println("EvaluationPath - ${message.evaluationPath}")
            println("InstanceLocation - ${message.instanceLocation}")
            println("InstanceNode - ${message.instanceNode}")
            println("Property - ${message.property}")
            println("SchemaLocation - ${message.schemaLocation}")
            */
            val node = message.instanceNode.asText()
            val size = node.length

            if (node.first() == '{' && node[1] == '{' && node.last() == '}' && node[size - 2] == '}') {
                println("Found handlebars template: $node")
            }
        }

        if (errors.isEmpty()) println("Sem erros")
    }

    companion object {
        object AllowedTemplateVars {
            val allowed = setOf("request.host", "request.method", "user.name")
        }

        fun extractHandlebarsVars(template: String): Set<String> {
            val regex = Regex("""\{\{\s*([a-zA-Z0-9_.]+)\s*}}""")
            return regex.findAll(template).map { it.groupValues[1] }.toSet()
        }

        fun validateTemplateVars(template: String, allowedVars: Set<String>): List<String> {
            val usedVars = extractHandlebarsVars(template)
            return usedVars.filter { it !in allowedVars }
        }

        fun test() {
            // Usage
            val userTemplate = "Hello {{request.host}}, method: {{request.method}}, bad: {{request.abc}}"
            val invalidVars = validateTemplateVars(userTemplate, AllowedTemplateVars.allowed)
            if (invalidVars.isNotEmpty()) {
                println("Invalid template variables: $invalidVars")
            } else {
                println("All variables are valid")
            }
        }

    }

    @Test
    fun `handlebars test`() {
        val handlebars = Handlebars()
        val template = handlebars.compileInline("Bom dia {{name.2}}!")

        //val res = template.apply(Context.newBuilder(mapOf("name" to "Tomané")).build())

        //val res = template.apply(Context.newContext(mapOf("name" to "Tomané")))

        data class Person(val name: List<String>)
        val person = Person(listOf("Marco","Tomané"))

        val res = template.apply(person)

        println(res)

    }

    @Test
    fun asd() {

        val response = listOf(
            Response(
            statusCode = StatusCode.OK,
            schema = ContentOrSchema.ContentField(
                content = mapOf(
                    Pair(
                        "application/json",
                        ContentOrSchema.SchemaObject(
                            """
                            {
                                "type": "null"
                            }
                            """.trimIndent()
                        )
                    )
                )
            )
            )
        )

        val expectedHeaders =
            ApiHeader(
                name = "Content-Type",
                type = ContentOrSchema.SchemaObject(
                    schema = null
                ),
                required = true,
                style = ParameterStyle.FORM,
                explode = false,
                description = null
            )


        //val gson = Gson()

        //val json = gson.toJson(dynamicHandler)

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()
            //.setSerializationInclusion(JsonInclude.Include.ALWAYS)
            /*.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE,
                JsonTypeInfo.As.PROPERTY
            )*/

        val json = mapper.writeValueAsString(expectedHeaders)

        println(json)

        val responseFromJson: ApiHeader = mapper.readValue(json, object : TypeReference<ApiHeader>() {})

        println("first deserialization: $responseFromJson")
        println("type value: ${responseFromJson.type}")
        val a = ContentOrSchema.SchemaObject(null)
        val aJson = mapper.writeValueAsString(a)
        val aFromJson: ContentOrSchema.SchemaObject = mapper.readValue(aJson, object : TypeReference<ContentOrSchema.SchemaObject>() {})
        println("correct type value: $expectedHeaders")
        if (responseFromJson.type is ContentOrSchema.SchemaObject) {
            println("schema")
        }
        if (responseFromJson.type is ContentOrSchema.ContentField)
            println("content")
    }
}