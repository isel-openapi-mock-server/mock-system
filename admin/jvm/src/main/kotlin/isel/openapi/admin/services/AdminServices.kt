package isel.openapi.admin.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import isel.openapi.admin.domain.admin.AdminDomain
import isel.openapi.admin.domain.admin.VerifyResponseError
import isel.openapi.admin.http.model.Scenario
import isel.openapi.admin.parsing.Parsing
import isel.openapi.admin.parsing.model.ApiPath
import isel.openapi.admin.parsing.model.ApiSpec
import isel.openapi.admin.parsing.model.HttpMethod
import isel.openapi.admin.parsing.model.PathOperation
import isel.openapi.admin.repository.TransactionManager
import isel.openapi.admin.utils.Either
import isel.openapi.admin.utils.Failure
import isel.openapi.admin.utils.failure
import isel.openapi.admin.utils.success
import org.springframework.stereotype.Component
import kotlinx.datetime.Clock

sealed interface CreateSpecError {
    data object InvalidOpenApiSpec : CreateSpecError
}

typealias CreateSpecResult = Either<CreateSpecError, String>

sealed interface SaveScenarioError {
    data object TransactionOrHostNotProvided : SaveScenarioError
    data object PathOperationDoesNotExist : SaveScenarioError
    data class InvalidResponseContent(val fails: List<VerifyResponseError>) : SaveScenarioError
    data object InvalidTransaction : SaveScenarioError
    data object HostDoesNotExist : SaveScenarioError
}

typealias SaveScenarioResult = Either<SaveScenarioError, String>

sealed interface CommitError {
    data object InvalidTransaction : CommitError
    data object HostDoesNotExist : CommitError
}

typealias CommitResult = Either<CommitError, String>

