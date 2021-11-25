package dev.moetz.isxyz

import dev.moetz.deaddrop.data.DataRepository
import dev.moetz.deaddrop.plugins.configureApi
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    private lateinit var dataRepository: DataRepository

    @Before
    fun setUp() {
        dataRepository = mockk()
    }

    @Test
    fun `post drop will succeed with response with default domain and https`() {
        coEvery { dataRepository.addDrop(any()) } returns "some-id"

        withTestApplication({ configureApi(dataRepository, true, "drop.moetz.dev") }) {
            handleRequest(
                method = HttpMethod.Post,
                uri = "/api/drop",
                setup = { setBody("some-content") }).apply {

                coVerify { dataRepository.addDrop("some-content") }
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("{\"pickupUrl\": \"https://drop.moetz.dev/pickup/some-id\"}", response.content)
            }
        }
    }

    @Test
    fun `post drop will succeed with response with a different domain and https`() {
        coEvery { dataRepository.addDrop(any()) } returns "some-id"

        withTestApplication({ configureApi(dataRepository, true, "drop.example.com") }) {
            handleRequest(
                method = HttpMethod.Post,
                uri = "/api/drop",
                setup = { setBody("some-content") }).apply {

                coVerify { dataRepository.addDrop("some-content") }
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("{\"pickupUrl\": \"https://drop.example.com/pickup/some-id\"}", response.content)
            }
        }
    }

    @Test
    fun `post drop will succeed with response with a different domain and http`() {
        coEvery { dataRepository.addDrop(any()) } returns "some-id"

        withTestApplication({ configureApi(dataRepository, false, "drop.example.com") }) {
            handleRequest(
                method = HttpMethod.Post,
                uri = "/api/drop",
                setup = { setBody("some-content") }).apply {

                coVerify { dataRepository.addDrop("some-content") }
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("{\"pickupUrl\": \"http://drop.example.com/pickup/some-id\"}", response.content)
            }
        }
    }

    @Test
    fun `get drop will succeed when drop is available in dataRepository`() {
        coEvery { dataRepository.getDrop(any()) } returns "some-content"

        withTestApplication({ configureApi(dataRepository, true, "drop.moetz.dev") }) {
            handleRequest(method = HttpMethod.Get, uri = "/api/drop/some-id").apply {

                coVerify { dataRepository.getDrop("some-id") }
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("some-content", response.content)
            }
        }
    }

    @Test
    fun `get drop will 404 when drop is not available in dataRepository`() {
        coEvery { dataRepository.getDrop(any()) } returns null

        withTestApplication({ configureApi(dataRepository, true, "drop.moetz.dev") }) {
            handleRequest(method = HttpMethod.Get, uri = "/api/drop/some-id").apply {

                coVerify { dataRepository.getDrop("some-id") }
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals(null, response.content)
            }
        }
    }

    @Test
    fun `get drop will Server Error when dataRepository throws`() {
        coEvery { dataRepository.getDrop(any()) } throws IllegalStateException("some exception")

        withTestApplication({ configureApi(dataRepository, true, "drop.moetz.dev") }) {
            handleRequest(method = HttpMethod.Get, uri = "/api/drop/some-id").apply {

                coVerify { dataRepository.getDrop("some-id") }
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals(null, response.content)
            }
        }
    }

    @Test
    fun `get drop without id will 404`() {
        withTestApplication({ configureApi(dataRepository, true, "drop.moetz.dev") }) {
            handleRequest(method = HttpMethod.Get, uri = "/api/drop").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun `get drop without id but with slash will 404`() {
        withTestApplication({ configureApi(dataRepository, true, "drop.moetz.dev") }) {
            handleRequest(method = HttpMethod.Get, uri = "/api/drop/").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

}