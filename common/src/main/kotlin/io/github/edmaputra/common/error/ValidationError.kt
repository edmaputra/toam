package io.github.edmaputra.common.error

data class ValidationError(
    val field: String,
    val message: String
)
