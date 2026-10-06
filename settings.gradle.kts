pluginManagement {
    repositories {
        // 中国のミラーサーバー（Aliyun）
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        // オリジナルも保持
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 中国のミラーサーバー（Aliyun）
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
        }
        // オリジナルも保持
        google()
        mavenCentral()
    }
}

rootProject.name = "mGBA Emulator"
include(":app")
