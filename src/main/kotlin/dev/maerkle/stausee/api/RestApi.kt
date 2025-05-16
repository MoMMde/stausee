package dev.maerkle.stausee.api

import dev.maerkle.stausee.Config
import dev.maerkle.stausee.STAUSEE_FILE_NAME
import dev.maerkle.stausee.api.routes.Content
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries

fun Route.Api() {

    authenticate {
        Content()
    }

}