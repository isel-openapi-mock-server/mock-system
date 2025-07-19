package domain

class ServiceChannelMember(
    val sid: String,
    val accountSid: String,
    val channelSid: String,
    val serviceSid: String,
    val identity: String,
    val dateCreated: String,
    val dateUpdated: String,
    val roleSid: String,
    val lastConsumedMessageIndex: Int,
    val lastConsumedTimestamp: String,
    val url: String
)