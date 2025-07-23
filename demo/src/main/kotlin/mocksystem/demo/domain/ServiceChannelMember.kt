package mocksystem.demo.domain

import kotlinx.serialization.Serializable

@Serializable
class ServiceChannelMember(
    val username: String,
    val id: Int,
)