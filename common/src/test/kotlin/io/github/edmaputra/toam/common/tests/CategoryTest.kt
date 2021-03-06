package io.github.edmaputra.toam.common.tests

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.ErrorType
import io.github.edmaputra.toam.common.ToamCommonApplication
import io.github.edmaputra.toam.common.VALIDATION_ERRORS
import io.github.edmaputra.toam.common.entity.Category
import io.github.edmaputra.toam.common.error.ValidationError
import io.github.edmaputra.toam.common.helper.TestHelper
import io.github.edmaputra.toam.common.repository.CategoryRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.graphql.ResponseError
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*
import java.util.stream.Collectors

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
  fun `given valid request, when submit, then return all list with expected field`() {
    val categories = doSubmitRequestAndReturnList(
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

  @Test
  fun `given get request with pagination, when submit, then return filtered list`() {
    doSubmitAndAssert(
      """
           {
            categories(page: 0, size: 1, sortBy: "name") {
                name, description
            }
           }
        """.trimIndent(),
      Tuple.tuple("drink", "the drink category really good"),
    )

    doSubmitAndAssert(
      """
           {
            categories(page: 0, size: 1, sortBy: "name", isAsc: true) {
                name, description
            }
           }
        """.trimIndent(),
      Tuple.tuple("beverages", "the beverages category")
    )

    doSubmitAndAssert(
      """
           {
            categories(page: 0, size: 2, sortBy: "name", isAsc: true) {
                name, description
            }
           }
        """.trimIndent(),
      Tuple.tuple("beverages", "the beverages category"),
      Tuple.tuple("dessert", "the dessert category"),
    )

    doSubmitAndAssert(
      """
           {
            categories(page: 1, size: 2, sortBy: "name", isAsc: true) {
                name, description
            }
           }
        """.trimIndent(),
      Tuple.tuple("drink", "the drink category really good")
    )
  }

  @Test
  fun `given get request with pagination and search, when submit, then return filtered list`() {
    doSubmitAndAssert(
      """
           {
            categories(search: "the d", page: 0, size: 2, sortBy: "name", isAsc: true) {
                name, description
            }
           }
        """.trimIndent(),
      Tuple.tuple("dessert", "the dessert category"),
      Tuple.tuple("drink", "the drink category really good")
    )
  }

  @Test
  fun `given valid id, when submit by id, then return one expected category`() {
    val beverages = getBeverages()

    val query = """
           {
            category(id: "${beverages?.id}") {
                id, name, description
            }
           }
        """.trimIndent()

    HttpGraphQlTester.create(webClient)
      .document(query)
      .execute()
      .path("category.id").entity(String::class.java).isEqualTo(beverages?.id.toString())
      .path("category.name").entity(String::class.java).isEqualTo("beverages")
      .path("category.description").entity(String::class.java).isEqualTo("the beverages category")
  }

  @Test
  fun `given random id, when submit by id, then expect not found`() {
    val randomId = UUID.randomUUID().toString()

    val query = """
           {
            category(id: "$randomId") {
                id, name, description
            }
           }
        """.trimIndent()

    HttpGraphQlTester.create(webClient)
      .document(query)
      .execute()
      .errors()
      .satisfy { err ->
        assertThat(err)
          .extracting(ResponseError::getErrorType)
          .contains(Tuple.tuple(ErrorType.DataFetchingException))

        assertThat(
          err.stream()
            .map { it.message }
            .collect(Collectors.toList())
        ).containsExactlyInAnyOrder("Data not exists. Id: $randomId")
      }
  }

  @Test
  fun `given valid create request, when submit for create, then category data is correctly created`() {
    val createMutation = """
      mutation {
        createCategory(input: {
          name: "test", description: "test category"
          }) {
            id, name, description
        }
      }
    """.trimIndent()

    doSubmitRequest(createMutation)
      .path("createCategory.id").entity(String::class.java)
      .matches { it.matches(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) }
      .path("createCategory.name").entity(String::class.java).isEqualTo("test")
      .path("createCategory.description").entity(String::class.java).isEqualTo("test category")
  }

  @Test
  fun `given invalid create request, when submit for create, then response error with expected message`() {
    val createMutation = """
      mutation {
        createCategory(input: {
          name: "", description: "test category"
          }) {
            id, name, description
        }
      }
    """.trimIndent()

    doSubmitRequest(createMutation)
      .errors()
      .satisfy { err ->
        assertValidationError(
          err,
          Tuple.tuple("name", "must not be blank")
        )
      }
  }

  @Test
  fun `given valid update request, when update, then expect updated one`() {
    val beverages = getBeverages()

    val updateMutation = """
      mutation {
        updateCategory(input: {
          id: "${beverages?.id}", name: "Beverage Fresh", description: "test fresh beverages category"
          }) {
            id, name, description
        }
      }
    """.trimIndent()

    doSubmitRequest(updateMutation)
      .path("updateCategory.id").entity(String::class.java).isEqualTo(beverages?.id.toString())
      .path("updateCategory.name").entity(String::class.java).isEqualTo("Beverage Fresh")
      .path("updateCategory.description").entity(String::class.java).isEqualTo("test fresh beverages category")
  }

  @Test
  fun `given random id, when update by id, then expect not found`() {
    val randomId = UUID.randomUUID().toString()

    val query = """
           mutation{
            updateCategory(input: {
          id: "$randomId", name: "Beverage Fresh", description: "test fresh beverages category"
          }) {
                id, name, description
            }
           }
        """.trimIndent()

    doSubmitRequest(query)
      .errors()
      .satisfy { err ->
        assertNotFound(err, randomId)
      }
  }

  @Test
  fun `given invalid update request, when submit for update, then response error with expected message`() {
    val createMutation = """
      mutation {
        updateCategory(input: {
          id: "", name: "", description: "test category"
          }) {
            id, name, description
        }
      }
    """.trimIndent()

    doSubmitRequest(createMutation)
      .errors()
      .satisfy { err ->
        assertValidationError(
          err,
          Tuple.tuple("id", "must not be blank"),
          Tuple.tuple("name", "must not be blank")
        )
      }
  }

  @Test
  fun `given valid delete request, when delete, then return deleted category`() {
    val beverages = getBeverages()

    val query = """
           mutation{
            deleteCategory(id: "${beverages?.id}") {
                id, name, description
            }
           }
        """.trimIndent()

    doSubmitRequest(query)
      .path("deleteCategory.id").entity(String::class.java).isEqualTo(beverages?.id.toString())
      .path("deleteCategory.name").entity(String::class.java).isEqualTo("beverages")
      .path("deleteCategory.description").entity(String::class.java).isEqualTo("the beverages category")

    val beverage = repository.findAll().collectList().block()
      ?.stream()
      ?.filter { it.name.equals("beverages", true) }
      ?.findFirst()

    assertThat(beverage).isNotNull
    assertThat(beverage?.get()?.deletedFlag).isTrue
  }

  @Test
  fun `given random id, when delete by id, then expect not found`() {
    val randomId = UUID.randomUUID().toString()

    val query = """
           mutation{
            deleteCategory(id: "$randomId") {
                id, name, description
            }
           }
        """.trimIndent()

    doSubmitRequest(query)
      .errors()
      .satisfy { err ->
        assertNotFound(err, randomId)
      }
  }

  fun doSubmitAndAssert(query: String, vararg tuples: Tuple) {
    val resultList = doSubmitRequestAndReturnList(query)
    assertThat(resultList)
      .extracting("name", "description")
      .containsExactlyInAnyOrder(*tuples)
  }

  fun doSubmitRequestAndReturnList(query: String): MutableList<Category> =
    doSubmitRequestAndGetPath(query, "categories")
      .entityList(Category::class.java)
      .get()

  fun doSubmitRequestAndGetPath(query: String, path: String) =
    doSubmitRequest(query)
      .path(path)

  fun doSubmitRequest(query: String) =
    HttpGraphQlTester.create(webClient)
      .document(query)
      .execute()

  fun getBeverages() = repository.findAll().collectList().block()
    ?.stream()
    ?.sorted(Comparator.comparing { it.name })
    ?.collect(Collectors.toList())
    ?.get(0)

  fun assertValidationError(
    err: MutableList<ResponseError>,
    vararg expectedErrorFields: Tuple
  ) {
    assertThat(err)
      .extracting(ResponseError::getErrorType)
      .contains(Tuple.tuple(ErrorType.ValidationError))

    assertThat(
      err.stream()
        .map { it.extensions[VALIDATION_ERRORS] as List<*> }
        .map { it.filterIsInstance<LinkedHashMap<String, String>>() }
        .flatMap { it.stream() }
        .map { objectMapper.convertValue(it, ValidationError::class.java) }
        .collect(Collectors.toList())
    ).extracting(ValidationError::field, ValidationError::message)
      .containsExactlyInAnyOrder(*expectedErrorFields)
  }

  private fun assertNotFound(err: List<ResponseError>, randomId: String) {
    assertThat(err)
      .extracting(ResponseError::getErrorType)
      .contains(Tuple.tuple(ErrorType.DataFetchingException))

    assertThat(
      err.stream()
        .map { it.message }
        .collect(Collectors.toList())
    ).containsExactlyInAnyOrder("Data not exists. Id: $randomId")
  }
}
