import org.gradle.kotlin.dsl.mavenCentral

rootProject.name = "KTeeth"

dependencyResolutionManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://packages.confluent.io/maven/")
    }
}
