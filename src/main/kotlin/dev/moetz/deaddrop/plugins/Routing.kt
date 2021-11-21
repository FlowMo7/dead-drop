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

private inline fun HTML.siteSkeleton(keepFilesTimeInHours: Int, crossinline block: DIV.() -> Unit) {
    head {
        charset("utf-8")
        title("Dead-Drop: Send secure information")
        link(href = "/static/materialize.min.css", rel = "stylesheet", type = "text/css")
        script(src = "/static/sjcl.js") {

        }
        script(src = "/static/drop.js") {

        }
        script(src = "/static/frontend.js") {

        }
        meta(name = "robots", content = "index, follow")
        meta(name = "og:title", content = "Dead-Drop: Send secure information")
        meta(name = "description", content = "Create one-time links for securely sending data")
        meta(name = "keywords", content = "drop,password,encrypt,secure,send")
    }
    body {
        nav(classes = "orange") {
            div(classes = "nav-wrapper") {
                a(href = "/", classes = "brand-logo center") {
                    +"One-Time Dead Drop"
                }
            }
        }
        br()
        div(classes = "container") {

            block.invoke(this)

            div(classes = "divider") {

            }
            div(classes = "section") {
                id = "is_this_safe"

                h3 {
                    onClick = "toggleIsThisSafeVisible()"
                    a(classes = "orange-text", href = "#is_this_safe") {
                        +"How is this safe?"
                    }
                }
                div {
                    id = "container_is_this_safe"
                    style = "display: none;"

                    div(classes = "row") {
                        div("col s12") {
                            +"Here are the steps this platform does with your message:"
                        }
                    }
                    div(classes = "row") {
                        div("col s12") {
                            +"Once you click on "
                            i { +"Make the drop!" }
                            +", the message is encrypted in your browser with a password generated in your browser."
                            br()
                            +"This means, that the data does not leave your browser unencrypted, as well as your password."
                            br()
                            +"The encrypted data is then loaded (using a secure connection) to our servers, where it is stored for a maximum of $keepFilesTimeInHours hours (or when the drop is fetched, whichever is earlier)."
                            br()
                            +"When getting the drop, the encrypted data is fetched from the server (and instantly deleted when doing so), and is only encrypted in the browser. So, also here, the inserted password never leaves the browser."
                            br()
                            +"So the server (we) cannot see your message, as we never get the password for it."

                            br()
                            br()

                            +"The encryption is algorithm used is "
                            a(
                                classes = "orange-text",
                                href = "https://github.com/bitwiseshiftleft/sjcl"
                            ) { +"github.com/bitwiseshiftleft/sjcl" }
                            +", which is a JavaScript crypto library developed at Stanford."
                            br()
                            +"The code is open source, and you can easily inspect what is going on on this website with your developer tools. Furthermore, feel free to host your own instance of this service, so that we do not even get to see your encrypted data at any time, and so that you do not have to rely on us not trying to decrypt your data."
                        }
                    }
                }
            }
        }
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
                siteSkeleton(keepFilesTimeInHours) {
                    div(classes = "section") {
                        id = "send_div"

                        div(classes = "row") {
                            div(classes = "col s6") {
                                +"Enter your message below:"
                            }
                        }

                        div(classes = "row") {
                            div(classes = "col s12") {
                                textArea(cols = "70", rows = "8") {
                                    style = "min-height:200px;"
                                    name = "message"
                                    id = "drop_content"
                                    placeholder = "Enter your message"
                                }
                            }
                            div(classes = "col s12") {
                                a(classes = "waves-effect waves-light btn orange") {
                                    onClick = "sendDrop(document.getElementById('drop_content').value)"
                                    +"Make the drop!"
                                }
                            }
                        }
                    }

                    div(classes = "section") {
                        id = "link_div"
                        hidden = true

                        div("row") {
                            div(classes = "col s12") {
                                h5 {
                                    +"Drop made!"
                                }
                            }
                        }
                        div("row") {
                            div(classes = "col s12") {
                                +"Your recipient needs the link as well as the password to get the drop."
                            }
                            div(classes = "col s12") {
                                +"It might be best to just fully copy the message below to your recipient."
                            }
                            div(classes = "col s12") {
                                +"Note, that for additional security, the link and password may be sent on separate channels (e.g. mail and a messenger)"
                            }
                        }

                        div(classes = "row") {
                            div(classes = "col s10") {
                                div(classes = "card blue-grey darken-1") {
                                    div(classes = "card-content black-text orange accent-1") {
                                        span(classes = "card-title") { +"Hi," }
                                        p {
                                            id = "message_to_share_drop"

                                            +"I'm sending you some secure information, which can be retrieved by browsing to:"
                                            br()
                                            +"Location: "
                                            b {
                                                span {
                                                    id = "drop_share_link"
                                                }
                                            }
                                            br()
                                            +"The password to be entered there is: "

                                            b {
                                                span {
                                                    id = "drop_share_password"
                                                }
                                            }
                                            br()
                                            br()

                                            b { +"Warning!" }
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
                                }
                            }
                        }

                        a(classes = "waves-effect waves-light btn-small orange") {
                            onClick = "window.location.assign('${if (isHttps) "https" else "http"}://$domain/')"
                            +"Make another drop"
                        }
                    }
                }
            }
        }


        get("pickup/{id}") {
            val dropId = call.parameters["id"]
            call.respondHtml {
                siteSkeleton(keepFilesTimeInHours) {

                    div(classes = "section") {
                        id = "container_get_drop"

                        div("row") {
                            div("col s12") {
                                +"Enter the password you got provided with this link."
                            }
                        }
                        div("row") {
                            div("col s12") {
                                +"This will only work "
                                b { +"once" }
                                +"! Clicking "
                                i { +"Get the drop" }
                                +" will delete the message permanently, regardless of whether the password was correct or not."
                            }
                        }

                        div("row") {
                            div("input-field col s12") {
                                textInput(classes = "validate") {
                                    id = "drop_password"
                                    placeholder = "Enter the password here"
                                }
                            }
                            div(classes = "col s12") {
                                a(classes = "waves-effect waves-light btn") {
                                    onClick = "getDrop('$dropId', document.getElementById('drop_password').value)"
                                    +"Get the drop"
                                }
                            }
                        }
                    }

                    div(classes = "section") {
                        id = "drop_content_section"
                        hidden = true

                        h3 {
                            +"Your drop:"
                        }

                        div(classes = "divider") {

                        }

                        div(classes = "row") {
                            div(classes = "col s12") {
                                pre {
                                    id = "drop_content"
                                }
                            }
                        }
                    }


                    div(classes = "section") {
                        id = "error_text"
                        hidden = true

                        div(classes = "row") {
                            h3(classes = "red-text") {
                                +"Error"
                            }
                        }
                        div(classes = "row") {
                            div(classes = "col s12") {
                                +"There was an error getting your drop. This can either be:"
                                br()
                                br()

                                +"The drop has already been fetched. You can only open / get a drop once. If you didn't get the drop yet, it might be that someone else tried (and if they got the password, may have succeeded) to get your drop. If you are unsure, you should consider that the dropped content is no longer safe in this scenario."

                                br()
                                br()
                                +"The entered password was wrong. You can only open / get a drop once, and if you enter the wrong password, the drop is gone forever. If you didn't get the drop yet, it might be that someone else tried (and if they got the password, may have succeeded) to get your drop. If you are unsure, you should consider that the dropped content is no longer safe in this scenario."

                                br()
                                br()
                                b {
                                    +"In each case, if neither you nor the creator of the drop took actions that could lead to this scenario, please consider that the content of your drop may be in malicious hands as of now."
                                }
                            }
                        }
                    }

                }
            }
        }

        static("static") {
            preCompressed {

                get("frontend.js") {
                    val content = """function sendDrop(data) {
    encryptAndPostDrop(data, function(id, password) {
        document.getElementById('drop_content').value = '';
        showDropLink(id, password);
    });
}

function toggleIsThisSafeVisible() {
    if (document.getElementById('container_is_this_safe').style.display == 'none') {
        document.getElementById('container_is_this_safe').style.display = 'block';
    } else {
        document.getElementById('container_is_this_safe').style.display = 'none';
    }
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
            document.getElementById('drop_content_section').style.display = 'block';
            document.getElementById('container_get_drop').style.display = 'none';
        },
        function() {
            document.getElementById('drop_content_section').style.display = 'none';
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

                resource(remotePath = "materialize.min.css", resource = "materialize/materialize.min.css")
            }
        }

    }
}
