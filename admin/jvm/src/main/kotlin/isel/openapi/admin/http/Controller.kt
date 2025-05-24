package isel.openapi.admin.http

import isel.openapi.admin.http.model.CreateSpecInputModel
import isel.openapi.admin.http.model.CreateSpecOutputModel
import isel.openapi.admin.http.model.Scenario
import isel.openapi.admin.parsingServices.Parsing
import isel.openapi.admin.services.AdminServices
import isel.openapi.admin.services.CreateSpecError
import isel.openapi.admin.services.RequestInfoError
import isel.openapi.admin.utils.Failure
import isel.openapi.admin.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class AdminController(
    private val adminServices: AdminServices,
) {
    @PutMapping("/openapi")
    fun addOpenApiSpec(
        @RequestBody openApiSpec: CreateSpecInputModel,
        @RequestParam host: String?,
        @RequestHeader transactionToken: String?
    ): ResponseEntity<*> {
        val res = adminServices.saveSpec(openApiSpec.spec, host, transactionToken)
        return when(res) {
            is Success -> {
                ResponseEntity.status(201)
                    .body(CreateSpecOutputModel(res.value))
            }
            is Failure -> {
                when(res.value) {
                    is CreateSpecError.InvalidOpenApiSpec ->
                        ResponseEntity.badRequest()
                            .body("Invalid OpenAPI Spec")
                    is CreateSpecError.HostDoesNotExist ->
                        ResponseEntity.badRequest()
                            .body("Host does not exist")
                }
            }
        }
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
                        ResponseEntity.status(404)
                            .body("Request not found")
                    }
                    is RequestInfoError.RequestCredentialNotFound -> {
                        ResponseEntity.badRequest().body("Invalid request")
                    }
                }
            }
        }
    }

    @PutMapping("/response")
    fun addResponseConfig(
        @RequestBody scenario: Scenario,
        @RequestHeader host: String,
        //@RequestHeader accessToken: String,
        @RequestHeader transactionToken: String?
    ): ResponseEntity<*> {
        adminServices.saveResponseConfig(host, scenario, transactionToken)
        TODO()
    }

    @PostMapping("/transaction")
    fun endTransaction(
        @RequestHeader transactionToken: String?
    ): ResponseEntity<*> {

        TODO()
    }

    @PostMapping("/commit")
    fun commitChanges(
        @RequestHeader host: String?,
        @RequestParam transaction: String,
    ): ResponseEntity<*> {
        adminServices.commitChanges(host, transaction)
    }
}
