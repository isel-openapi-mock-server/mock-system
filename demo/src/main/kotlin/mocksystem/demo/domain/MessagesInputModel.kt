package mocksystem.demo.domain

class MessagesInputModel(
    val serviceSid: String,
    val channelSid: String,
    val messageBody: String,
    val from: String,
    val attributes: String? = null,
)