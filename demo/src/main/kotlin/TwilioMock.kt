import domain.ServiceChannelInvite
import domain.ServiceChannelMember
import domain.ServiceChannelMessage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

class TwilioMock(
    override val client: HttpClient,
    override val host: String,
) : Twilio {

    override suspend fun createInvite(serviceSid: String, channelSid: String, roleSid: String, identity: String): ServiceChannelInvite? {
        val response = client.post("http://$host/v1/Services/$serviceSid/Channels/$channelSid/Invites") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("Identity", identity)
                        append("RoleSid", roleSid)
                    }
                )
            )
        }

        return if(response.status == HttpStatusCode.Created) response.body<ServiceChannelInvite>() else null

    }

    override suspend fun createMemberInChannel(serviceSid: String, channelSid: String, roleSid: String, identity: String): ServiceChannelMember? {
        val response = client.post("http://$host/v1/Services/$serviceSid/Channels/$channelSid/Members") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("Identity", identity)
                        append("RoleSid", roleSid)
                    }
                )
            )
        }

        return if(response.status == HttpStatusCode.Created) {
            response.body<ServiceChannelMember>()
        } else {
            null
        }
    }

    override suspend fun createMessage(serviceSid: String, channelSid: String, messageBody: String, from: String): ServiceChannelMessage? {
        val response = client.post("http://$host/v1/Services/$serviceSid/Channels/$channelSid/Messages") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("Body", messageBody)
                        append("From", from)
                    }
                )
            )
        }

        return if(response.status == HttpStatusCode.Created) {
            response.body<ServiceChannelMessage>()
        } else {
            null
        }
    }

}