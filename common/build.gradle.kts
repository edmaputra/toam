plugins {
  id("org.springframework.boot")
  id("org.graalvm.buildtools.native")

  kotlin("jvm")
  kotlin("plugin.spring")
}

extra["springCloudVersion"] = "2022.0.4"

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

dependencies {
  implementation(project(":core"))

  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-graphql")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
//  implementation("org.springframework.cloud:spring-cloud-starter-vault-config")
  implementation("org.liquibase:liquibase-core")

  compileOnly("org.projectlombok:lombok")

  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("org.postgresql:r2dbc-postgresql")

  annotationProcessor("org.projectlombok:lombok")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.springframework.graphql:spring-graphql-test")
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
  }
}

tasks.test {
  useJUnitPlatform()
}
