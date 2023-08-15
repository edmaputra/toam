package io.github.edmaputra.toam.common.repository

import io.github.edmaputra.toam.common.entity.Category
import org.springframework.data.r2dbc.repository.R2dbcRepository
import java.util.UUID

interface CategoryRepository : R2dbcRepository<Category, UUID>
