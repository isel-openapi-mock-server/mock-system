package mocksystem.demo.domain

@kotlinx.serialization.Serializable
class ServiceChannelMessage (
    val sid: String,
    val accountSid: String,
    val attributes: String,
    val serviceSid: String,
    val to: String,
    val channelSid: String,
    val dateCreated: String,
    val dateUpdated: String,
    val wasEdited: Boolean,
    val from: String,
    val body: String,
    val index: Int,
    val url: String
)