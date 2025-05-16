package dev.maerkle.stausee

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.writeBytes

const val STAUSEE_FILE_NAME = ".stausee.json"

@Serializable
data class StauseeConfigFile(
    var password: String,
    var id: String = generateId()
)

private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

private fun generateId(n: Int = 16): String {
    var s = ""
    repeat(n) {
        s += (('a'..'z') + ('A'..'Z')).random()
    }
    return s
}

fun readConfigFile(path: Path): StauseeConfigFile {
    val configFile = path.resolve(STAUSEE_FILE_NAME).toFile()
    return json.decodeFromString(configFile.readText())
}

fun updateConfigFile(path: Path, configFile: StauseeConfigFile) {
    if (path.name != STAUSEE_FILE_NAME) {
        var path = path.resolve(STAUSEE_FILE_NAME)
    }
    path.writeBytes(json.encodeToString(configFile).encodeToByteArray())
}