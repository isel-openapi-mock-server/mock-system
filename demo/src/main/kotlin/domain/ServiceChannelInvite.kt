package domain

class ServiceChannelInvite(
    val sid: String,
    val accountSid: String,
    val channelSid: String,
    val serviceSid: String,
    val identity: String,
    val dateCreated: String,
    val dateUpdated: String,
    val roleSid: String,
    val createdBy: String,
    val url: String
)