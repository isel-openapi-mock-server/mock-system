package mocksystem.demo.http

import mocksystem.demo.services.Services
import mocksystem.demo.domain.CreateInviteReqBody
import mocksystem.demo.domain.CreateMemberReqBody
import mocksystem.demo.domain.CreateMessageReqBody
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class Http(
    private val services: Services,
) {

    @PostMapping("/v1/services.Services/{serviceSid}/Channels/{channelSid}/Invites")
    suspend fun createInvite(
        @PathVariable serviceSid: String,
        @PathVariable channelSid: String,
        @RequestBody body: CreateInviteReqBody
    ): ResponseEntity<*> {

        val res = services.createInvite(serviceSid, channelSid, body.roleSid, body.identity)

        return if (res != null) {
            ResponseEntity.status(201).body(res)
        } else {
            ResponseEntity.status(404).body("Invite not created\n")
        }

    }

    @PostMapping("/v1/services.Services/{serviceSid}/Channels/{channelSid}/Members")
    suspend fun createMemberInChannel(
        @PathVariable serviceSid: String,
        @PathVariable channelSid: String,
        @RequestParam members: String,
    ): ResponseEntity<*>{

        val res = services.createMemberInChannel(serviceSid, channelSid, members)

        return if (res != null) {
            ResponseEntity.status(201).body(res)
        } else {
            ResponseEntity.status(404).body("Member not created\n")
        }

    }

    @PostMapping("/v1/services.Services/{serviceSid}/Channels/{channelSid}/Messages")
    suspend fun createMessage(
        @PathVariable serviceSid: String,
        @PathVariable channelSid: String,
        @RequestBody body: CreateMessageReqBody
    ): ResponseEntity<*> {

        val res = services.createMessage(serviceSid, channelSid, body.messageBody, body.from)

        return if (res != null) {
            ResponseEntity.status(201).body(res)
        } else {
            ResponseEntity.status(404).body("Message not created\n")
        }

    }

}