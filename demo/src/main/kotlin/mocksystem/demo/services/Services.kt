package mocksystem.demo.services

import mocksystem.demo.domain.ServiceChannelInvite
import org.springframework.stereotype.Component

@Component
class Services(
    private val twilio: Twilio
) {

    suspend fun createInvite(serviceSid: String, channelSid: String, roleSid: String, identity: String): Pair<ServiceChannelInvite?, Boolean> =
        twilio.createInvite(serviceSid, channelSid, roleSid, identity)

    suspend fun createMemberInChannel(serviceSid: String, channelSid: String, members: List<String>) =
        twilio.createMemberInChannel(serviceSid, channelSid, members)

    suspend fun createMessage(serviceSid: String, channelSid: String, messageBody: String, from: String) =
        twilio.createMessage(serviceSid, channelSid, messageBody, from)

}