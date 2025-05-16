package dev.maerkle.stausee.sftp

import dev.maerkle.stausee.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.util.network.*
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.common.session.Session
import org.apache.sshd.common.session.SessionListener
import org.apache.sshd.core.CoreModuleProperties
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.WelcomeBannerPhase
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.shell.ProcessShellFactory
import org.apache.sshd.sftp.server.*
import java.nio.file.*


private val logger = KotlinLogging.logger("dev.maerkle.stausee.sftp.StauseeSftp")

private val sshdKeyFile = "./sshd_server.key"

class StauseeSftp : SessionListener {
    private val ssh: SshServer = SshServer.setUpDefaultServer()
    init {
        ssh.port = 8022
        ssh.host = "0.0.0.0"
        ssh.passwordAuthenticator = SftpAuthentication(ssh)

        CoreModuleProperties.PREFERRED_AUTHS.set(ssh, "password")
        CoreModuleProperties.WELCOME_BANNER_PHASE.set(ssh, WelcomeBannerPhase.POST_SUCCESS)

        val sftp = SftpSubsystemFactory.Builder()
            .withFileSystemAccessor(SftpFilesystem())
            .build()

        ssh.addSessionListener(object : SessionListener {
            override fun sessionClosed(session: Session) {
                logger.info { "Closed session ${session.remoteAddress.remoteAddressString}" }
            }
        })

        ssh.subsystemFactories = listOf(sftp)
        ssh.addSessionListener(this)

        ssh.keyPairProvider = SimpleGeneratorHostKeyProvider(Path.of(sshdKeyFile))

        ssh.start()
    }


}