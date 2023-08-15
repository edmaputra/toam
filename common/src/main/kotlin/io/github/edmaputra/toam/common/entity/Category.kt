package io.github.edmaputra.toam.common.entity

import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table
data class Category(
  @Id val id: UUID?,
  @field:NotBlank(message = "Name must not be blank") val name: String,
  val description: String? = ""
)
