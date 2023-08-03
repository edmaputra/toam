package io.github.edmaputra.common.repository

import io.github.edmaputra.common.entity.Category
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CategoryTemplateRepository {
  fun findAll(page: Long?, size: Int?, sortBy: String?, isAsc: Boolean?, keyWords: String?): Flux<Category>
  fun findById(id: String): Mono<Category>
}

@Component
class CategoryTemplateRepositoryImpl(private val template: R2dbcEntityTemplate) : CategoryTemplateRepository {

  override fun findAll(page: Long?, size: Int?, sortBy: String?, isAsc: Boolean?, keyWords: String?): Flux<Category> {
    val pageable = PageRequest.of((page ?: 0).toInt(), size ?: 10)
      .withSort(constructSort(sortBy, isAsc))

    return template.select(Category::class.java)
      .matching(
        Query
          .query(constructCriteria(keyWords))
          .with(pageable)
      ).all()
  }

  fun constructCriteria(keyWords: String?): CriteriaDefinition {
    if (keyWords == null) {
      return CriteriaDefinition.empty()
    }

    return where("name").like(keyWords)
      .or(where("description").like(keyWords))
  }

  fun constructSort(sortBy: String?, isAsc: Boolean?): Sort {
    val temp: Boolean = isAsc ?: false

    if (sortBy == null) {
      return Sort.unsorted()
    }

    return Sort.by(if (temp) Sort.Direction.ASC else Sort.Direction.DESC, sortBy)
  }

  override fun findById(id: String): Mono<Category> {
    return template.selectOne(Query.query(where("id").like(id)), Category::class.java)
  }
}
