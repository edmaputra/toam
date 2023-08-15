package io.github.edmaputra.toam.common.error

data class ValidationError(
    val field: String,
    val message: String
)
