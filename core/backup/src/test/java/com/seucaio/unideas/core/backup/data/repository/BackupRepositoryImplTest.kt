package com.seucaio.unideas.core.backup.data.repository

import android.content.Context
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.seucaio.unideas.data.local.database.UnideasDatabase
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.OutputStream

class BackupRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val database: UnideasDatabase = mockk(relaxed = true)
    private val context: Context = mockk()
    private val driveService: Drive = mockk(relaxed = true)

    private lateinit var repository: BackupRepositoryImpl

    @Before
    fun setUp() {
        mockkObject(UnideasDatabase.Companion)
        justRun { UnideasDatabase.resetInstance() }
        justRun { UnideasDatabase.checkpoint(database) }
        every { UnideasDatabase.getInstance(context) } returns database

        val dbFile = tempFolder.newFile("unideas.db").also { it.writeText("fake-db") }
        val cacheDir = tempFolder.newFolder("cache")
        every { context.getDatabasePath(UnideasDatabase.DATABASE_NAME) } returns dbFile
        every { context.cacheDir } returns cacheDir

        repository = BackupRepositoryImpl(database, context)
    }

    @After
    fun tearDown() {
        unmockkObject(UnideasDatabase.Companion)
    }

    @Test
    fun `uploadBackup checkpoints the database and uploads without closing it`() = runTest {
        val driveFiles = mockk<Drive.Files>()
        val createRequest = mockk<Drive.Files.Create>(relaxed = true)
        val uploadedFile = File().apply {
            id = "remote-file-id"
            name = UnideasDatabase.DATABASE_NAME
            createdTime = DateTime(System.currentTimeMillis())
            factory = GsonFactory.getDefaultInstance()
        }

        every { driveService.files() } returns driveFiles
        every { driveFiles.create(any(), any()) } returns createRequest
        every { createRequest.setFields(any()) } returns createRequest
        every { createRequest.execute() } returns uploadedFile

        val result = repository.uploadBackup(driveService)

        verify(exactly = 1) { UnideasDatabase.checkpoint(database) }
        verify(exactly = 0) { database.close() }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `uploadBackup tags the file as automatic when isAutomatic is true`() = runTest {
        val driveFiles = mockk<Drive.Files>()
        val createRequest = mockk<Drive.Files.Create>(relaxed = true)
        val metadataSlot = slot<File>()
        val uploadedFile = File().apply {
            id = "remote-file-id"
            name = UnideasDatabase.DATABASE_NAME
            createdTime = DateTime(System.currentTimeMillis())
            factory = GsonFactory.getDefaultInstance()
        }

        every { driveService.files() } returns driveFiles
        every { driveFiles.create(capture(metadataSlot), any()) } returns createRequest
        every { createRequest.setFields(any()) } returns createRequest
        every { createRequest.execute() } returns uploadedFile

        val result = repository.uploadBackup(driveService, isAutomatic = true)

        assertTrue(result.isSuccess)
        assertEquals(mapOf("backupType" to "auto"), metadataSlot.captured.appProperties)
    }

    @Test
    fun `uploadBackup does not tag the file when isAutomatic is false`() = runTest {
        val driveFiles = mockk<Drive.Files>()
        val createRequest = mockk<Drive.Files.Create>(relaxed = true)
        val metadataSlot = slot<File>()
        val uploadedFile = File().apply {
            id = "remote-file-id"
            name = UnideasDatabase.DATABASE_NAME
            createdTime = DateTime(System.currentTimeMillis())
            factory = GsonFactory.getDefaultInstance()
        }

        every { driveService.files() } returns driveFiles
        every { driveFiles.create(capture(metadataSlot), any()) } returns createRequest
        every { createRequest.setFields(any()) } returns createRequest
        every { createRequest.execute() } returns uploadedFile

        val result = repository.uploadBackup(driveService)

        assertTrue(result.isSuccess)
        assertEquals(null, metadataSlot.captured.appProperties)
    }

    @Test
    fun `uploadBackup returns failure when drive throws`() = runTest {
        val driveFiles = mockk<Drive.Files>()
        val createRequest = mockk<Drive.Files.Create>(relaxed = true)

        every { driveService.files() } returns driveFiles
        every { driveFiles.create(any(), any()) } returns createRequest
        every { createRequest.setFields(any()) } returns createRequest
        every { createRequest.execute() } throws RuntimeException("Network error")

        val result = repository.uploadBackup(driveService)

        assertTrue(result.isFailure)
    }

    @Test
    fun `listBackups returns an empty list when drive has no files`() = runTest {
        val driveFiles = mockk<Drive.Files>()
        val listRequest = mockk<Drive.Files.List>(relaxed = true)
        val emptyFileList = FileList().apply { files = emptyList() }

        every { driveService.files() } returns driveFiles
        every { driveFiles.list() } returns listRequest
        every { listRequest.setSpaces(any()) } returns listRequest
        every { listRequest.setFields(any()) } returns listRequest
        every { listRequest.setQ(any()) } returns listRequest
        every { listRequest.setOrderBy(any()) } returns listRequest
        every { listRequest.execute() } returns emptyFileList

        val result = repository.listBackups(driveService)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `getCurrentAutoBackupInfo returns the file when drive has an automatic backup`() = runTest {
        val driveFiles = mockk<Drive.Files>()
        val listRequest = mockk<Drive.Files.List>(relaxed = true)
        val autoFile = File().apply {
            id = "auto-file-id"
            name = UnideasDatabase.DATABASE_NAME
            createdTime = DateTime(System.currentTimeMillis())
            setSize(2048L)
        }
        val fileList = FileList().apply { files = listOf(autoFile) }

        every { driveService.files() } returns driveFiles
        every { driveFiles.list() } returns listRequest
        every { listRequest.setSpaces(any()) } returns listRequest
        every { listRequest.setFields(any()) } returns listRequest
        every { listRequest.setQ(any()) } returns listRequest
        every { listRequest.setOrderBy(any()) } returns listRequest
        every { listRequest.execute() } returns fileList

        val result = repository.getCurrentAutoBackupInfo(driveService)

        assertTrue(result.isSuccess)
        assertEquals("auto-file-id", result.getOrNull()?.fileId)
    }

    @Test
    fun `getCurrentAutoBackupInfo returns null when drive has no automatic backup`() = runTest {
        val driveFiles = mockk<Drive.Files>()
        val listRequest = mockk<Drive.Files.List>(relaxed = true)
        val emptyFileList = FileList().apply { files = emptyList() }

        every { driveService.files() } returns driveFiles
        every { driveFiles.list() } returns listRequest
        every { listRequest.setSpaces(any()) } returns listRequest
        every { listRequest.setFields(any()) } returns listRequest
        every { listRequest.setQ(any()) } returns listRequest
        every { listRequest.setOrderBy(any()) } returns listRequest
        every { listRequest.execute() } returns emptyFileList

        val result = repository.getCurrentAutoBackupInfo(driveService)

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull())
    }

    @Test
    fun `restoreBackup closes the database before downloading and reopens it after`() = runTest {
        val driveFiles = mockk<Drive.Files>()
        val getRequest = mockk<Drive.Files.Get>(relaxed = true)
        val outputStreamSlot = slot<OutputStream>()

        every { driveService.files() } returns driveFiles
        every { driveFiles.get("file-id-1") } returns getRequest
        every { getRequest.executeMediaAndDownloadTo(capture(outputStreamSlot)) } answers {
            outputStreamSlot.captured.write("restored".toByteArray())
        }

        val result = repository.restoreBackup(driveService, "file-id-1")

        coVerifyOrder {
            database.close()
            UnideasDatabase.resetInstance()
            UnideasDatabase.getInstance(context)
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteBackup executes the delete request for the given file id`() = runTest {
        val driveFiles = mockk<Drive.Files>()
        val deleteRequest = mockk<Drive.Files.Delete>(relaxed = true)

        every { driveService.files() } returns driveFiles
        every { driveFiles.delete("file-id-1") } returns deleteRequest

        val result = repository.deleteBackup(driveService, "file-id-1")

        verify(exactly = 1) { deleteRequest.execute() }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteBackup returns failure when drive throws`() = runTest {
        val driveFiles = mockk<Drive.Files>()
        val deleteRequest = mockk<Drive.Files.Delete>(relaxed = true)

        every { driveService.files() } returns driveFiles
        every { driveFiles.delete("file-id-1") } returns deleteRequest
        every { deleteRequest.execute() } throws RuntimeException("Network error")

        val result = repository.deleteBackup(driveService, "file-id-1")

        assertTrue(result.isFailure)
    }
}
