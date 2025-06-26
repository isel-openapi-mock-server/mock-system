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
import org.postgresql.util.PGobject

class JdbiProblemsRepository(
    private val handle: Handle,
) : ProblemsRepository {

    override fun addRequest(uuid: String, pathTemplate: String, method: String, resolvedPath: String, externalKey: String?, host: String, headers: String?, date: Long) {

        val transactionToken = handle.createQuery(
            """
            SELECT uuid FROM open_transactions WHERE host = :host
            """
        )
            .bind("host", host)
            .mapTo<String>()
            .first()

        val specId = handle.createQuery(
            """
            SELECT id FROM specs WHERE transaction_token = :transactionToken
            """
        )
            .bind("transactionToken", transactionToken)
            .mapTo<Int>()
            .first()

        handle.createUpdate("INSERT INTO requests (uuid, path_template, method, resolved_path, external_key, host, spec_id, headers, date) VALUES (:uuid, :url, :method, :path, :externalKey, :host, :specId, :headers, :date)")
            .bind("uuid", uuid)
            .bind("path_template", pathTemplate)
            .bind("method", method)
            .bind("resolved_path", resolvedPath)
            .bind("externalKey", externalKey)
            .bind("host", host)
            .bind("specId", specId)
            .bind("headers", jsonb(headers))
            .bind("date", date)
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

    override fun addResponse(uuid: String, statusCode: Int, headers: String?): Int {
        return handle.createUpdate("INSERT INTO responses (uuid, status_code, headers) VALUES (:uuid, :statusCode, :headers)")
            .bind("uuid", uuid)
            .bind("statusCode", statusCode)
            .bind("headers", jsonb(headers))
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun addResponseBody(id: Int, body: ByteArray, contentType: String) {
        handle.createUpdate("INSERT INTO response_body (response_id, content, content_type) VALUES (:response_id, :content, :contentType)")
            .bind("response_id", id)
            .bind("content", body)
            .bind("contentType", contentType)
            .execute()
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

    private fun jsonb(value: String?): PGobject? {
        if(value == null) {
            return null
        }
        return PGobject().apply {
            type = "jsonb"
            setValue(value)
        }
    }

}