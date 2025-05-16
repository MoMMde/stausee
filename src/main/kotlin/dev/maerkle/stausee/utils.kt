package dev.maerkle.stausee

import io.ktor.util.network.*
import org.apache.sshd.server.session.ServerSession
import java.net.SocketAddress

val SocketAddress.remoteAddressString: String
    get() = "${this.address}:${this.port} (${this.hostname})"


fun usernameToFolder(name: String) = name
    .replace("-", "")
    .replace("~", "")
    .replace("/", "")
    .replace("\\", "")