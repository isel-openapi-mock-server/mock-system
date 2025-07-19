package domain

class ServiceChannelMessage (
    val sid: String,
    val account_sid: String,
    val attributes: String,
    val service_sid: String,
    val to: String,
    val channel_sid: String,
    val date_created: String,
    val date_updated: String,
    val was_edited: Boolean,
    val from: String,
    val body: String,
    val index: Int,
    val url: String
)