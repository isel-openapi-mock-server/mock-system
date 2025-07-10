package isel.openapi.admin.http

import isel.openapi.admin.http.model.CommitOutputModel
import isel.openapi.admin.http.model.CreateSpecInputModel
import isel.openapi.admin.http.model.CreateSpecOutputModel
import isel.openapi.admin.http.model.Problem
import isel.openapi.admin.http.model.Scenario
import isel.openapi.admin.services.AdminServices
import isel.openapi.admin.services.CommitError
import isel.openapi.admin.services.CreateSpecError
import isel.openapi.admin.services.SaveScenarioError
import isel.openapi.admin.utils.Failure
import isel.openapi.admin.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

const val TRANSACTION_HEADER = "Transaction-token"

@RestController
class AdminController(
    private val adminServices: AdminServices,
) {
    @PostMapping(Uris.Admin.API_SPEC)
    fun addOpenApiSpec(
        @RequestBody openApiSpec: CreateSpecInputModel,
    ): ResponseEntity<*> {
        val res = adminServices.saveNewSpec(openApiSpec.spec)
        return when(res) {
            is Success -> {
                ResponseEntity.status(201)
                    .body(CreateSpecOutputModel(res.value))
            }
            is Failure -> {
                when(res.value) {
                    is CreateSpecError.InvalidOpenApiSpec -> Problem.response(400, Problem.invalidOpenAPISpec)
                }
            }
        }
    }

    @PutMapping(Uris.Admin.RESPONSES)
    fun addResponseConfig(
        @RequestBody scenario: Scenario,
        @RequestParam host: String?,
        @RequestHeader(TRANSACTION_HEADER) transactionToken: String?
    ): ResponseEntity<*> {
        val res = adminServices.saveResponseConfig(host, scenario, transactionToken)
        return when(res) {
            is Success -> {
                ResponseEntity.status(201)
                    .body(CreateSpecOutputModel(res.value))
            }
            is Failure -> {
                when(res.value) {
                    is SaveScenarioError.InvalidTransaction ->
                        Problem.response(400, Problem.invalidTransaction)
                    is SaveScenarioError.HostDoesNotExist ->
                        Problem.response(400, Problem.hostDoesNotExist)
                    is SaveScenarioError.PathOperationDoesNotExist ->
                        Problem.response(404, Problem.pathOperationDoesNotExist)
                    is SaveScenarioError.InvalidResponseContent ->
                        Problem.response(400, Problem.invalidResponseContent)
                    is SaveScenarioError.TransactionOrHostNotProvided ->
                        Problem.response(400, Problem.transactionOrHostNotProvided)
                }
            }
        }
    }

    @PostMapping(Uris.Admin.COMMIT)
    fun commitChanges(
        @RequestParam host: String?,
        @RequestHeader(TRANSACTION_HEADER) transactionToken: String,
    ): ResponseEntity<*> {
        val res = adminServices.commitChanges(host, transactionToken)
        return when(res) {
            is Success -> ResponseEntity.ok().body(CommitOutputModel(res.value))
            is Failure -> {
                when(res.value) {
                    is CommitError.InvalidTransaction -> Problem.response(400, Problem.invalidTransaction)
                    is CommitError.HostDoesNotExist -> Problem.response(400, Problem.hostDoesNotExist)
                }
            }
        }
    }

    @DeleteMapping(Uris.Admin.DELETE)
    fun deleteTransactions() : ResponseEntity<*> {
        adminServices.deleteTransactions()
        return ResponseEntity
            .ok().body(1)
    }

}
