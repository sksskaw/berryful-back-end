import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val kotlinVer = "1.6.10"

	id("org.springframework.boot") version "2.5.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	war
	kotlin("jvm") version kotlinVer
	kotlin("plugin.spring") version kotlinVer
	kotlin("plugin.jpa") version kotlinVer
	kotlin("kapt") version kotlinVer
}

group = "berryful.lounge"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_16

repositories {
	mavenCentral()
}

tasks.war {
	archiveFileName.value("ROOT.war")
}

allOpen {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.Embeddable")
	annotation("javax.persistence.MappedSuperclass")
}

noArg {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.Embeddable")
	annotation("javax.persistence.MappedSuperclass")
}

dependencies {
	implementation("com.querydsl:querydsl-jpa:5.0.0")
	implementation("com.querydsl:querydsl-core:5.0.0")
	kapt("com.querydsl:querydsl-apt:5.0.0:jpa")
	kapt("org.springframework.boot:spring-boot-configuration-processor")

	implementation("org.apache.httpcomponents:httpclient:4.5.13")
	implementation("org.apache.httpcomponents:httpmime:4.5.13")
	implementation("com.google.code.gson:gson:2.8.5")

	implementation("com.googlecode.json-simple:json-simple:1.1.1")
	implementation("org.openkoreantext:open-korean-text:2.3.1")
	implementation("org.apache.commons:commons-text:1.9")

	implementation("com.google.firebase:firebase-admin:8.1.0")
	implementation("com.squareup.okhttp3:okhttp:4.9.3")

	implementation("com.amazonaws:aws-java-sdk-ses:1.12.213")
	implementation("com.auth0:java-jwt:3.19.1")
	implementation("org.apache.commons:commons-lang3")
	implementation("io.github.microutils:kotlin-logging:2.1.21")
	implementation("org.mariadb.jdbc:mariadb-java-client:3.0.4")
	implementation("net.sf.ehcache:ehcache:2.10.6")

	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	runtimeOnly("mysql:mysql-connector-java")
	providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
}

kotlin.sourceSets.main {
	println("kotlin sourceSets buildDir :: $buildDir")
	setBuildDir("$buildDir")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "16"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}