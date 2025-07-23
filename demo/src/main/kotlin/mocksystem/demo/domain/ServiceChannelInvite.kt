package mocksystem.demo.domain

import kotlinx.serialization.Serializable

@Serializable
class ServiceChannelInvite(
    val sid: String,
    val account_sid: String,
    val channel_sid: String,
    val service_sid: String,
    val identity: String,
    val date_created: String,
    val date_updated: String,
    val role_sid: String,
    val created_by: String,
    val url: String
)