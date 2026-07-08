// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
}

tasks.register<Exec>("installGitHooks") {
    description = "Configura o git para usar os hooks de .githooks/"
    commandLine(
        "bash", "-c",
        "git config core.hooksPath .githooks && " +
        "chmod +x .githooks/pre-commit .githooks/commit-msg .githooks/pre-push && " +
        "echo '✅ Git hooks instalados: pre-commit, commit-msg, pre-push.'"
    )
}