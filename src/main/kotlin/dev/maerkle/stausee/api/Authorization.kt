package dev.maerkle.stausee.api

import dev.maerkle.stausee.Config
import dev.maerkle.stausee.StauseeConfigFile
import dev.maerkle.stausee.readConfigFile
import dev.maerkle.stausee.usernameToFolder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import java.nio.file.Path

data class StauseePrincipal(
    val path: Path,
    val config: StauseeConfigFile
)

fun Application.Authorization() {
    install(Authentication) {
        basic {
            validate { credentials ->
                val folder = usernameToFolder(credentials.name)
                val path = Path.of(Config.STORAGE_PATH).resolve(folder)

                val config = readConfigFile(path)
                if (credentials.password == config.password) {
                    StauseePrincipal(path, config)
                } else {
                    null
                }
            }
        }
    }
}