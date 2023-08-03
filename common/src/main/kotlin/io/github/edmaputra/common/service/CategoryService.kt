package io.github.edmaputra.common.service

import io.github.edmaputra.common.entity.Category
import io.github.edmaputra.common.repository.CategoryTemplateRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CategoryService {

  fun findAll(page: Long?, size: Int?, sortBy: String?, isAsc: Boolean?, search: String?): Flux<Category>
  fun find(id: String): Mono<Category>
}

@Service
class CategoryServiceImpl(private val repository: CategoryTemplateRepository) : CategoryService {

  override fun findAll(page: Long?, size: Int?, sortBy: String?, isAsc: Boolean?, search: String?): Flux<Category> {
    return repository.findAll(page, size, sortBy, isAsc, search)
  }

  override fun find(id: String): Mono<Category> {
    return repository.findById(id)
  }
}
