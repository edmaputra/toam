package io.github.edmaputra.toam.common.tests

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.edmaputra.common.ToamCommonApplication
import io.github.edmaputra.common.entity.Category
import io.github.edmaputra.common.repository.CategoryRepository
import io.github.edmaputra.toam.common.helper.TestHelper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
  classes = [ToamCommonApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryTest {

  @LocalServerPort
  private var serverPort: Int = 0

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  @Autowired
  private lateinit var repository: CategoryRepository

  private lateinit var webClient: WebTestClient

  @BeforeEach
  fun setup() {
    clear()

    webClient = WebTestClient.bindToServer()
      .baseUrl("http://localhost:$serverPort/graphql")
      .build()

    val categories = objectMapper.readValue(
      TestHelper.readJson("mock/db/category/data.json"),
      object : TypeReference<List<Category>>() {})
    repository.saveAll(categories).collectList().block()
  }

  @AfterEach
  fun tearDown() {
    clear()
  }

  fun clear() {
    repository.deleteAll().block()
  }

  @Test
  fun getAllCategories() {
    var query = """
           {
            categories {
                name
            }
           }
        """.trimIndent()

    var categories = HttpGraphQlTester.create(webClient)
      .document(query)
      .execute()
      .path("categories")
      .entityList(Category::class.java)
      .get()

    assertThat(categories)
      .extracting("name")
      .containsExactlyInAnyOrder("dessert", "drink", "beverages")

    query = """
           {
            categories {
                name, description
            }
           }
        """.trimIndent()

    categories = HttpGraphQlTester.create(webClient)
      .document(query)
      .execute()
      .path("categories")
      .entityList(Category::class.java)
      .get()

    assertThat(categories)
      .extracting("name", "description")
      .containsExactlyInAnyOrder(
        Tuple.tuple("dessert", "the dessert category"),
        Tuple.tuple("drink", "the drink category"),
        Tuple.tuple("beverages", "the beverages category")
      )
  }
}
