package dev.moetz.deaddrop.data

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DataRepositoryTest {

    private lateinit var dataRepository: DataRepository
    private lateinit var encryptionManager: EncryptionManager
    private lateinit var dataFolderPath: File

    @Before
    fun setUp() {
        encryptionManager = mockk()
        dataFolderPath = File("./dataFolderPath")
    }

    @After
    fun tearDown() {
        dataFolderPath.deleteRecursively()
    }

    @Test
    fun test() = runBlocking {
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

}