package io.github.edmaputra.toam.common.service

import io.github.edmaputra.toam.common.entity.Category
import io.github.edmaputra.toam.common.repository.CategoryTemplateRepository
import io.github.edmaputra.toam.common.web.request.CategoryCreateInput
import io.github.edmaputra.toam.common.web.request.CategoryUpdateInput
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CategoryService {

  fun findAll(page: Long?, size: Int?, sortBy: String?, isAsc: Boolean?, search: String?): Flux<Category>
  fun find(id: String): Mono<Category>
  fun create(input: CategoryCreateInput): Mono<Category>
  fun update(input: CategoryUpdateInput): Mono<Category>
  fun delete(id: String): Mono<Category>
}

@Service
class CategoryServiceImpl(private val repository: CategoryTemplateRepository) : CategoryService {

  override fun findAll(page: Long?, size: Int?, sortBy: String?, isAsc: Boolean?, search: String?): Flux<Category> {
    return repository.findAll(page, size, sortBy, isAsc, search)
  }

  override fun find(id: String): Mono<Category> {
    return repository.findById(id)
  }

  override fun create(input: CategoryCreateInput): Mono<Category> {
    val category = Category(null, input.name, input.description)
    return repository.save(category)
  }

  override fun update(input: CategoryUpdateInput): Mono<Category> {
    return find(input.id)
      .map { saved ->
        run {
          saved.name = input.name
          saved.description = input.description
          saved
        }
      }
      .flatMap { updated -> repository.update(updated) }

  }

  override fun delete(id: String) = repository.delete(id)
}
