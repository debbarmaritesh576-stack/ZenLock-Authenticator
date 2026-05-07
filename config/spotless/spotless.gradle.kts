apply(plugin = "com.diffplug.spotless")

spotless {
    kotlin {
        target("**/*.kt")
        ktlint("12.1.0")
            .setUseExperimental(true)
            .editorConfigOverride(
                mapOf(
                    "indent_size" to "4",
                    "max_line_length" to "120"
                )
            )
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint("12.1.0")
        trimTrailingWhitespace()
        endWithNewline()
    }
}