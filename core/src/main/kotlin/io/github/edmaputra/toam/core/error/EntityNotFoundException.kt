package io.github.edmaputra.toam.core.error

class EntityNotFoundException(id: String) : RuntimeException("Data not exists. Id: $id")
