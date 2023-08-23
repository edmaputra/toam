package io.github.edmaputra.toam.common.web.request

import jakarta.validation.constraints.NotBlank

data class CategoryUpdateInput(

  @field:NotBlank(message = "must not be blank")
  var id: String,

  @field:NotBlank(message = "must not be blank")
  var name: String,

  var description: String?
)
