package io.github.edmaputra.common.web

import io.github.edmaputra.common.entity.Category
import io.github.edmaputra.common.service.CategoryService
import io.github.edmaputra.common.web.request.CategoryInput
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class CategoryController(private val service: CategoryService) {

  @QueryMapping
  fun categories(
    @Argument("page") page: Long?,
    @Argument("size") size: Int?,
    @Argument("sortBy") sortBy: String?,
    @Argument("isAsc") isAsc: Boolean?,
    @Argument("search") keyword: String?
  ): Flux<Category> {
    return service.findAll(page, size, sortBy, isAsc, keyword)
  }

  @QueryMapping
  fun category(@Argument id: String): Mono<Category> = service.find(id)

  @MutationMapping
  fun create(@Argument("input") input: CategoryInput): Mono<Category> {
    return Mono.empty()
  }
}
