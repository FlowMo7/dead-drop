//package dev.moetz.isxyz
//
//import dev.moetz.isxyz.data.DataSource
//import dev.moetz.isxyz.plugins.configure
//import io.ktor.http.*
//import io.ktor.server.testing.*
//import org.junit.After
//import org.junit.Before
//import java.io.File
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//class ApplicationTest {
//
//    private lateinit var contentFile: File
//    private lateinit var colorFile: File
//    private lateinit var dataSource: DataSource
//    private lateinit var adminUsername: String
//    private lateinit var adminPassword: String
//
//    @Before
//    fun setUp() {
//        contentFile = File("./test_content_${this.hashCode()}")
//        colorFile = File("./test_color_${this.hashCode()}")
//
//        dataSource = DataSource(contentFile = contentFile, colorFile = colorFile)
//
//        adminUsername = "admin"
//        adminPassword = "This1Is2A3Password4"
//    }
//
//    @After
//    fun tearDown() {
//        contentFile.delete()
//        colorFile.delete()
//    }
//
//    @Test
//    fun testSetGetAuthorization() {
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(HttpMethod.Get, "/set").apply {
//                assertEquals(HttpStatusCode.Unauthorized, response.status())
//            }
//        }
//    }
//
//    @Test
//    fun testSetGetWithTrailingSlashRedirect() {
//        contentFile.delete()
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/set/",
//                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//        }
//    }
//
//    @Test
//    fun testSetGetWithNonExistingFile() {
//        contentFile.delete()
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/set",
//                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {
//
//
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals(
//                    """<!DOCTYPE html>
//<html>
//  <head>
//    <title>Set the Dark Souls Counter Text</title>
//  </head>
//  <body>
//    <div>
//      <p>Enter the text to display:</p>
//      <form action="/set" method="post"><input type="text" name="content" value=""><input type="submit"><br><br><br>Hex Color-Code: #<input type="text" name="color" value="FFFFFF"></form>
//    </div>
//  </body>
//</html>
//""", response.content
//                )
//            }
//        }
//    }
//
//    @Test
//    fun testSetGetWithEmptyFile() {
//        contentFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/set",
//                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {
//
//
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals(
//                    """<!DOCTYPE html>
//<html>
//  <head>
//    <title>Set the Dark Souls Counter Text</title>
//  </head>
//  <body>
//    <div>
//      <p>Enter the text to display:</p>
//      <form action="/set" method="post"><input type="text" name="content" value=""><input type="submit"><br><br><br>Hex Color-Code: #<input type="text" name="color" value="FFFFFF"></form>
//    </div>
//  </body>
//</html>
//""", response.content
//                )
//            }
//        }
//    }
//
//    @Test
//    fun testSetGetWithNonEmptyFileHelloWorld() {
//        contentFile.writeText("Hello World!")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/set",
//                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {
//
//
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals(
//                    """<!DOCTYPE html>
//<html>
//  <head>
//    <title>Set the Dark Souls Counter Text</title>
//  </head>
//  <body>
//    <div>
//      <p>Enter the text to display:</p>
//      <form action="/set" method="post"><input type="text" name="content" value="Hello World!"><input type="submit"><br><br><br>Hex Color-Code: #<input type="text" name="color" value="FFFFFF"></form>
//    </div>
//  </body>
//</html>
//""", response.content
//                )
//            }
//        }
//    }
//
//    @Test
//    fun testSetGetWithNonEmptyFileWithUmlaute() {
//        contentFile.writeText("Hello World! äöüÖÄÜß")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/set",
//                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {
//
//
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals(
//                    """<!DOCTYPE html>
//<html>
//  <head>
//    <title>Set the Dark Souls Counter Text</title>
//  </head>
//  <body>
//    <div>
//      <p>Enter the text to display:</p>
//      <form action="/set" method="post"><input type="text" name="content" value="Hello World! äöüÖÄÜß"><input type="submit"><br><br><br>Hex Color-Code: #<input type="text" name="color" value="FFFFFF"></form>
//    </div>
//  </body>
//</html>
//""", response.content
//                )
//            }
//        }
//    }
//
//    @Test
//    fun testSetGetWithNonEmptyFileWithHtmlCharacters() {
//        contentFile.writeText("<i>Hello, world!</i><script>alert('Test');</script>")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/set",
//                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {
//
//
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals(
//                    """<!DOCTYPE html>
//<html>
//  <head>
//    <title>Set the Dark Souls Counter Text</title>
//  </head>
//  <body>
//    <div>
//      <p>Enter the text to display:</p>
//      <form action="/set" method="post"><input type="text" name="content" value="&amp;lt;i&amp;gt;Hello, world!&amp;lt;/i&amp;gt;&amp;lt;script&amp;gt;alert(&amp;#x27;Test&amp;#x27;);&amp;lt;/script&amp;gt;"><input type="submit"><br><br><br>Hex Color-Code: #<input type="text" name="color" value="FFFFFF"></form>
//    </div>
//  </body>
//</html>
//""", response.content
//                )
//            }
//        }
//    }
//
//
//    @Test
//    fun testIndexGetPage() {
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/",
//                setup = {  }).apply {
//
//
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals(
//                    """<!DOCTYPE html>
//<html>
//<head>
//<title>Dark Souls Counter</title>
//</head>
//<body">
//<div id="loaded_content" style="font-size: 4em;"></<div>
//</body>
//<script>
//function loadContent() {
//    var xhr = new XMLHttpRequest();
//    xhr.open('GET', '/content', true);
//    xhr.onreadystatechange = function() {
//        if (this.readyState !== 4) return;
//        if (this.status !== 200) return;
//        document.getElementById('loaded_content').innerHTML = this.responseText;
//    };
//    xhr.send();
//}
//
//var intervalId = setInterval(function() {
//  loadContent();
//}, 5000);
//</script>
//</html>
//""", response.content
//                )
//            }
//        }
//    }
//
//
//    @Test
//    fun testSetPostAuthorization() {
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(HttpMethod.Post, "/set").apply {
//                assertEquals(HttpStatusCode.Unauthorized, response.status())
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithMissingParameter() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = { addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==") }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\"></div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithEmptyParameter1() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    setBody("content=")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("", contentFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\"></div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithEmptyParameter2() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    setBody("content=&color=")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\"></div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithEmptyParameter3() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    setBody("color=")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\"></div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithEmptyParameter4() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    setBody("color=&content=")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\"></div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithColorEmpty() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=Test&color=")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("Test", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\">Test</div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithColorInvalid1() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=Test&color=ASD")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("Test", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\">Test</div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithColorInvalid2() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=Test&color=<script>alert('Test');</script>")
//                }).apply {
//
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("Test", contentFile.readText())
//            assertEquals("", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\">Test</div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithHelloWorldParameter() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=Hello+World&color=FFFFFF")
//                }).apply {
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("Hello World", contentFile.readText())
//            assertEquals("FFFFFF", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#FFFFFF;\">Hello World</div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithHtml() {
//        contentFile.writeText("")
//        colorFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=<i>Hello World!</i>&color=AFAFAF")
//                }).apply {
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("<i>Hello World!</i>", contentFile.readText())
//            assertEquals("AFAFAF", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#AFAFAF;\">&lt;i&gt;Hello World!&lt;/i&gt;</div>", response.content)
//            }
//        }
//    }
//
//    @Test
//    fun testSetPostWithAlertInjectionTest() {
//        contentFile.writeText("")
//        withTestApplication({ configure(dataSource, adminUsername, adminPassword) }) {
//            handleRequest(
//                method = HttpMethod.Post,
//                uri = "/set",
//                setup = {
//                    addHeader("Authorization", "Basic YWRtaW46VGhpczFJczJBM1Bhc3N3b3JkNA==")
//                    addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    setBody("content=<script>alert('Hello');</script>&color=000000")
//                }).apply {
//
//                assertEquals(HttpStatusCode.Found, response.status())
//                assertEquals("/set", response.headers["Location"])
//            }
//
//            assertEquals("<script>alert('Hello');</script>", contentFile.readText())
//            assertEquals("000000", colorFile.readText())
//
//            handleRequest(
//                method = HttpMethod.Get,
//                uri = "/content"
//            ).apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("<div style=\"color:#000000;\">&lt;script&gt;alert(&#x27;Hello&#x27;);&lt;/script&gt;</div>", response.content)
//            }
//        }
//    }
//
//}