package io.github.edmaputra.common.repository

import io.github.edmaputra.common.entity.Category
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface CategoryRepository : ReactiveCrudRepository<Category, String>,
  ReactiveQueryByExampleExecutor<Category>
