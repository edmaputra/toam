package io.github.edmaputra.common.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table
data class Category(
  @Id val id: UUID?,
  val name: String,
  val description: String? = ""
)
