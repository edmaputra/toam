package io.github.edmaputra.toam.common.helper

import org.springframework.core.io.ClassPathResource
import java.nio.file.Files

class TestHelper {
  companion object {
    fun readJson(file: String): String =
      String(Files.readAllBytes(ClassPathResource(file).file.toPath()))
  }
}
