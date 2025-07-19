
class Services(
    private val twilio: Twilio
) {

    suspend fun createInvite(serviceSid: String, channelSid: String, roleSid: String, identity: String) =
        twilio.createInvite(serviceSid, channelSid, roleSid, identity)

    suspend fun createMemberInChannel(serviceSid: String, channelSid: String, roleSid: String, identity: String) =
        twilio.createMemberInChannel(serviceSid, channelSid, roleSid, identity)


    suspend fun createMessage(serviceSid: String, channelSid: String, messageBody: String, from: String) =
        twilio.createMessage(serviceSid, channelSid, messageBody, from)

}