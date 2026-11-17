package tech.ula.model.remote

import android.content.res.AssetManager
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.ula.BuildConfig
import tech.ula.model.entities.App
import tech.ula.utils.* // ktlint-disable no-wildcard-imports
import java.io.File
import java.io.IOException
import java.util.Locale

class GithubAppsFetcher(
    private val filesDirPath: String,
    private val assets: AssetManager,
    private val sharedPreferences: SharedPreferences,
    private val httpStream: HttpStream = HttpStream(),
    private val logger: Logger = SentryLogger()
) {

    // Allows destructing of the list of application elements
    private operator fun <T> List<T>.component6() = get(5)
    private operator fun <T> List<T>.component7() = get(6)

    private fun baseUrl(): String {
        // Sakura Candy Milestone 1: use the verified GIMP catalog; preserve preferences unchanged.
        return "https://raw.githubusercontent.com/CypherpunkArmory/UserLAnd-Assets-Support/staging/apps"
    }

    @Throws(IOException::class)
    suspend fun fetchAppsList(): List<App> = withContext(Dispatchers.IO) {
        val url = "${baseUrl()}/apps.txt"
        try {
            val lines = if (BuildConfig.APPS_IN_ASSETS) {
                assets.open("apps/apps.txt").bufferedReader().use { it.readLines() }
            } else {
                httpStream.toLines(url)
            }
            val apps = lines.drop(1).mapIndexedNotNull { index, original ->
                val lineNumber = index + 2
                val line = original.trim()
                if (line.isEmpty()) return@mapIndexedNotNull null
                val parts = line.split(',').map { it.trim() }
                if (parts.size != 7) {
                    throw IOException("Catalog line $lineNumber: expected 7 fields, got ${parts.size}: $line")
                }
                val (name, category, filesystem, cli, gui, paid, versionText) = parts
                if (name.isBlank() || category.isBlank() || filesystem.isBlank()) {
                    throw IOException("Catalog line $lineNumber: name, category, and filesystem are required")
                }
                fun parseFlag(value: String): Boolean = when (value.lowercase(Locale.ENGLISH)) {
                    "true" -> true
                    "false" -> false
                    else -> throw IOException("Catalog line $lineNumber: invalid boolean '$value'")
                }
                val version = versionText.toLongOrNull()
                    ?: throw IOException("Catalog line $lineNumber: invalid version '$versionText'")
                App(name.lowercase(Locale.ENGLISH), category.lowercase(Locale.ENGLISH),
                    filesystem.lowercase(Locale.ENGLISH), parseFlag(cli), parseFlag(gui),
                    parseFlag(paid), version)
            }
            if (apps.none { it.name == "gimp" && it.filesystemRequired == "debian" }) {
                throw IOException("Catalog missing expected GIMP entry requiring Debian: $url")
            }
            apps
        } catch (err: Exception) {
            val exception = IOException("Cannot read or parse catalog $url: ${err.message}", err)
            logger.addExceptionBreadcrumb(exception)
            throw exception
        }
    }
    suspend fun fetchAppIcon(app: App) = withContext(Dispatchers.IO) {
        val directoryAndFilename = "${app.name}/${app.name}.png"
        val file = File("$filesDirPath/apps/$directoryAndFilename")

        if (BuildConfig.APPS_IN_ASSETS) {
            file.parentFile!!.mkdirs()
            assets.open("apps/$directoryAndFilename").use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output, 1024)
                }
            }
        } else {
            val url = "${baseUrl()}/$directoryAndFilename"
            httpStream.toFile(url, file)
        }
    }

    suspend fun fetchAppDescription(app: App) = withContext(Dispatchers.IO) {
        val directoryAndFilename = "${app.name}/${app.name}.txt"
        val file = File("$filesDirPath/apps/$directoryAndFilename")

        if (BuildConfig.APPS_IN_ASSETS) {
            file.parentFile!!.mkdirs()
            assets.open("apps/$directoryAndFilename").use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output, 1024)
                }
            }
        } else {
            val url = "${baseUrl()}/$directoryAndFilename"
            httpStream.toTextFile(url, file)
        }
    }

    suspend fun fetchAppScript(app: App) = withContext(Dispatchers.IO) {
        val directoryAndFilename = "${app.name}/${app.name}.sh"
        val file = File("$filesDirPath/apps/$directoryAndFilename")

        if (BuildConfig.APPS_IN_ASSETS) {
            file.parentFile!!.mkdirs()
            assets.open("apps/$directoryAndFilename").use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output, 1024)
                }
            }
        } else {
            val url = "${baseUrl()}/$directoryAndFilename"
            httpStream.toTextFile(url, file)
        }
    }
}
