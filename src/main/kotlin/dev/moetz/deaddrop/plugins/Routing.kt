package dev.moetz.deaddrop.plugins

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import java.math.BigInteger
import java.security.MessageDigest

private fun FlowContent.isThisSafe(keepFilesTimeInHours: Int) {
    div {
        id = "is_this_safe"

        h3 { +"How is this safe?" }
        +"Here are the steps this platform does with your message:"
        br()
        ul {
            li {
                +"Once you click on "
                i { +"Make the drop!" }
                +", the message is encrypted in your browser with a password generated in your browser."
            }
            li {
                +"This means, that the data does not leave your browser unencrypted, as well as your password."
            }
            li {
                +"The encrypted data is then loaded (using a secure connection) to our servers, where it is stored for a maximum of $keepFilesTimeInHours hours (or when the drop is fetched, whichever is earlier)."
            }
            li {
                +"When getting the drop, the encrypted data is fetched from the server (and instantly deleted when doing so), and is only encrypted in the browser. So, also here, the inserted password never leaves the browser."
            }
            li {
                +"So the server (we) cannot see your message, as we never get the password for it."
            }
        }
        br()
        +"The encryption is algorithm used is "
        a(href = "https://github.com/bitwiseshiftleft/sjcl") { +"github.com/bitwiseshiftleft/sjcl" }
        +", which is a JavaScript crypto library developed at Stanford."
        br()
        +"The code is open source, and you can easily inspect what is going on on this website with your developer tools. Furthermore, feel free to host your own instance of this service, so that we do not even get to see your encrypted data at any time, and so that you do not have to rely on us not trying to decrypt your data."
    }
}

private fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0')
}

private suspend fun ApplicationCall.etagMagic(content: String) {
    val etag = content.md5()
    if (this.request.header(HttpHeaders.IfNoneMatch) == etag) {
        this.respond(status = HttpStatusCode.NotModified, message = "")
    } else {
        this.response.header("ETag", etag)
        this.respondText(contentType = ContentType.Application.JavaScript, text = content)
    }
}

