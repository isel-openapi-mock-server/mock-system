package domain

class ServiceChannelMember(
    val sid: String,
    val account_sid: String,
    val channel_sid: String,
    val service_sid: String,
    val identity: String,
    val date_created: String,
    val date_updated: String,
    val role_sid: String,
    val last_consumed_message_index: Int,
    val last_consumed_timestamp: String,
    val url: String
)