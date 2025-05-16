package dev.maerkle.stausee.api.routes

import dev.maerkle.stausee.Config
import dev.maerkle.stausee.STAUSEE_FILE_NAME
import dev.maerkle.stausee.api.StauseePrincipal
import dev.maerkle.stausee.generated.BuildConfig
import dev.schlaubi.stdx.logging.logger
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.*
import kotlin.random.Random


@Serializable
private data class DirectoryContent(
    val id: String,
    @SerialName("file_count")
    val fileCount: Int,
    @SerialName("total_size")
    val totalSize: Long,
    val files: List<FileMetaData>
)

@Serializable
private data class FileMetaData(
    val name: String,
    @SerialName("file_size")
    val fileSize: Long,
    @SerialName("last_modified")
    val lastModified: Long
)

private val logger = logger()

fun Route.Content() {
    get("/list") {
        val principal = call.principal<StauseePrincipal>()

        val storage = principal?.path ?: return@get call.respond(HttpStatusCode.NotFound)
        val config = principal.config


        logger.info { "Listing the contents of ${storage.absolutePathString()} (${config.id})" }
        val response = DirectoryContent(
            id = config.id,
            fileCount = storage.toFile().listFiles()?.count()?.minus(1) ?: 0,
            totalSize = storage.listDirectoryEntries().sumOf { it.fileSize() }.minus(storage.resolve(STAUSEE_FILE_NAME).fileSize()),
            files = storage.listDirectoryEntries().filter { it.name != STAUSEE_FILE_NAME }.map { file ->
                FileMetaData(
                    name = file.name,
                    fileSize = file.fileSize(),
                    lastModified = file.getLastModifiedTime().toMillis()
                )
            }
        )

        call.respond(response)
    }

    get("/download") {

        val principal = call.principal<StauseePrincipal>()

        val storage = principal?.path ?: return@get call.respond(HttpStatusCode.NotFound)
        val config = principal.config

        val filesToDownload = call.queryParameters["files"]?.split(',')
            ?.map { storage.resolve(it) } ?: storage.listDirectoryEntries()

        if (filesToDownload.isEmpty())
            return@get call.respond(HttpStatusCode.NotFound, "Could not find files")

        if (filesToDownload.size == 1) {
            logger.info { "User is downloading one file uncompressed. ${filesToDownload.first().absolutePathString()}" }
            return@get call.respondBytes(ContentType.Application.Any) {
                filesToDownload.first().readBytes()
            }
        }

        val zipFile = createZipFile(filesToDownload)

        call.respondFile(zipFile) {
            logger.info { "User is downloading ${this.contentLength} bytes. (Compressed as zip)" }
        }
        zipFile.delete()
    }
}

private fun createZipFile(files: List<Path>): File {
    val zipFile = File.createTempFile("temp", "${Random.nextInt()}-${files.count()}.zip")
    val outputStream = FileOutputStream(zipFile)
    val zipStream = ZipOutputStream(outputStream).apply {
        setComment("Stausee v${BuildConfig.VERSION} - Auto generated")
    }
    files.filter { it.name != STAUSEE_FILE_NAME }.forEach {
        val zipEntry = ZipEntry(it.name)
        zipStream.putNextEntry(zipEntry)
        zipStream.write(it.readBytes())
    }

    zipStream.close()
    outputStream.close()
    return zipFile
}