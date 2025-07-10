package isel.openapi.admin.services

import isel.openapi.admin.domain.requests.RequestInfo
import isel.openapi.admin.parsing.model.HttpMethod
import isel.openapi.admin.repository.TransactionManager
import isel.openapi.admin.utils.Either
import isel.openapi.admin.utils.failure
import isel.openapi.admin.utils.success
import org.springframework.stereotype.Component

sealed interface RequestInfoError {
    data object RequestNotFound : RequestInfoError
    data object RequestCredentialIsRequired : RequestInfoError
}

typealias RequestInfoResult = Either<RequestInfoError, List<RequestInfo>>

sealed interface SearchError {
    data object HostDoesNotExist : SearchError
    data object InvalidDateRange : SearchError
    data object InvalidMethod : SearchError
}

typealias SearchResult = Either<SearchError, List<RequestInfo>>

@Component
class RequestsServices(
    private val transactionManager: TransactionManager,
) {

    /**
     * Gera uma lista de RequestInfo com base na chave de troca ou chave externa.
     * @param exchangeKey Chave de troca do pedido.
     * @param externalKey Chave externa do pedido.
     * @return Uma lista de RequestInfo se encontrado, ou um erro se não encontrado ou se as credenciais forem inválidas.
     */
    fun getRequestInfo(
        exchangeKey: String?,
        externalKey: String?
    ) : RequestInfoResult {

        if( exchangeKey == null && externalKey == null) {
            return failure(RequestInfoError.RequestCredentialIsRequired)
        }

        return transactionManager.run {
            val requestsRepository = it.requestsRepository

            val requests = when {
                exchangeKey != null -> {
                    listOf(requestsRepository.getRequestInfoExchangeKey(exchangeKey)
                        ?: return@run failure(RequestInfoError.RequestNotFound))
                }
                externalKey != null -> {
                    requestsRepository.getRequestInfoExternalKey(externalKey)
                }
                else -> return@run failure(RequestInfoError.RequestCredentialIsRequired)
            }
            success(requests)
        }
    }

    /**
     * Procura por pedidos com base nos critérios fornecidos.
     * @param host O host do pedido.
     * @param method O método HTTP do pedido (opcional).
     * @param path O caminho do pedido (opcional).
     * @param startDate Data de início do intervalo de pesquisa (opcional).
     * @param endDate Data de fim do intervalo de pesquisa (opcional).
     * @return Uma lista de RequestInfo que correspondem aos critérios de pesquisa, ou um erro se o host não existir, o intervalo de datas for inválido ou o método HTTP for inválido.
     */
    fun searchRequests(
        host: String,
        method: String?,
        path: String?,
        startDate: Long?,
        endDate: Long?
    ) : SearchResult {

        if(startDate != null && endDate != null && startDate > endDate) {
            return failure(SearchError.InvalidDateRange)
        }

        if (!method.isNullOrBlank()) {
            try {
                HttpMethod.valueOf(method.uppercase())
            } catch (e: IllegalArgumentException) {
                return failure(SearchError.InvalidMethod)
            }
        } else {
            return failure(SearchError.InvalidMethod)
        }


        return transactionManager.run {
            val requestsRepository = it.requestsRepository
            val adminRepository = it.adminRepository

            adminRepository.getSpecId(host) ?: return@run failure(SearchError.HostDoesNotExist)

            val requests = requestsRepository.searchRequests(
                host,
                method,
                path,
                startDate,
                endDate
            )
            success(requests)
        }
    }
}