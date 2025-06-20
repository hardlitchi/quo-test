import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.flywaydb.flyway") version "9.22.3"
    id("nu.studer.jooq") version "8.2.1"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    jacoco
}

group = "test.quo.hardlitchi"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    
    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // Database
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")
    
    // jOOQ
    jooqGenerator("org.postgresql:postgresql")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("com.h2database:h2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // テスト実行後にカバレッジレポートを生成
}

// JaCoCo設定
jacoco {
    toolVersion = "0.8.11"  // Java 21サポート版
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // テスト実行後にレポート生成
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    
    // カバレッジ対象から除外するクラス
    classDirectories.setFrom(files(classDirectories.files.map {
        fileTree(it) {
            exclude(
                // jOOQ生成クラスを除外
                "test/quo/hardlitchi/generated/**",
                // Spring Boot起動クラスを除外
                "test/quo/hardlitchi/QuoTestApplicationKt.class",
                // エンティティクラス（データクラス）を除外する場合
                // "test/quo/hardlitchi/common/entity/**"
            )
        }
    }))
}

// カバレッジ検証タスク
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80%以上のカバレッジを要求
            }
        }
        rule {
            element = "CLASS"
            limit {
                counter = "BRANCH"
                minimum = "0.70".toBigDecimal() // 分岐カバレッジ70%以上
            }
        }
    }
    
    // カバレッジ検証対象から除外するクラス
    classDirectories.setFrom(files(classDirectories.files.map {
        fileTree(it) {
            exclude(
                "test/quo/hardlitchi/generated/**",
                "test/quo/hardlitchi/QuoTestApplicationKt.class"
            )
        }
    }))
}

// kotlin設定
kotlin {
    jvmToolchain(21)
}

// jOOQ設定
jooq {
    version.set("3.18.7")
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)  // 自動生成を無効化
            
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/quo_test"
                    user = "quo_user"
                    password = "quo_pass"
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "test.quo.hardlitchi.generated"
                        directory = "src/main/generated"
                    }
                }
            }
        }
    }
}

// Flyway設定
flyway {
    url = "jdbc:postgresql://localhost:5432/quo_test"
    user = "quo_user"
    password = "quo_pass"
    schemas = arrayOf("public")
}