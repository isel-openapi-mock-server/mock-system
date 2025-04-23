package isel.openapi.admin.parsingServices.model

sealed interface Type {
    data object NullType : Type
    data object BooleanType : Type
    data class ObjectType(val fieldsTypes: Map<String, Type>) : Type
    data class ArrayType(val elementsType: Type) : Type
    data object NumberType : Type
    data object StringType : Type
    data object IntegerType : Type
    data object UnknownType : Type
}