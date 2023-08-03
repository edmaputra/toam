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
    val categories = doSubmitRequest(
      "{ categories { name }}"
    )
    assertThat(categories)
      .extracting("name")
      .containsExactlyInAnyOrder("dessert", "drink", "beverages")

    doSubmitAndAssert(
      "{ categories { name, description }}",
      Tuple.tuple("dessert", "the dessert category"),
      Tuple.tuple("drink", "the drink category really good"),
      Tuple.tuple("beverages", "the beverages category")
    )
  }

  @Test
  fun `given get request with search, when submit, then return filtered list`() {
    doSubmitAndAssert(
      """
           {
            categories(search: "dessert") {
                name, description
            }
           }
        """.trimIndent(),
      Tuple.tuple("dessert", "the dessert category")
    )
  }

  fun doSubmitAndAssert(query: String, vararg tuples: Tuple) {
    val resultList = doSubmitRequest(query)
    assertThat(resultList)
      .extracting("name", "description")
      .containsExactlyInAnyOrder(*tuples)
  }

  fun doSubmitRequest(query: String): MutableList<Category> {
    return HttpGraphQlTester.create(webClient)
      .document(query)
      .execute()
      .path("categories")
      .entityList(Category::class.java)
      .get()
  }
}
