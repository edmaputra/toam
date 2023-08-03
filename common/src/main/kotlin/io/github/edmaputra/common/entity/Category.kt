package io.github.edmaputra.common.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class Category(
  @Id val id: String?,
  val name: String,
  val description: String? = ""
)
