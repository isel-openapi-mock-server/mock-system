package isel.openapi.mock.domain.problems

import isel.openapi.mock.http.VerifyBodyError
import isel.openapi.mock.http.VerifyHeadersError
import isel.openapi.mock.http.VerifyParamsError

class Problems {

    object Body {
        fun invalidBodyFormat(p: VerifyBodyError.InvalidBodyFormat) = "Invalid body format: ${p.receivedBody} for content type ${p.expectedBodyType}"
    }

    object Header {
        fun invalidType(p: VerifyHeadersError.InvalidType) = "Invalid header type: ${p.headerKey} ${p.receivedType} for content type ${p.expectedType}"
        fun invalidContent(p: VerifyHeadersError.InvalidContentType) = "Expected header type: ${p.expectedType} but received ${p.receivedType}"
        fun missing(p: VerifyHeadersError.MissingHeader) = "Missing header: ${p.expectedHeader}"
        fun missingContent(p: VerifyHeadersError.MissingHeaderContent) = "Missing header content: ${p.headerKey}"
    }

    object Parameter {
        fun invalidType(p: VerifyParamsError.InvalidType) = "Invalid parameter type: ${p.expectedType} for parameter ${p.name} in ${p.location.locationToString()}"
        fun paramCantBeEmpty(p: VerifyParamsError.ParamCantBeEmpty) = "Parameter ${p.paramName} in ${p.location.locationToString()} cannot be empty"
        fun invalidParam(p: VerifyParamsError.InvalidParam) = "Invalid parameter: ${p.paramName} in ${p.location.locationToString()}"
        fun missingParam(p: VerifyParamsError.MissingParam) = "Missing parameter: ${p.paramName} in ${p.location.locationToString()}"
    }

}