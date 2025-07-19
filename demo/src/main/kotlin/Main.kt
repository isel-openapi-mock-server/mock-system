import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.*

data class TransactionToken(val transactionToken: String)

data class OpenAPI(
    val spec: String,
)

data class ResponseConfig(
    val statusCode: String,
    val contentType: String?,
    val headers: Map<String, Any>?,
    val body: String?,
)

data class Scenario(
    val name: String,
    val path: String,
    val method: String,
    val responses: List<ResponseConfig>
)

data class Host(
    val host: String
)

suspend fun fetchPost() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    val post: OpenAPI = OpenAPI("info:\n  title: Twilio - Chat\n  description: This is the public Twilio REST API.\n  termsOfService: https://www.twilio.com/legal/tos\n  contact:\n    name: Twilio Support\n    url: https://support.twilio.com\n    email: support@twilio.com\n  license:\n    name: Apache 2.0\n    url: https://www.apache.org/licenses/LICENSE-2.0.html\n  version: 1.0.0\nopenapi: 3.0.1\npaths:\n  /v1/Services/{ServiceSid}/Channels/{ChannelSid}/Invites:\n    servers:\n    - url: https://chat.twilio.com\n    description: Pending invitations to users to become channel members\n    x-twilio:\n      defaultOutputProperties:\n      - sid\n      - identity\n      - date_created\n      parent: /Services/{ServiceSid}/Channels/{Sid}\n      pathType: list\n    post:\n      description: ''\n      summary: ''\n      tags:\n      - ChatV1Invite\n      parameters:\n      - name: ServiceSid\n        in: path\n        description: The SID of the [Service](https://www.twilio.com/docs/api/chat/rest/services)\n          to create the resource under.\n        schema:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^IS[0-9a-fA-F]{32}$\n        required: true\n      - name: ChannelSid\n        in: path\n        description: The SID of the [Channel](https://www.twilio.com/docs/api/chat/rest/channels)\n          the new resource belongs to.\n        schema:\n          type: string\n        required: true\n      responses:\n        '201':\n          content:\n            application/json:\n              schema:\n                __REF__: '#/components/schemas/chat.v1.service.channel.invite'\n              examples:\n                create:\n                  value:\n                    account_sid: ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    channel_sid: CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    created_by: created_by\n                    date_created: '2015-07-30T20:00:00Z'\n                    date_updated: '2015-07-30T20:00:00Z'\n                    identity: identity\n                    role_sid: RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    service_sid: ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    sid: INaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    url: https://chat.twilio.com/v1/Services/ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Channels/CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Invites/INaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n          headers:\n            Access-Control-Allow-Origin:\n              description: Specify the origin(s) allowed to access the resource\n              schema:\n                type: string\n              example: '*'\n            Access-Control-Allow-Methods:\n              description: Specify the HTTP methods allowed when accessing the resource\n              schema:\n                type: string\n              example: POST, OPTIONS\n            Access-Control-Allow-Headers:\n              description: Specify the headers allowed when accessing the resource\n              schema:\n                type: string\n              example: Content-Type, Authorization\n            Access-Control-Expose-Headers:\n              description: Headers exposed to the client\n              schema:\n                type: string\n                example: X-Custom-Header1, X-Custom-Header2\n          description: Created\n      operationId: CreateInvite\n      requestBody:\n        content:\n          application/x-www-form-urlencoded:\n            schema:\n              type: object\n              title: CreateInviteRequest\n              properties:\n                Identity:\n                  type: string\n                  description: The `identity` value that uniquely identifies the new\n                    resource's [User](https://www.twilio.com/docs/api/chat/rest/v1/user)\n                    within the [Service](https://www.twilio.com/docs/api/chat/rest/v1/service).\n                    See [access tokens](https://www.twilio.com/docs/api/chat/guides/create-tokens)\n                    for more info.\n                RoleSid:\n                  type: string\n                  minLength: 34\n                  maxLength: 34\n                  pattern: ^RL[0-9a-fA-F]{32}$\n                  description: The SID of the [Role](https://www.twilio.com/docs/api/chat/rest/roles)\n                    assigned to the new member.\n              required:\n              - Identity\n            examples:\n              create:\n                value:\n                  Identity: identity\n                  RoleSid: RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n  /v1/Services/{ServiceSid}/Channels/{ChannelSid}/Members:\n    servers:\n    - url: https://chat.twilio.com\n    description: Users joined to specific channels\n    x-twilio:\n      defaultOutputProperties:\n      - sid\n      - identity\n      - date_created\n      parent: /Services/{ServiceSid}/Channels/{Sid}\n      pathType: list\n    post:\n      description: ''\n      summary: ''\n      tags:\n      - ChatV1Member\n      parameters:\n      - name: ServiceSid\n        in: path\n        description: The SID of the [Service](https://www.twilio.com/docs/api/chat/rest/services)\n          to create the resource under.\n        schema:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^IS[0-9a-fA-F]{32}$\n        required: true\n      - name: ChannelSid\n        in: path\n        description: The unique ID of the [Channel](https://www.twilio.com/docs/api/chat/rest/channels)\n          the new member belongs to. Can be the Channel resource's `sid` or `unique_name`.\n        schema:\n          type: string\n        required: true\n      responses:\n        '201':\n          content:\n            application/json:\n              schema:\n                __REF__: '#/components/schemas/chat.v1.service.channel.member'\n              examples:\n                create:\n                  value:\n                    sid: MBaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    account_sid: ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    channel_sid: CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    service_sid: ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    identity: jing\n                    role_sid: RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    last_consumed_message_index: null\n                    last_consumption_timestamp: null\n                    date_created: '2016-03-24T21:05:50Z'\n                    date_updated: '2016-03-24T21:05:50Z'\n                    url: https://chat.twilio.com/v1/Services/ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Channels/CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Members/MBaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n          headers:\n            Access-Control-Allow-Origin:\n              description: Specify the origin(s) allowed to access the resource\n              schema:\n                type: string\n              example: '*'\n            Access-Control-Allow-Methods:\n              description: Specify the HTTP methods allowed when accessing the resource\n              schema:\n                type: string\n              example: POST, OPTIONS\n            Access-Control-Allow-Headers:\n              description: Specify the headers allowed when accessing the resource\n              schema:\n                type: string\n              example: Content-Type, Authorization\n            Access-Control-Allow-Credentials:\n              description: Indicates whether the browser should include credentials\n              schema:\n                type: boolean\n            Access-Control-Expose-Headers:\n              description: Headers exposed to the client\n              schema:\n                type: string\n                example: X-Custom-Header1, X-Custom-Header2\n          description: Created\n      operationId: CreateMember\n      requestBody:\n        content:\n          application/x-www-form-urlencoded:\n            schema:\n              type: object\n              title: CreateMemberRequest\n              properties:\n                Identity:\n                  type: string\n                  description: The `identity` value that uniquely identifies the new\n                    resource's [User](https://www.twilio.com/docs/api/chat/rest/v1/user)\n                    within the [Service](https://www.twilio.com/docs/api/chat/rest/services).\n                    See [access tokens](https://www.twilio.com/docs/api/chat/guides/create-tokens)\n                    for more details.\n                RoleSid:\n                  type: string\n                  minLength: 34\n                  maxLength: 34\n                  pattern: ^RL[0-9a-fA-F]{32}$\n                  description: The SID of the [Role](https://www.twilio.com/docs/api/chat/rest/roles)\n                    to assign to the member. The default roles are those specified\n                    on the [Service](https://www.twilio.com/docs/chat/api/services).\n              required:\n              - Identity\n            examples:\n              create:\n                value:\n                  Identity: Twilio\n  /v1/Services/{ServiceSid}/Channels/{ChannelSid}/Messages:\n    servers:\n    - url: https://chat.twilio.com\n    description: Individual chat messages\n    x-twilio:\n      defaultOutputProperties:\n      - sid\n      - from\n      - to\n      - date_created\n      parent: /Services/{ServiceSid}/Channels/{Sid}\n      pathType: list\n    post:\n      description: ''\n      summary: ''\n      tags:\n      - ChatV1Message\n      parameters:\n      - name: ServiceSid\n        in: path\n        description: The SID of the [Service](https://www.twilio.com/docs/api/chat/rest/services)\n          to create the resource under.\n        schema:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^IS[0-9a-fA-F]{32}$\n        required: true\n      - name: ChannelSid\n        in: path\n        description: The unique ID of the [Channel](https://www.twilio.com/docs/api/chat/rest/channels)\n          the new resource belongs to. Can be the Channel resource's `sid` or `unique_name`.\n        schema:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^CH[0-9a-fA-F]{32}$\n        required: true\n      responses:\n        '201':\n          content:\n            application/json:\n              schema:\n                __REF__: '#/components/schemas/chat.v1.service.channel.message'\n              examples:\n                create:\n                  value:\n                    sid: IMaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    account_sid: ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    service_sid: ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    to: CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    channel_sid: CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    attributes: null\n                    date_created: '2016-03-24T20:37:57Z'\n                    date_updated: '2016-03-24T20:37:57Z'\n                    was_edited: false\n                    from: system\n                    body: Hello\n                    index: 0\n                    url: https://chat.twilio.com/v1/Services/ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Channels/CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Messages/IMaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                createWithAttributes:\n                  value:\n                    sid: IMaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    account_sid: ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    service_sid: ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    to: CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    channel_sid: CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n                    date_created: '2016-03-24T20:37:57Z'\n                    date_updated: '2016-03-24T20:37:57Z'\n                    was_edited: false\n                    from: system\n                    attributes: '{}'\n                    body: Hello\n                    index: 0\n                    url: https://chat.twilio.com/v1/Services/ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Channels/CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Messages/IMaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n          headers:\n            Access-Control-Allow-Origin:\n              description: Specify the origin(s) allowed to access the resource\n              schema:\n                type: string\n              example: '*'\n            Access-Control-Allow-Methods:\n              description: Specify the HTTP methods allowed when accessing the resource\n              schema:\n                type: string\n              example: POST, OPTIONS\n            Access-Control-Allow-Headers:\n              description: Specify the headers allowed when accessing the resource\n              schema:\n                type: string\n              example: Content-Type, Authorization\n            Access-Control-Allow-Credentials:\n              description: Indicates whether the browser should include credentials\n              schema:\n                type: boolean\n            Access-Control-Expose-Headers:\n              description: Headers exposed to the client\n              schema:\n                type: string\n                example: X-Custom-Header1, X-Custom-Header2\n          description: Created\n      operationId: CreateMessage\n      requestBody:\n        content:\n          application/x-www-form-urlencoded:\n            schema:\n              type: object\n              title: CreateMessageRequest\n              properties:\n                Body:\n                  type: string\n                  description: The message to send to the channel. Can also be an\n                    empty string or `null`, which sets the value as an empty string.\n                    You can send structured data in the body by serializing it as\n                    a string.\n                From:\n                  type: string\n                  description: The [identity](https://www.twilio.com/docs/api/chat/guides/identity)\n                    of the new message's author. The default value is `system`.\n                Attributes:\n                  type: string\n                  description: A valid JSON string that contains application-specific\n                    data.\n              required:\n              - Body\n            examples:\n              create:\n                value:\n                  Body: Hello\n              createWithAttributes:\n                value:\n                  Body: Hello\n                  Attributes: '{\"test\": \"test\"}'        \ncomponents:\n  schemas:\n    chat.v1.service.channel.invite:\n      type: object\n      properties:\n        sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^IN[0-9a-fA-F]{32}$\n          nullable: true\n          description: The unique string that we created to identify the Invite resource.\n        account_sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^AC[0-9a-fA-F]{32}$\n          nullable: true\n          description: The SID of the [Account](https://www.twilio.com/docs/api/rest/account)\n            that created the Invite resource.\n        channel_sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^CH[0-9a-fA-F]{32}$\n          nullable: true\n          description: The SID of the [Channel](https://www.twilio.com/docs/api/chat/rest/channels)\n            the resource belongs to.\n        service_sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^IS[0-9a-fA-F]{32}$\n          nullable: true\n          description: The SID of the [Service](https://www.twilio.com/docs/api/chat/rest/services)\n            the resource is associated with.\n        identity:\n          type: string\n          nullable: true\n          description: The application-defined string that uniquely identifies the\n            resource's [User](https://www.twilio.com/docs/api/chat/rest/users) within\n            the [Service](https://www.twilio.com/docs/api/chat/rest/services). See\n            [access tokens](https://www.twilio.com/docs/api/chat/guides/create-tokens)\n            for more info.\n          x-twilio:\n            pii:\n              handling: standard\n              deleteSla: 30\n        date_created:\n          type: string\n          format: date-time\n          nullable: true\n          description: The date and time in GMT when the resource was created specified\n            in [RFC 2822](http://www.ietf.org/rfc/rfc2822.txt) format.\n        date_updated:\n          type: string\n          format: date-time\n          nullable: true\n          description: The date and time in GMT when the resource was last updated\n            specified in [RFC 2822](http://www.ietf.org/rfc/rfc2822.txt) format.\n        role_sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^RL[0-9a-fA-F]{32}$\n          nullable: true\n          description: The SID of the [Role](https://www.twilio.com/docs/api/chat/rest/roles)\n            assigned to the resource.\n        created_by:\n          type: string\n          nullable: true\n          description: The `identity` of the User that created the invite.\n        url:\n          type: string\n          format: uri\n          nullable: true\n          description: The absolute URL of the Invite resource.\n    chat.v1.service.channel.member:\n      type: object\n      properties:\n        sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^MB[0-9a-fA-F]{32}$\n          nullable: true\n          description: The unique string that we created to identify the Member resource.\n        account_sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^AC[0-9a-fA-F]{32}$\n          nullable: true\n          description: The SID of the [Account](https://www.twilio.com/docs/api/rest/account)\n            that created the Member resource.\n        channel_sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^CH[0-9a-fA-F]{32}$\n          nullable: true\n          description: The unique ID of the [Channel](https://www.twilio.com/docs/api/chat/rest/channels)\n            for the member.\n        service_sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^IS[0-9a-fA-F]{32}$\n          nullable: true\n          description: The SID of the [Service](https://www.twilio.com/docs/api/chat/rest/services)\n            the resource is associated with.\n        identity:\n          type: string\n          nullable: true\n          description: The application-defined string that uniquely identifies the\n            resource's [User](https://www.twilio.com/docs/api/chat/rest/users) within\n            the [Service](https://www.twilio.com/docs/api/chat/rest/services). See\n            [access tokens](https://www.twilio.com/docs/api/chat/guides/create-tokens)\n            for more info.\n          x-twilio:\n            pii:\n              handling: standard\n              deleteSla: 30\n        date_created:\n          type: string\n          format: date-time\n          nullable: true\n          description: The date and time in GMT when the resource was created specified\n            in [RFC 2822](http://www.ietf.org/rfc/rfc2822.txt) format.\n        date_updated:\n          type: string\n          format: date-time\n          nullable: true\n          description: The date and time in GMT when the resource was last updated\n            specified in [RFC 2822](http://www.ietf.org/rfc/rfc2822.txt) format.\n        role_sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^RL[0-9a-fA-F]{32}$\n          nullable: true\n          description: The SID of the [Role](https://www.twilio.com/docs/api/chat/rest/roles)\n            assigned to the member.\n        last_consumed_message_index:\n          type: integer\n          nullable: true\n          description: The index of the last [Message](https://www.twilio.com/docs/api/chat/rest/messages)\n            in the [Channel](https://www.twilio.com/docs/api/chat/rest/channels) that\n            the Member has read.\n        last_consumption_timestamp:\n          type: string\n          format: date-time\n          nullable: true\n          description: The ISO 8601 timestamp string that represents the date-time\n            of the last [Message](https://www.twilio.com/docs/api/chat/rest/messages)\n            read event for the Member within the [Channel](https://www.twilio.com/docs/api/chat/rest/channels).\n        url:\n          type: string\n          format: uri\n          nullable: true\n          description: The absolute URL of the Member resource.\n    chat.v1.service.channel.message:\n      type: object\n      properties:\n        sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^IM[0-9a-fA-F]{32}$\n          nullable: true\n          description: The unique string that we created to identify the Message resource.\n        account_sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^AC[0-9a-fA-F]{32}$\n          nullable: true\n          description: The SID of the [Account](https://www.twilio.com/docs/api/rest/account)\n            that created the Message resource.\n        attributes:\n          type: string\n          nullable: true\n          description: The JSON string that stores application-specific data. **Note**\n            If this property has been assigned a value, it's only  displayed in a\n            FETCH action that returns a single resource; otherwise, it's null. If\n            the attributes have not been set, `{}` is returned.\n          x-twilio:\n            pii:\n              handling: sensitive\n              deleteSla: 30\n        service_sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^IS[0-9a-fA-F]{32}$\n          nullable: true\n          description: The SID of the [Service](https://www.twilio.com/docs/api/chat/rest/services)\n            the resource is associated with.\n        to:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^CH[0-9a-fA-F]{32}$\n          nullable: true\n          description: The SID of the [Channel](https://www.twilio.com/docs/chat/api/channels)\n            that the message was sent to.\n        channel_sid:\n          type: string\n          minLength: 34\n          maxLength: 34\n          pattern: ^CH[0-9a-fA-F]{32}$\n          nullable: true\n          description: The unique ID of the [Channel](https://www.twilio.com/docs/api/chat/rest/channels)\n            the Message resource belongs to.\n        date_created:\n          type: string\n          format: date-time\n          nullable: true\n          description: The date and time in GMT when the resource was created specified\n            in [RFC 2822](http://www.ietf.org/rfc/rfc2822.txt) format.\n        date_updated:\n          type: string\n          format: date-time\n          nullable: true\n          description: The date and time in GMT when the resource was last updated\n            specified in [RFC 2822](http://www.ietf.org/rfc/rfc2822.txt) format.\n        was_edited:\n          type: boolean\n          nullable: true\n          description: Whether the message has been edited since it was created.\n        from:\n          type: string\n          nullable: true\n          description: The [identity](https://www.twilio.com/docs/api/chat/guides/identity)\n            of the message's author. The default value is `system`.\n          x-twilio:\n            pii:\n              handling: standard\n              deleteSla: 30\n        body:\n          type: string\n          nullable: true\n          description: The content of the message.\n          x-twilio:\n            pii:\n              handling: sensitive\n              deleteSla: 30\n        index:\n          type: integer\n          default: 0\n          description: The index of the message within the [Channel](https://www.twilio.com/docs/chat/api/channels).\n        url:\n          type: string\n          format: uri\n          nullable: true\n          description: The absolute URL of the Message resource.".replace("__REF__", "\$ref"))

    val scenario = Scenario(
        name = "Create Invite",
        path = "/v1/Services/{ServiceSid}/Channels/{ChannelSid}/Invites",
        method = "POST",
        responses = listOf(
            ResponseConfig(
                statusCode = "201",
                contentType = "application/json",
                headers = mapOf(
                    "Access-Control-Allow-Origin" to "*",
                    "Access-Control-Allow-Methods" to "POST, OPTIONS",
                    "Access-Control-Allow-Headers" to "Content-Type, Authorization",
                    "Access-Control-Allow-Credentials" to true,
                ),
                body = """
                    {
                        "sid": "INaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "account_sid": "ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "channel_sid": "{{pathParams.ChannelSid.0}}",
                        "service_sid": "{{pathParams.ServiceSid.0}}",
                        "identity": "identity",
                        "date_created": "2023-10-01T00:00:00Z",
                        "date_updated": "2023-10-01T00:00:00Z",
                        "role_sid": "RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "created_by": "created_by",
                        "url": "https://chat.twilio.com/v1/Services/ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Channels/CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Invites/INaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                    }
                """
            )
        )
    )

    try {
        val token = client.post("http://localhost:8081/admin/openapi") {
            contentType(ContentType.Application.Json)
            setBody(post)
        }.body<TransactionToken>().transactionToken

        val response = client.post("http://localhost:8081/admin/response") {
            contentType(ContentType.Application.Json)
            setBody(scenario)
            header("Transaction-token", token)
        }.body<TransactionToken>().transactionToken

        val host = client.post("http://localhost:8081/admin/commit") {
            header("Transaction-token", token)
        }.body<Host>().host

        println("Host: $host")

    } catch (e: Exception) {
        println("Erro ao buscar: ${e.message}")
    } finally {
        client.close()
    }
}

suspend fun fetchTest(a: String) {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    try {
        val inviteResponse = client.post("http://localhost:8080/v1/Services/ISaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Channels/CHaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Invites") {
            contentType(ContentType.Application.FormUrlEncoded)
            header("Exchange-Key", "test-exchange-key")
            header("ABC", a)
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("Identity", "identity")
                        append("RoleSid", "RLaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                    }
                )
            )
        }.body<TestBody>()

        println("Invite Response: $inviteResponse")

    } catch (e: Exception) {
        println("Erro ao buscar: ${e.message}")
    } finally {
        client.close()
    }
}

fun main() {
    println("Starting Ktor Client Example...")
    while (true) {
        println("Choose an option:")
        println("1. Send OpenAPI spec and create scenario")
        println("2. Fetch Invite Response")
        println("3. Exit")
        val choice = readLine()?.toIntOrNull() ?: 1
        if (choice == 3) break
        runBlocking {
            when (choice) {
                1 -> fetchPost()
                2 -> {
                    println("Enter host:")
                    val host = readLine() ?: ""
                    if (host.isNotEmpty()) {
                        fetchTest(host)
                    } else {
                        println("Host cannot be empty")
                    }
                }
                else -> println("Invalid choice")
            }
        }
    }
}