@Component
class AdminServices(
    private val parsing: Parsing,
    private val transactionManager: TransactionManager,
    private val adminDomain: AdminDomain,
    private val router: RouteValidatorResolver,
    private val clock: Clock
) {

    /**
     * Salva uma nova especificação OpenAPI no sistema.
     * @param openApiSpec A especificação OpenAPI em formato String.
     * @return Um resultado que contém o token de transação se a operação for bem-sucedida, ou um erro caso contrário.
     */
    fun saveNewSpec(
        openApiSpec: String,
    ) : CreateSpecResult {

        val apiSpec = parseOpenApiSpec(openApiSpec)
            ?: return failure(CreateSpecError.InvalidOpenApiSpec)

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()

        var transactionToken = adminDomain.generateTokenValue()

        transactionManager.run {
            val adminRepository = it.adminRepository
            val transactionsRepository = it.transactionsRepository

            //Verifica se o transactionToken não existe ainda
            while(transactionsRepository.isTransactionActive(transactionToken)) {
                transactionToken = adminDomain.generateTokenValue()
            }

            val specId = adminRepository.addAPISpec(apiSpec.name, apiSpec.description, transactionToken)

            transactionsRepository.addNewTransaction(
                transactionToken,
                specId,
                null,
                clock.now().epochSeconds
            )

            for (path in apiSpec.paths) {
                val operationsJson = mapper.writeValueAsString(path.operations)
                adminRepository.addPath(specId, path.fullPath, operationsJson)
            }

        }
        // Regista a especificação no router
        router.register(apiSpec, transactionToken)
        return success(transactionToken)
    }

    /**
     * Salva a configuração de resposta para um cenário específico.
     * @param host O host para o qual a configuração de resposta será salva.
     * @param scenario O cenário contendo as respostas a serem salvas.
     * @param transactionToken O token da transação, se existir. Se for nulo, cria um novo token.
     * @return Um resultado que contém o token de transação atualizado se a operação for bem-sucedida, ou um erro caso contrário.
     */
    fun saveResponseConfig(
        host: String?,
        scenario: Scenario,
        transactionToken: String?
    ): SaveScenarioResult {

        var token = transactionToken ?:
            adminDomain.generateTokenValue()

        if(transactionToken == null && host == null) {
            return failure(SaveScenarioError.TransactionOrHostNotProvided)
        }

        var currentSpecId: Int = -1

        if (transactionToken != null) {
            val isActive = transactionManager.run {
                val transactionsRepository = it.transactionsRepository
                transactionsRepository.isTransactionActive(transactionToken)
            }

            if (!isActive) {
                return failure(SaveScenarioError.InvalidTransaction)
            }
        } else {
            val spec = getApiSpec(host!!)
                ?: return failure(SaveScenarioError.HostDoesNotExist)

            transactionManager.run {
                val transactionsRepository = it.transactionsRepository
                val adminRepository = it.adminRepository
                while(transactionsRepository.isTransactionActive(token)) {
                    token = adminDomain.generateTokenValue()
                }

                // Copia a especificação para a transação
                val specId = adminRepository.getSpecId(host) ?:
                    return@run failure(SaveScenarioError.HostDoesNotExist)

                //Cria uma nova transação
                currentSpecId = transactionsRepository.copySpecToTransaction(token, specId)
                transactionsRepository.addNewTransaction(token, currentSpecId, host, clock.now().epochSeconds)

            }
            router.register(spec, token)
        }

        val httpMethod = HttpMethod.valueOf(scenario.method.uppercase())

        val responseValidator = router.match(token, httpMethod, scenario.path)
            ?: return failure(SaveScenarioError.PathOperationDoesNotExist)

        scenario.responses.forEach { response ->
            val respValRes = responseValidator.validateResponse(response)
            if (respValRes.isNotEmpty()) {
                return failure(SaveScenarioError.InvalidResponseContent(respValRes))
            }
        }

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()

        transactionManager.run {
            val transactionsRepository = it.transactionsRepository

            //Verifica se já existe um cenário com o mesmo nome e se sim apaga-o
            if(transactionsRepository.getScenarioNameByTransaction(token) != null) {
                transactionsRepository.deleteScenario(token, scenario.name)
            }

            if(currentSpecId == -1) {
                currentSpecId = transactionsRepository.getSpecIdByTransaction(token)
                    ?: return@run failure(SaveScenarioError.InvalidTransaction)
            }

            // Regista o cenário no repositório
            transactionsRepository.addScenario(token, scenario.name, scenario.method, scenario.path, currentSpecId, clock.now().epochSeconds)

            // Regista as respostas no repositório
            for (i in scenario.responses.indices) {

                //Converte os headers para JSON
                val jsonHeaders =
                    if(scenario.responses[i].headers != null) mapper.writeValueAsString(scenario.responses[i].headers)
                    else null

                transactionsRepository.addScenarioResponse(
                    token,
                    scenario.name,
                    i,
                    scenario.responses[i].statusCode.toString(),
                    scenario.responses[i].body?.toByteArray(),
                    jsonHeaders,
                    scenario.responses[i].contentType,
                    currentSpecId
                )
            }
        }
        return success(token)
    }

    /**
     * Confirma as alterações feitas em uma transação, associando-a a um host específico.
     * @param host O host ao qual as alterações serão aplicadas. Se nulo, um novo host será gerado.
     * @param transactionToken O token da transação a ser confirmada.
     * @return Um resultado que contém o host associado à transação se a operação for bem-sucedida, ou um erro caso contrário.
     */
    fun commitChanges(
        host: String?,
        transactionToken: String,
    ) : CommitResult {

        var currentHost = host ?: ""

        val result = transactionManager.run {
            val transactionsRepository = it.transactionsRepository

            if(!transactionsRepository.isTransactionActive(transactionToken)) {
                return@run failure(CommitError.InvalidTransaction)
            }

            if(host != null && !transactionsRepository.isHostExists(host)) {
                return@run failure(CommitError.HostDoesNotExist)
            }

            if(host == null) {
                val isHostRegistered = transactionsRepository.getHostByTransactionToken(transactionToken)
                if(isHostRegistered != null) {
                    // Se o host já está registado, usa-o
                    currentHost = isHostRegistered
                } else {
                    // Se não está registado, gera um novo host
                    currentHost = adminDomain.generateHost()
                    while(transactionsRepository.isHostExists(currentHost)) {
                        currentHost = adminDomain.generateHost()
                    }
                }
            }
            transactionsRepository.commitTransaction(transactionToken, currentHost)
        }
        when(result) {
            is Failure<*> -> {
                return when (result.value) {
                    is CommitError.InvalidTransaction -> failure(CommitError.InvalidTransaction)
                    is CommitError.HostDoesNotExist -> failure(CommitError.HostDoesNotExist)
                    else -> failure(CommitError.InvalidTransaction)
                }
            }

            else -> {
                router.remove(transactionToken)
                return success(currentHost)
            }
        }
    }

    fun deleteTransactions() : Int {
        return transactionManager.run {
            val transactionsRepository = it.transactionsRepository

            val transactionsToDelete = transactionsRepository.getTransactionsToDelete(
                clock.now().epochSeconds
            )

            transactionsToDelete.forEach { transaction ->
                // Remove o token do router
                router.remove(transaction)

                // Remove o cenário associado ao token
                transactionsRepository.deleteScenarioFromTransaction(transaction)

                // Remove a especificação associada ao token
                transactionsRepository.deleteSpecFromTransaction(transaction)

                // Remove a transação
                transactionsRepository.deleteTransaction(transaction)
            }

            return@run transactionsToDelete.size
        }
    }

    /**
     * Valida e extrai uma especificação OpenAPI a partir de uma string.
     * @param openApiSpec A especificação OpenAPI em formato String.
     * @return Um objeto ApiSpec se a especificação for válida, ou null caso contrário.
     */
    private fun parseOpenApiSpec(openApiSpec: String): ApiSpec? {
        if(!parsing.validateOpenApi(openApiSpec)) {
            return null
        }
        val openApi = parsing.parseOpenApi(openApiSpec)
            ?: return null

        return parsing.extractApiSpec(openApi)
    }

    /**
     * Obtém a especificação da API associada a um host específico.
     * @param host O host para o qual a especificação da API será recuperada.
     * @return Um objeto ApiSpec contendo a especificação da API, ou null se não for encontrada.
     */
    private fun getApiSpec(host: String): ApiSpec? {

        val spec = transactionManager.run {
            val adminRepository = it.adminRepository
            val transactionRepository = it.transactionsRepository

            val transactionToken = transactionRepository.getTransactionByHost(host)
                ?: return@run null

            adminRepository.getApiSpecByTransactionToken(transactionToken)
        } ?: return null

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()

        val paths = mutableListOf<ApiPath>()

        spec.paths.forEach { apiPath ->

            val operations: List<PathOperation> =
                mapper.readValue(apiPath.operations, object : TypeReference<List<PathOperation>>() {})

            paths.add(
                ApiPath(
                    fullPath = apiPath.path,
                    operations = operations,
                    path = parsing.splitPath(apiPath.path)
                )
            )

        }

        return ApiSpec(
            name = spec.name,
            description = spec.description,
            paths = paths
        )
    }

}