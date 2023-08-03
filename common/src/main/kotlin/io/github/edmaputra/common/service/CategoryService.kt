package io.github.edmaputra.common.service

import io.github.edmaputra.common.entity.Category
import io.github.edmaputra.common.repository.CategoryRepository
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CategoryService {

  fun findAll(search: String?): Flux<Category>
  fun find(id: String): Mono<Category>
}

@Service
class CategoryServiceImpl(private val repository: CategoryRepository) : CategoryService {

  override fun findAll(search: String?): Flux<Category> {
    return repository.findAll(
      Example.of(
        Category(null, search.orEmpty(), search.orEmpty()),
        ExampleMatcher.matchingAny()
          .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
          .withMatcher("description", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
      )
    )
  }

  override fun find(id: String): Mono<Category> {
    return repository.findById(id)
  }
}
