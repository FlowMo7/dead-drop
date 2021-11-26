package dev.moetz.deaddrop.data

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.ZonedDateTime

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
            keepFilesTimeInSeconds = 100,
            timePeriodToSweepOverdueFilesInSeconds = 100
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
            keepFilesTimeInSeconds = 100,
            timePeriodToSweepOverdueFilesInSeconds = 100
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
            keepFilesTimeInSeconds = 100,
            timePeriodToSweepOverdueFilesInSeconds = 100
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
            keepFilesTimeInSeconds = 100,
            timePeriodToSweepOverdueFilesInSeconds = 100
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
            keepFilesTimeInSeconds = 100,
            timePeriodToSweepOverdueFilesInSeconds = 100
        )

        val dropContent = dataRepository.getDrop("ASDF1234")

        coVerify(exactly = 0) { encryptionManager.decrypt(any()) }

        assertNull(dropContent)
    }


}