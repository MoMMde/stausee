package dev.maerkle.stausee.sftp

import dev.maerkle.stausee.Config
import dev.maerkle.stausee.SSH_FOLDER_PROPERTY
import dev.maerkle.stausee.STAUSEE_FILE_NAME
import dev.maerkle.stausee.remoteAddressString
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.nio.*
import kotlinx.coroutines.runBlocking
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.sftp.server.DirectoryHandle
import org.apache.sshd.sftp.server.FileHandle
import org.apache.sshd.sftp.server.SftpFileSystemAccessor
import org.apache.sshd.sftp.server.SftpSubsystemProxy
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.FileAttribute
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.io.path.readText

class SftpFilesystem : SftpFileSystemAccessor {

    override fun openDirectory(
        subsystem: SftpSubsystemProxy?,
        dirHandle: DirectoryHandle?,
        dir: Path?,
        handle: String?,
        vararg linkOptions: LinkOption?
    ): DirectoryStream<Path> {
        return Files.newDirectoryStream(dir) { it.name != STAUSEE_FILE_NAME }
    }

    override fun openFile(
        subsystem: SftpSubsystemProxy,
        fileHandle: FileHandle,
        file: Path,
        handle: String,
        options: MutableSet<out OpenOption>,
        vararg attrs: FileAttribute<*>
    ): SeekableByteChannel {
        if (file.name == STAUSEE_FILE_NAME) {
            subsystem.session.close()
            throw IllegalAccessException("User tried to access ${file.absolutePathString()}. ${subsystem.session.remoteAddress.remoteAddressString}")
        }
        return FileChannel.open(file)
    }

    private fun asInternalFile(sftp: SftpSubsystemProxy, file: Path) = (sftp.session.properties[SSH_FOLDER_PROPERTY] as Path)
        .resolve(file.name)
        .normalize()
}