package isel.openapi.mock.services

import isel.openapi.mock.domain.openAPI.HttpMethod
import isel.openapi.mock.domain.openAPI.StatusCode

data class ResponseConfig(
    val statusCode: StatusCode,
    val contentType: String?,
    val headers: Map<String, String>?,
    val body: ByteArray?, // TODO nao sei se deve ser deste tipo
)

class Scenario(
    val name: String,
    //val method: HttpMethod,
    //val path: String,
    private val responses: List<ResponseConfig>
) {
    private var state = 0

    // Para ser chamado no handle de uma operaçao
    fun getResponse(): ResponseConfig {
        val response = responses[state]

        if (state == responses.lastIndex)
            state = 0
        else
            state++

        return response
    }

    // Ver se existe alguma resposta para uma rota-metodo neste scenario. Respostas para varias rotas diferentes
    //fun responseForPathMethod(path: String, method: HttpMethod): Boolean = responses.any { it.path == path && it.method == method }



    /*Acho que isto não é necessário, lidar com concorrencia, a nao ser que sejam feitos varios pedidos iguais para o mesmo scenario ao mesmo tempo e o state seja
    incrementado varias vezes, ou se 2 pedidos, o 1º do 1º estado e o 2º do 2º estado, o 2º pode obter o valor de state para ir buscar a resposta antes do 1º ter incrementado
    o state, assim deveria dar mas nao dá porque o state está desatualizado. TODO perguntar ao prof
    private val state2 = AtomicInteger(0)

    fun getResponse2(path: String, method: HttpMethod): ResponseConfig? {
        while (true) {
            val expectedState = state2.get()
            val response = responses[expectedState]

            if (response.method == method && response.path == path) {
                val newState = if (expectedState == responses.lastIndex) 0 else expectedState+1
                if (state2.compareAndSet(expectedState, newState))
                    return response
            }
            else
                return null
        }
    }
    */
}