package mocksystem.demo.services

import mocksystem.demo.domain.ServiceChannelInvite
import mocksystem.demo.domain.ServiceChannelMember
import mocksystem.demo.domain.ServiceChannelMessage
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.slf4j.LoggerFactory

class TwilioMock(
    override val client: HttpClient,
    override val host: String,
) : Twilio {

    override suspend fun createInvite(serviceSid: String, channelSid: String, roleSid: String, identity: String): ServiceChannelInvite? {
        try {
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
        } catch (e: Exception) {
            // Handle exceptions, e.g., log them or rethrow
            logger.info("Error creating invite: ${e.message}")
            return null
        }

    }

    override suspend fun createMemberInChannel(serviceSid: String, channelSid: String, members: String): ServiceChannelMember? {
        try {
            val response = client.post("http://$host/v1/Services/$serviceSid/Channels/$channelSid/Members?members=$members") {
                contentType(ContentType.Application.FormUrlEncoded)
            }

            return if(response.status == HttpStatusCode.Created) {
                response.body<ServiceChannelMember>()
            } else {
                null
            }
        } catch (e: Exception) {
            // Handle exceptions, e.g., log them or rethrow
            logger.info("Error creating member in channel: ${e.message}")
            return null
        }

    }

    override suspend fun createMessage(serviceSid: String, channelSid: String, messageBody: String, from: String): ServiceChannelMessage? {
        try {
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
        } catch (e: Exception) {
            // Handle exceptions, e.g., log them or rethrow
            logger.info("Error creating message: ${e.message}")
            return null
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(TwilioMock::class.java)
    }
}