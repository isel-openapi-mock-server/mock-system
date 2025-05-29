package isel.openapi.admin.http

import isel.openapi.admin.http.model.Problem
import isel.openapi.admin.parsing.model.HttpMethod
import isel.openapi.admin.services.RequestsServices
import isel.openapi.admin.services.RequestInfoError
import isel.openapi.admin.services.SearchError
import isel.openapi.admin.utils.Failure
import isel.openapi.admin.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class RequestsController(
    private val requestsServices: RequestsServices,
) {

    @GetMapping(Uris.Requests.REQUESTS)
    fun getRequestInfo(
        @RequestParam exchangeKey: String?,
        @RequestParam externalKey: String?,
    ): ResponseEntity<*> {
        val res = requestsServices.getRequestInfo(exchangeKey, externalKey)
        return when (res) {
            is Success -> {
                ResponseEntity.status(200).body(res.value)
            }
            is Failure -> {
                when (res.value) {
                    is RequestInfoError.RequestNotFound ->
                        Problem.response(404, Problem.requestNotFound)
                    is RequestInfoError.RequestCredentialIsRequired ->
                        Problem.response(400, Problem.requestCredentialIsRequired)
                }
            }
        }
    }

    @GetMapping(Uris.Requests.REQUESTS_SEARCH)
    fun searchRequests(
        @RequestParam("host") host: String,
        @RequestParam method: String?,
        @RequestParam path: String?,
        @RequestParam startDate: Long?,
        @RequestParam endDate: Long?,
    ): ResponseEntity<*> {
        val res = requestsServices.searchRequests(host, method, path, startDate, endDate)
        return when (res) {
            is Success -> {
                ResponseEntity.status(200).body(res.value)
            }
            is Failure -> {
                when (res.value) {
                    is SearchError.HostDoesNotExist ->
                        Problem.response(400, Problem.hostDoesNotExist)
                    is SearchError.InvalidDateRange ->
                        Problem.response(400, Problem.invalidDateRange)
                    is SearchError.InvalidMethod ->
                        Problem.response(400, Problem.invalidMethod)
                }
            }
        }
    }

}
