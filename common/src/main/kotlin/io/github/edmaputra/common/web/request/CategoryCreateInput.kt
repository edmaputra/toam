package io.github.edmaputra.common.web.request

import jakarta.validation.constraints.NotBlank

data class CategoryCreateInput(
  @field:NotBlank(message = "must not be blank")
  var name: String,

  var description: String?
)
