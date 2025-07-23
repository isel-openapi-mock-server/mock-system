package mocksystem.demo.http

import mocksystem.demo.services.Services
import mocksystem.demo.domain.InviteInputModel
import mocksystem.demo.domain.MessagesInputModel
import mocksystem.demo.services.CreateMemberResp
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class Http(
    private val services: Services,
) {

    @PostMapping("/channels/invites")
    suspend fun createInvite(
        @RequestBody body: InviteInputModel
    ): ResponseEntity<*> {

        val res = services.createInvite(body.serviceSid, body.channelSid, body.roleSid, body.identity)

        return if (res != null) {
            ResponseEntity.status(201).body(res)
        } else {
            ResponseEntity.status(404).body("Invite not created\n")
        }
    }

    @GetMapping("/channels/members")
    suspend fun createMemberInChannel(
        @RequestParam serviceSid: String,
        @RequestParam channelSid: String,
        @RequestParam members: List<String>
    ): ResponseEntity<*>{

        val res = services.createMemberInChannel(
            serviceSid = serviceSid,
            channelSid = channelSid,
            members = members
        )

        return when (res) {
            is CreateMemberResp.Success -> ResponseEntity.status(201).body(res.member)
            is CreateMemberResp.Error -> ResponseEntity.status(400).body(res.error)
            else -> ResponseEntity.status(404).body("Member not found\n")
        }

    }

    @PostMapping("/channels/messages")
    suspend fun createMessage(
        @RequestBody body: MessagesInputModel,
    ): ResponseEntity<*> {

        val res = services.createMessage(
            serviceSid = body.serviceSid,
            channelSid = body.channelSid,
            messageBody = body.messageBody,
            from = body.from
        )

        return if (res != null) {
            ResponseEntity.status(201).body(res)
        } else {
            ResponseEntity.status(404).body("Message not created\n")
        }

    }

}