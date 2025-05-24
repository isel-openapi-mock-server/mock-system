package isel.openapi.admin.domain

import com.github.erosb.jsonsKema.*
import isel.openapi.admin.parsingServices.model.*
import isel.openapi.admin.utils.Either
import isel.openapi.admin.utils.failure
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

sealed interface VerifyResponseError {

    data class MissingHeader(val name: String) : VerifyResponseError

    data class MissingHeaderContent(val name: String) : VerifyResponseError

    data class InvalidType(val name: String, val expectedType: String, val givenType: String) : VerifyResponseError

    data class InvalidBodyFormat(val expectedFormat: String, val givenFormat: ByteArray) : VerifyResponseError

    data object WrongStatusCode : VerifyResponseError

}

//typealias VerifyResponseResult = Either<VerifyResponseError, Boolean>

@Component
class AdminDomain {

    fun generateHost(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val length = 16
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    fun verifyResponse(
        responseSpec: Response,
        statusCode: StatusCode,
        contentType: String?,
        headers: Map<String, String>?,
        body: ByteArray?
    ):  List<VerifyResponseError> {

        val failList = mutableListOf<VerifyResponseError>()

        if (responseSpec.statusCode != statusCode) {

            failList.add(VerifyResponseError.WrongStatusCode)

            return failList
        }

        failList.addAll(verifyHeaders(responseSpec.headers, headers, contentType))

        failList.addAll(verifyBody(responseSpec.schema, body, contentType))

        return failList
    }

    private fun verifyHeaders(headersSpec: List<ApiHeader>, headers: Map<String, String>?, contentType: String?): List<VerifyResponseError> {
        val failList = mutableListOf<VerifyResponseError>()

        if (headersSpec.isEmpty() && headers != null) {
            return TODO() //erro headers que a mais
        }
        // nao há headers
        if (headers == null) {
            // eram esperados headers obrigatórios
            if (headersSpec.isNotEmpty() && headersSpec.any { it.required }) {
                return TODO() //lista com todos os headers que estão em falta e sao obrigatorios
            }
            // nao são esperados headers, ou nenhum dos esperados é obrigatório
            return failList
        }

        headersSpec.forEach { headerSpec ->
            if(headerSpec.required && !headers.containsKey(headerSpec.name)) {
                failList.add(VerifyResponseError.MissingHeader(headerSpec.name))
            }

            val headerValue = headers[headerSpec.name]

            if(headerValue == null && headerSpec.required) {
                failList.add(VerifyResponseError.MissingHeaderContent(headerSpec.name))
            }

            when(val headerType = headerSpec.type) {
                is ContentOrSchema.SchemaObject -> {
                    val validationResult = jsonValidator(headerType.schema , "\"$headerValue\"" )
                    if(validationResult != null) {
                        failList.add(VerifyResponseError.InvalidType(headerSpec.name, headerSpec.type.toString(), convertToType(headerValue).toString()))
                    }
                }
                is ContentOrSchema.ContentField -> {
                    val contentField = headerType.content[contentType]
                    if(contentField == null) {
                        failList.add(VerifyResponseError.InvalidType(headerSpec.name, headerSpec.type.toString(), convertToType(headerValue).toString()))
                    }
                    val validationResult = jsonValidator(contentField?.schema, "\"$headerValue\"")
                    if(validationResult != null) {
                        failList.add(VerifyResponseError.InvalidType(headerSpec.name, headerSpec.type.toString(), convertToType(headerValue).toString()))
                    }
                }
            }
        }

        return failList
    }

    private fun verifyBody(bodySpec: ContentOrSchema.ContentField?, body: ByteArray?, contentType: String?): List<VerifyResponseError> {

        val failList = mutableListOf<VerifyResponseError>()

        //TODO()
        if (bodySpec == null && body == null && contentType == null) return failList

        if (bodySpec != null && body != null && contentType != null) {
            try {
                bodySpec.content.forEach { (key, value) ->
                    if (key == contentType) {
                        val validationResult = jsonValidator(value.schema, body.toString())
                        if (validationResult != null) {
                            failList.add(
                                VerifyResponseError.InvalidBodyFormat(
                                    bodySpec.content[contentType]?.schema.toString() ?: contentType, body
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                failList.add(
                    VerifyResponseError.InvalidBodyFormat(
                        bodySpec.content[contentType]?.schema.toString() ?: contentType, body
                    )
                )
            }
        }
        else {
            // TODO ver as falhas em falta
        }

        return failList
    }

    private fun jsonValidator(
        schema: String?,
        receivedType: String,
    ): ValidationFailure? {

        if(schema == null) { return null }

        val jsonVal = JsonParser(schema).parse()

        val schemaLoader = SchemaLoader(jsonVal)
        val validator = Validator.create(schemaLoader.load(), ValidatorConfig(FormatValidationPolicy.ALWAYS))
        try {
            val receivedJsonType = JsonParser(receivedType).parse()
            val validationResult = validator.validate(receivedJsonType)
            return validationResult
        } catch (e: JsonParseException) {
            println(e.location)
            println(e.message)
            println(e.localizedMessage)
            println(e.cause)
            println(e.suppressed)
            println(e.stackTrace)
            return null // TODO mudar
        }
    }

    private fun convertToType(value: Any?): Type {
        return when (value) {
            null -> Type.NullType
            is Boolean -> Type.BooleanType
            is Int -> Type.IntegerType
            is Number -> Type.NumberType
            is String -> Type.StringType
            is List<*> -> {
                val elementType = convertToType(value.firstOrNull())
                Type.ArrayType(elementType)
            }
            is Map<*, *> -> {
                val fieldsTypes = value.map { (key, value) ->
                    if(key !is String) throw IllegalArgumentException("Invalid key type")
                    key to convertToType(value)
                }.toMap()
                Type.ObjectType(fieldsTypes)
            }
            else -> Type.UnknownType
        }
    }

}