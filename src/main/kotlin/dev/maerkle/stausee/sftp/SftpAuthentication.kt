package dev.maerkle.stausee.sftp

import dev.maerkle.stausee.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.core.CoreModuleProperties
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.WelcomeBannerPhase
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.session.ServerSession
import java.nio.file.Path
import java.time.Clock
import kotlin.time.toKotlinInstant

private val logger = KotlinLogging.logger("dev.maerkle.stausee.sftp.SftpAuthentication")

class SftpAuthentication(val ssh: SshServer) : PasswordAuthenticator {
    override fun authenticate(username: String, password: String, session: ServerSession): Boolean {

        val folder = usernameToFolder(username)
        val path = Path.of(Config.STORAGE_PATH).resolve(folder)

        val remoteAddress = session.remoteAddress.remoteAddressString

        val config = readConfigFile(path)

        if (password == config.password) {
            CoreModuleProperties.WELCOME_BANNER.set(ssh, """
                Successful login as $folder
                Server Time: ${Clock.systemUTC().instant().atZone(Clock.systemDefaultZone().zone)}
                Id: ${config.id}
            """.trimIndent())
            ssh.fileSystemFactory = VirtualFileSystemFactory(path)

            logger.info { "Successfully login from $remoteAddress to ${path.fileName}" }
            session.properties[SSH_FOLDER_PROPERTY] = path
            return true
        }

        return false
    }

}