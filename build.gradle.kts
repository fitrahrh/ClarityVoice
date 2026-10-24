plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "me.harukiyu.clarityvoice"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.maxhenkel.de/repository/public")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("de.maxhenkel.voicechat:voicechat-api:2.6.25")
    compileOnly(files("../ClarityLangCore/build/libs/ClarityLangCore-1.0.0.jar"))

    implementation("io.javalin:javalin:6.4.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "25"
    targetCompatibility = "25"
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
