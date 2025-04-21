package isel.openapi.admin.http

import isel.openapi.admin.parsingServices.Parsing
import isel.openapi.admin.services.AdminServices
import isel.openapi.admin.services.RequestInfoError
import isel.openapi.admin.utils.Failure
import isel.openapi.admin.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class AdminController(
    private val parsing: Parsing,
    private val adminServices: AdminServices,
) {
    @PostMapping("/openapi")
    fun addOpenApiSpec(
    ): ResponseEntity<*> {
        return ResponseEntity.ok("Admin application started")
    }

    @GetMapping("/requests")
    fun getRequestInfo(
        @RequestParam("uuid") uuid: String?,
        @RequestParam("externalKey") externalKey: String?,
    ): ResponseEntity<*> {
        if (uuid == null && externalKey == null) {
            return ResponseEntity.badRequest().body("Either uuid or externalKey must be provided")
        }
        val res = adminServices.getRequestInfo(uuid, externalKey)
        return when (res) {
            is Success -> {
                ResponseEntity.status(200)
                    .body(res.value)
            }
            is Failure -> {
                when (res.value) {
                    is RequestInfoError.RequestNotFound -> {
                        ResponseEntity.notFound().build<Unit>()
                    }
                    is RequestInfoError.RequestCredentialNotFound -> {
                        ResponseEntity.badRequest().body("Invalid request")
                    }
                }
            }
        }
    }
}
