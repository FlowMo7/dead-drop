package dev.moetz.deaddrop.data

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.seconds

class DataRepositoryTest {

    private lateinit var dataRepository: DataRepository
    private lateinit var encryptionManager: EncryptionManager
    private lateinit var dataFolderPath: File

    @Before
    fun setUp() {
        encryptionManager = mockk()
    }

    @After
    fun tearDown() {
        dataFolderPath.deleteRecursively()
    }

    @Test
    fun `addDrop succeeds when data folder does not exist yet`() = runBlocking {
        dataFolderPath = File("./dataFolderPath-test1")
        dataRepository = DataRepository(
            dataFolderPath = dataFolderPath.absolutePath,
            encryptionManager = encryptionManager,
            keepFilesTime = 100.seconds,
            timePeriodToSweepOverdueFiles = 100.seconds,
        )

        every { encryptionManager.encrypt(any()) } returns "encryptedContent"

        val dropId = dataRepository.addDrop("some-content")

        assertNotNull(dropId)
        assertNotEquals("", dropId)

        val dropFile = File(dataFolderPath, dropId)

        assertTrue(dropFile.exists())

        assertEquals("encryptedContent", dropFile.readText())
    }

    @Test
    fun `addDrop succeeds when data folder already exists`() = runBlocking {
        dataFolderPath = File("./dataFolderPath-test2").also { it.mkdirs() }
        dataRepository = DataRepository(
            dataFolderPath = dataFolderPath.absolutePath,
            encryptionManager = encryptionManager,
            keepFilesTime = 100.seconds,
            timePeriodToSweepOverdueFiles = 100.seconds,
        )

        every { encryptionManager.encrypt(any()) } returns "encryptedContent12"

        val dropId = dataRepository.addDrop("some-content1")

        assertNotNull(dropId)
        assertNotEquals("", dropId)

        val dropFile = File(dataFolderPath, dropId)

        assertTrue(dropFile.exists())

        assertEquals("encryptedContent12", dropFile.readText())
    }

    @Test
    fun `getDrop succeeds when file exists and is not overdue`() = runBlocking {
        dataFolderPath = File("./dataFolderPath-test3").also { it.mkdirs() }
        val dropFile = File(dataFolderPath, "ASDF1234").also {
            it.writeText("encryptedContent123")
        }

        dataRepository = DataRepository(
            dataFolderPath = dataFolderPath.absolutePath,
            encryptionManager = encryptionManager,
            keepFilesTime = 100.seconds,
            timePeriodToSweepOverdueFiles = 100.seconds,
        )

        every { encryptionManager.decrypt("encryptedContent123") } returns "some-content".toByteArray(Charsets.UTF_8)

        val dropContent = dataRepository.getDrop("ASDF1234")

        assertNotNull(dropContent)
        assertEquals("some-content", dropContent)

        assertFalse(dropFile.exists())
    }

    @Test
    fun `getDrop returns null when file exists but is overdue`() = runBlocking {
        dataFolderPath = File("./dataFolderPath-test4").also { it.mkdirs() }
        val dropFile = File(dataFolderPath, "ASDF1234").also {
            it.writeText("encryptedContent123")
            it.setLastModified(ZonedDateTime.now().minusSeconds(101).toInstant().epochSecond)
        }

        dataRepository = DataRepository(
            dataFolderPath = dataFolderPath.absolutePath,
            encryptionManager = encryptionManager,
            keepFilesTime = 100.seconds,
            timePeriodToSweepOverdueFiles = 100.seconds,
        )

        every { encryptionManager.decrypt("encryptedContent123") } returns "some-content".toByteArray(Charsets.UTF_8)

        val dropContent = dataRepository.getDrop("ASDF1234")

        coVerify(exactly = 0) { encryptionManager.decrypt(any()) }

        assertNull(dropContent)

        assertFalse(dropFile.exists())
    }

    @Test
    fun `getDrop returns null when file does not exist`() = runBlocking {
        dataFolderPath = File("./dataFolderPath-test5").also { it.mkdirs() }

        dataRepository = DataRepository(
            dataFolderPath = dataFolderPath.absolutePath,
            encryptionManager = encryptionManager,
            keepFilesTime = 100.seconds,
            timePeriodToSweepOverdueFiles = 100.seconds,
        )

        val dropContent = dataRepository.getDrop("ASDF1234")

        coVerify(exactly = 0) { encryptionManager.decrypt(any()) }

        assertNull(dropContent)
    }

    @Test
    fun `cleanUpOverdueFiles removes files older than keepFilesTimeInSeconds`() = runBlocking {
        dataFolderPath = File("./dataFolderPath-test-cleanup").also { it.mkdirs() }

        val fileOverdue1 = File(dataFolderPath, "file-overdue-1").also {
            it.writeText("old content 1")
            it.setLastModified(System.currentTimeMillis() - 101_000) // 101 seconds ago
        }

        val fileNotOverdue = File(dataFolderPath, "file-not-overdue").also {
            it.writeText("recent content")
            it.setLastModified(System.currentTimeMillis() - 50_000) // 50 seconds ago
        }

        val fileOverdue2 = File(dataFolderPath, "file-overdue-2").also {
            it.writeText("old content 2")
            it.setLastModified(System.currentTimeMillis() - 150_000) // 150 seconds ago
        }

        // Create repository with short sweep period
        dataRepository = DataRepository(
            dataFolderPath = dataFolderPath.absolutePath,
            encryptionManager = encryptionManager,
            keepFilesTime = 100.seconds,
            timePeriodToSweepOverdueFiles = 2.seconds,
        )

        // Wait for cleanup to run (2s sweep period + buffer)
        delay(3000)

        // Assert overdue files are deleted
        assertFalse(fileOverdue1.exists())
        assertFalse(fileOverdue2.exists())

        // Assert non-overdue file still exists
        assertTrue(fileNotOverdue.exists())
    }

    @Test
    fun `cleanUpOverdueFiles handles empty directory without errors`() = runBlocking {
        dataFolderPath = File("./dataFolderPath-test-empty").also { it.mkdirs() }

        dataRepository = DataRepository(
            dataFolderPath = dataFolderPath.absolutePath,
            encryptionManager = encryptionManager,
            keepFilesTime = 100.seconds,
            timePeriodToSweepOverdueFiles = 2.seconds,
        )

        // Wait for cleanup to run
        delay(3000)

        // Assert no errors and folder still exists
        assertTrue(dataFolderPath.exists())
        assertTrue(dataFolderPath.isDirectory)
    }


}