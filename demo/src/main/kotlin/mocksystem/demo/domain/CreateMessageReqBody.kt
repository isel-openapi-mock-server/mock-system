package mocksystem.demo.domain

data class CreateMessageReqBody(
    val messageBody: String,
    val from: String,
    val attributes: String? = null,
)
