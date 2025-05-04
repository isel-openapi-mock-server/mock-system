package isel.openapi.admin.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import isel.openapi.admin.domain.AdminDomain
import isel.openapi.admin.domain.RequestInfo
import isel.openapi.admin.http.model.Scenario
import isel.openapi.admin.parsingServices.Parsing
import isel.openapi.admin.repository.TransactionManager
import isel.openapi.admin.utils.Either
import isel.openapi.admin.utils.failure
import isel.openapi.admin.utils.success
import org.springframework.stereotype.Component
import kotlinx.datetime.Clock

sealed interface RequestInfoError {
    data object RequestNotFound : RequestInfoError
    data object RequestCredentialNotFound : RequestInfoError
}

typealias RequestInfoResult = Either<RequestInfoError, List<RequestInfo>>

sealed interface CreateSpecError {
    data object InvalidOpenApiSpec : CreateSpecError
    data object HostDoesNotExist : CreateSpecError
}

typealias CreateSpecResult = Either<CreateSpecError, String>

@Component
class AdminServices(
    private val parsing: Parsing,
    private val transactionManager: TransactionManager,
    private val adminDomain: AdminDomain,
    private val router: NotRouter,
) {

    fun getRequestInfo(
        uuid: String?,
        externalKey: String?
    ) : RequestInfoResult {
        return transactionManager.run {

            val adminRepository = it.adminRepository

            val requests = if(uuid != null) {
                listOf(adminRepository.getRequestInfoUUID(uuid) ?: return@run failure(RequestInfoError.RequestNotFound))
            } else if (externalKey != null) {
                adminRepository.getRequestInfoExternalKey(externalKey)
            } else return@run failure(RequestInfoError.RequestCredentialNotFound)

            if(requests.isEmpty()) return@run failure(RequestInfoError.RequestNotFound)

            success(requests)

        }
    }

    fun saveSpec(
        openApiSpec: String,
        host: String? = null
    ) : CreateSpecResult {

        if(!parsing.validateOpenApi(openApiSpec)) {
            failure(CreateSpecError.InvalidOpenApiSpec)
        }
        val openApi = parsing.parseOpenApi(openApiSpec)
            ?: return failure(CreateSpecError.InvalidOpenApiSpec)

        val apiSpec = parsing.extractApiSpec(openApi)
        var currentHost = host ?: adminDomain.generateHost()

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()

        transactionManager.run {
            val adminRepository = it.adminRepository
            if(host == null) {
                while(adminRepository.getSpecId(currentHost) != null) {
                    currentHost = adminDomain.generateHost()
                }
                val specId = adminRepository.addAPISpec(apiSpec.name, apiSpec.description, currentHost)
                for (path in apiSpec.paths) {
                    val operationsJson = mapper.writeValueAsString(path.operations)
                    adminRepository.addPath(specId, path.fullPath, operationsJson)
                }
            } else {
                val specId = adminRepository.getSpecId(currentHost)
                    ?: return@run failure(CreateSpecError.HostDoesNotExist)
                adminRepository.updateAPISpec(specId, apiSpec.name, apiSpec.description)
                for (path in apiSpec.paths) {
                    val operationsJson = mapper.writeValueAsString(path.operations)
                    adminRepository.addPath(specId, path.fullPath, operationsJson)
                }
            }
        }

        router.register(apiSpec, currentHost)

        return success(currentHost)
    }

    fun saveResponseConfig(host: String, accessToken: String, scenario: Scenario) {
        scenario.responses.forEach { response ->
            val responseValidator = router.match(host, response.method, response.path) ?: return TODO() //erro que nao existe aquele path/method
            responseValidator.validateResponse(response)
        }
        TODO("Not yet implemented")
    }


}