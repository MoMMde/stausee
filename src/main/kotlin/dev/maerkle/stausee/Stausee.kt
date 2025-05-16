package dev.maerkle.stausee

import dev.maerkle.stausee.api.Api
import dev.maerkle.stausee.api.Authorization
import dev.maerkle.stausee.sftp.StauseeSftp
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

const val SSH_FOLDER_PROPERTY = "directory"

fun main() {
    embeddedServer(CIO,
        configure = {
            connector {
                port = 8080
                host = "0.0.0.0"
            }
        }) {

        install(ContentNegotiation) {
            json()
        }

        install(CallLogging) {
            format { call ->
                "[${call.request.httpMethod} (${call.response.status()})] ${call.request.path()}"
            }
        }

        StauseeSftp()

        Authorization()

        routing {
            route("/api") {
                Api()
            }
        }

    }.start(wait = true)
}