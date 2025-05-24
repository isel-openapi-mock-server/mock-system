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

sealed interface CommitError {
    data object InvalidTransaction : CommitError
    data object HostDoesNotExist : CommitError
}

typealias CommitResult = Either<CommitError, Boolean>

sealed interface SaveScenarioError {
    data object PathOperationDoesNotExist : SaveScenarioError
    data object InvalidResponseContent : SaveScenarioError
}

typealias SaveScenarioResult = Either<SaveScenarioError, String>

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


    /**
     * Retorna o token da transaçao, se o transactionToken == null cria um novo, se não retorna o mesmo. Só retorna token se não tiver erros.
     */
    fun saveSpec(
        openApiSpec: String,
        host: String? = null,
        transactionToken: String?,
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

        //val transToken = transactionToken ?: TODO() //funçao para criar o novo token
        /*
        if (host == null) gerar novo host
        if (transactionToken == null) gerar novo token
        adicionar a spec à DB.
        */
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

        router.register(apiSpec, currentHost) // TODO acho que aqui nao muda nada com o transactionToken

        return success(currentHost)
    }

    /**
     * Retorna o token da transaçao, se o transactionToken == null cria um novo, se não retorna o mesmo. Só retorna token se não tiver erros.
     */
    fun saveResponseConfig(host: String, scenario: Scenario, transactionToken: String?): SaveScenarioResult {
        val responseValidator = router.match(host, scenario.method, scenario.path) ?: return failure(SaveScenarioError.PathOperationDoesNotExist) //erro que nao existe aquele path/method
        scenario.responses.forEach { response ->
            val respValRes = responseValidator.validateResponse(response)
            // TODO nao seria assim, ter a lista de erros de validaçao, para depois guarda-los na DB
            if (respValRes.isNotEmpty()) {
                // Guardar as falhas na DB ou retornar as falhas?
                return failure(SaveScenarioError.InvalidResponseContent)
            }
        }
        TODO("Not yet implemented") // se ainda nao teve erros, guardar, se teve, guardar os erros de validaçao, ver o transactionToken
    }

    fun commitChanges(
        host: String?,
        transaction: String,
    ) : CommitResult {
        transactionManager.run {
            val transactionsRepository = it.transactionsRepository

            if(!transactionsRepository.isTransactionActive(transaction)) {
                return@run failure(CommitError.InvalidTransaction)
            }

            if(host != null && !transactionsRepository.isHostExists(host)) {
                return@run failure(CommitError.HostDoesNotExist)
            }

            val newHost = adminDomain.generateHost()

            transactionsRepository.commitTransaction(
                transaction,
                newHost,
            )
        }

        return success(true)

    }

}