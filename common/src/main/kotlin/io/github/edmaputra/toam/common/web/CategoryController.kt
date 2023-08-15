package io.github.edmaputra.toam.common.web

import io.github.edmaputra.toam.common.entity.Category
import io.github.edmaputra.toam.common.service.CategoryService
import io.github.edmaputra.toam.common.web.request.CategoryCreateInput
import jakarta.validation.Valid
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

  @MutationMapping("createCategory")
  fun createCategory(@Argument @Valid input: CategoryCreateInput): Mono<Category> {
    return service.create(input)
  }

  @MutationMapping()
  fun deleteCategory(@Argument id: String): Mono<Category> {
    return service.delete(id)
  }
}
