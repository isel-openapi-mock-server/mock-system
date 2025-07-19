import domain.ServiceChannelInvite
import domain.ServiceChannelMember
import domain.ServiceChannelMessage
import io.ktor.client.*

interface Twilio {

    val client: HttpClient

    val host: String

    suspend fun createInvite(serviceSid: String, channelSid: String, roleSid: String, identity: String): ServiceChannelInvite?

    suspend fun createMemberInChannel(serviceSid: String, channelSid: String, roleSid: String, identity: String): ServiceChannelMember?

    suspend fun createMessage(serviceSid: String, channelSid: String, messageBody: String, from: String): ServiceChannelMessage?

}

