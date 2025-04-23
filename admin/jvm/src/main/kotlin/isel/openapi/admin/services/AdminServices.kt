package isel.openapi.admin.services

import isel.openapi.admin.domain.RequestInfo
import isel.openapi.admin.repository.TransactionManager
import isel.openapi.admin.utils.Either
import isel.openapi.admin.utils.failure
import isel.openapi.admin.utils.success
import org.springframework.stereotype.Component

sealed interface RequestInfoError {
    data object RequestNotFound : RequestInfoError
    data object RequestCredentialNotFound : RequestInfoError
}

typealias RequestInfoResult = Either<RequestInfoError, List<RequestInfo>>

@Component
class AdminServices(
    private val transactionManager: TransactionManager,
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

}