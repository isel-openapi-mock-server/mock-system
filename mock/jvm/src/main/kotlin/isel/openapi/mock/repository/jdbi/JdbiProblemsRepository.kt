package isel.openapi.mock.repository.jdbi

import isel.openapi.mock.domain.problems.ParameterInfo
import isel.openapi.mock.domain.problems.Problems
import isel.openapi.mock.http.VerificationError
import isel.openapi.mock.http.VerifyBodyError
import isel.openapi.mock.http.VerifyHeadersError
import isel.openapi.mock.http.VerifyParamsError
import org.jdbi.v3.core.kotlin.mapTo
import isel.openapi.mock.repository.ProblemsRepository
import org.jdbi.v3.core.Handle

class JdbiProblemsRepository(
    private val handle: Handle,
) : ProblemsRepository {

    override fun addRequest(uuid: String, url: String, method: String, path: String, externalKey: String, host: String) {
        handle.createUpdate("INSERT INTO requests (uuid, url, method, path, external_key, host) VALUES (:uuid, :url, :method, :path, :externalKey, :host)")
            .bind("uuid", uuid)
            .bind("url", url)
            .bind("method", method)
            .bind("path", path)
            .bind("externalKey", externalKey)
            .bind("host", host)
            .execute()
    }

    override fun addRequestParams(uuid: String, params: List<ParameterInfo>) {
        params.forEach { param ->
            handle.createUpdate("INSERT INTO request_params (uuid, name, content, location, type) VALUES (:uuid, :name, :content, :location, :type)")
                .bind("uuid", uuid)
                .bind("name", param.name)
                .bind("content", param.content)
                .bind("location", param.location.locationToString())
                .bind("type", param.typeToString())
                .execute()
        }
    }

    override fun addRequestBody(uuid: String, body: ByteArray, contentType: String) {
        handle.createUpdate("INSERT INTO request_body (uuid, content, content_type) VALUES (:uuid, :content, :contentType)")
            .bind("uuid", uuid)
            .bind("content", body)
            .bind("contentType", contentType)
            .execute()
    }

    override fun addProblems(uuid: String, problems: List<VerificationError>) {
        problems.forEach { problem ->
            handle.createUpdate("INSERT INTO problems (uuid, description, type) VALUES (:uuid, :description, :type)")
                .bind("uuid", uuid)
                .bind("description", problemsToString(problem))
                .bind("type", problem::class.simpleName)
                .execute()
        }
    }

    override fun addResponse(uuid: String, statusCode: Int): Int {
        return handle.createUpdate("INSERT INTO responses (uuid, status_code) VALUES (:uuid, :statusCode)")
            .bind("uuid", uuid)
            .bind("statusCode", statusCode)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun addResponseHeaders(id:Int, headers: Map<String, String>) {
        headers.forEach { (key, value) ->
            handle.createUpdate("INSERT INTO response_headers (response_id, name, content) VALUES (:uuid, :name, :content)")
                .bind("response_id", id)
                .bind("name", key)
                .bind("value", value)
                .execute()
        }
    }

    override fun addResponseBody(id: Int, body: ByteArray, contentType: String) {
        handle.createUpdate("INSERT INTO response_body (response_id, content, content_type) VALUES (:uuid, :content, :contentType)")
            .bind("response_id", id)
            .bind("content", body.toString())
            .bind("contentType", contentType)
            .execute()
    }

    override fun addRequestHeaders(uuid: String, headers: Map<String, String>) {
        headers.forEach { (key, value) ->
            if(value == "") return@forEach
            handle.createUpdate("INSERT INTO request_headers (uuid, name, content) VALUES (:uuid, :name, :content)")
                .bind("uuid", uuid)
                .bind("name", key)
                .bind("content", value)
                .execute()
        }
    }

    private fun problemsToString(problem: VerificationError): String {
        return when (problem) {
            is VerifyBodyError.InvalidBodyFormat -> Problems.Body.invalidBodyFormat(problem)
            is VerifyHeadersError.InvalidType -> Problems.Header.invalidType(problem)
            is VerifyHeadersError.InvalidContentType -> Problems.Header.invalidContent(problem)
            is VerifyHeadersError.MissingHeader -> Problems.Header.missing(problem)
            is VerifyHeadersError.MissingHeaderContent -> Problems.Header.missingContent(problem)
            is VerifyParamsError.InvalidType -> Problems.Parameter.invalidType(problem)
            is VerifyParamsError.ParamCantBeEmpty -> Problems.Parameter.paramCantBeEmpty(problem)
            is VerifyParamsError.InvalidParam -> Problems.Parameter.invalidParam(problem)
            is VerifyParamsError.MissingParam -> Problems.Parameter.missingParam(problem)
            else -> "Unknown problem"
        }
    }


}