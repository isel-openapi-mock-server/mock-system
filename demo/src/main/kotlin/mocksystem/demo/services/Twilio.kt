package mocksystem.demo.services

import mocksystem.demo.domain.ServiceChannelInvite
import mocksystem.demo.domain.ServiceChannelMember
import mocksystem.demo.domain.ServiceChannelMessage
import io.ktor.client.*

interface Twilio {

    val client: HttpClient

    val host: String

    suspend fun createInvite(serviceSid: String, channelSid: String, roleSid: String, identity: String): ServiceChannelInvite?

    suspend fun createMemberInChannel(serviceSid: String, channelSid: String, members: String): CreateMemberResp?

    suspend fun createMessage(serviceSid: String, channelSid: String, messageBody: String, from: String): ServiceChannelMessage?

}

