package io.github.edmaputra.common.repository

import io.github.edmaputra.common.entity.Category
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface CategoryRepository : R2dbcRepository<Category, String>
