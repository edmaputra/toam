package io.github.edmaputra.common.web

import io.github.edmaputra.common.entity.Category
import io.github.edmaputra.common.web.request.CategoryInput
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class CategoryController {

    @QueryMapping
    fun categoriesTest(): Flux<Category> {
        return Flux.just(Category("", "test"))
    }

    @MutationMapping
    fun create(@Argument("input") input: CategoryInput): Mono<Category> {
        return Mono.empty()
    }
}