fun Application.configure(domain: String, isHttps: Boolean, keepFilesTimeInHours: Int) {

    routing {

        get("robots.txt") {
            call.respondText(ContentType.Text.Plain) {
                "User-agent: * Allow: /"
            }
        }

        get {
            call.respondHtml {
                head {
                    title("Dead-Drop: Send secure information")
                    link(href = "/static/styles.css", rel = "stylesheet", type = "text/css")
                    script(src = "/static/sjcl.js") {

                    }
                    script(src = "/static/drop.js") {

                    }
                    script(src = "/static/frontend.js") {

                    }
                }
                body {
                    h1 {
                        +"One Time Dead-Drop"
                    }
                    br()
                    div {
                        +"Need to send some data securely? This is the place to do it."
                    }
                    br()
                    br()

                    div {
                        id = "send_div"

                        +"Enter your message below:"
                        br()
                        textArea(cols = "70", rows = "8") {
                            name = "message"
                            id = "drop_content"
                            placeholder = "Enter your message"
                        }


                        br()

                        button {
                            onClick = "sendDrop(document.getElementById('drop_content').value)"
                            +"Make the drop!"
                        }

                    }

                    div {
                        id = "link_div"
                        hidden = true

                        +"Drop made!"
                        br()
                        +"Your recipient needs the link as well as the password to get the drop."
                        br()
                        +"It might be best to just fully copy the message below to your recipient."
                        br()
                        +"Note, that for additional security, the link and password may be sent on separate channels (e.g. mail and a messenger)"

                        br()
                        br()
                        br()

                        div {
                            id = "to_copy"

                            +"Hi,"
                            br()
                            +"I'm sending you some secure information, which can be retrieved by browsing to:"
                            br()
                            +"Location: "
                            span {
                                id = "drop_share_link"
                            }
                            br()
                            +"The password to be entered there is: "
                            span {
                                id = "drop_share_password"
                            }
                            br()
                            br()
                            br()

                            div {
                                +"Warning!"
                                br()
                                +"This drop will only work "
                                b { +"once" }
                                +", so be careful with the password, and make sure to copy the data immediately."
                                br()
                                +"After you pick it up (either successfully or with e.g. a wrong password), the data will self-destruct and won't be available anymore."
                                br()
                                +"This link will only work for $keepFilesTimeInHours hours, after that, the data will self-destruct as well."
                            }

                        }

                        button {
                            onClick = "window.location.assign('${if (isHttps) "https" else "http"}://$domain/')"
                            +"Make another drop"
                        }
                    }

                    isThisSafe(keepFilesTimeInHours)

                }
            }
        }

        get("pickup/{id}") {
            val dropId = call.parameters["id"]
            call.respondHtml {
                head {
                    title("Dead-Drop: Send secure information")
                    link(href = "/static/styles.css", rel = "stylesheet", type = "text/css")
                    script(src = "/static/sjcl.js") {

                    }
                    script(src = "/static/drop.js") {

                    }
                    script(src = "/static/frontend.js") {

                    }
                }
                body {
                    h1 {
                        +"One Time Dead-Drop"
                    }
                    br()
                    div {
                        +"Need to send some data securely? This is the place to do it."
                    }
                    br()
                    br()

                    div {
                        id = "container_get_drop"

                        +"Enter the password"
                        br()
                        +"Enter the password you got provided with this link."
                        br()
                        +"This will only work "
                        b { +"once" }
                        +"! Clicking "
                        i { +"Get the drop" }
                        +" will delete the message permanently, regardless of whether the password was correct or not."

                        textInput {
                            id = "drop_password"
                            placeholder = "Enter the password here"
                        }

                        br()

                        button {
                            onClick = "getDrop('$dropId', document.getElementById('drop_password').value)"
                            +"Get the drop"
                        }
                    }

                    div {
                        id = "drop_content"
                    }

                    div {
                        id = "error_text"
                        hidden = true

                        +"There was an error getting your drop. This can either be:"
                        ul {
                            li {
                                +" The drop has already been fetched. You can only open / get a drop once. If you didn't get the drop yet, it might be that someone else tried (and if they got the password, may have succeeded) to get your drop. If you are unsure, you should consider that the dropped content is no longer safe in this scenario."
                            }
                            li {
                                +"The entered password was wrong. You can only open / get a drop once, and if you enter the wrong password, the drop is gone forever. If you didn't get the drop yet, it might be that someone else tried (and if they got the password, may have succeeded) to get your drop. If you are unsure, you should consider that the dropped content is no longer safe in this scenario."
                            }
                        }

                        br()

                        +"In each case, if neither you nor the creator of the drop took actions that could lead to this scenario, please consider that the content of your drop may be in malicious hands as of now."
                    }

                    isThisSafe(keepFilesTimeInHours)
                }
            }
        }

        static("static") {
            preCompressed {

                get("styles.css") {
                    call.respondText(contentType = ContentType.Text.CSS) {
                        """

"""
                    }
                }

                get("frontend.js") {
                    val content = """function sendDrop(data) {
    encryptAndPostDrop(data, function(id, password) {
        document.getElementById('drop_content').value = '';
        showDropLink(id, password);
    });
}

function showDropLink(id, password) {
    document.getElementById('send_div').style.display = 'none';
    document.getElementById('link_div').style.display = 'block';
    document.getElementById('drop_share_link').innerHTML = '${if (isHttps) "https" else "http"}://$domain/pickup/' + id;
    document.getElementById('drop_share_password').innerHTML = password;
}

function getDrop(id, password) {
    fetchDropAndDecrypt(
        id,
        password,
        function(data) {
            document.getElementById('drop_content').innerHTML = data;
            document.getElementById('container_get_drop').style.display = 'none';
        },
        function() {
            document.getElementById('drop_content').style.display = 'none';
            document.getElementById('container_get_drop').style.display = 'none';
            document.getElementById('error_text').style.display = 'block';
        }
    );
}
"""
                    call.etagMagic(content)
                }

                get("drop.js") {
                    val content = """function encryptAndPostDrop(plainData, onComplete) {
    var generatedPassword = generateStringSequence(16);
    var encrypted = sjcl.encrypt(generatedPassword, plainData);
    
    post('/api/drop', encrypted, function(data) {
        onComplete(data.id, generatedPassword);
    });
}

function fetchDropAndDecrypt(id, password, onLoaded, onError) {
    get('/api/drop/' + id, function(statusCode, data) {
        if (statusCode == 200) {
            try {
                var decrypted = sjcl.decrypt(password, data);
                onLoaded(decrypted);
            } catch(e) {
                onError();
            }
        } else {
            onError();
        }
    });
}

function generateStringSequence(length) {
    var result           = '';
    var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < length; i++) {
      result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

function post(path, content, onComplete) {
    var request = new XMLHttpRequest();
    request.open("POST", path, true);
    request.setRequestHeader('Content-Type', 'application/json');
    request.onload = function() {
        onComplete(JSON.parse(this.responseText));
    };
    request.send(content);
}

function get(path, onComplete) {
    var request = new XMLHttpRequest();
    request.open("GET", path, true);
    request.onload = function() {
        onComplete(this.status, this.responseText);
    };
    request.send();
}
"""
                    call.etagMagic(content)
                }

                resource(remotePath = "sjcl.js", resource = "sjcl/sjcl.js")
            }
        }

    }
}
