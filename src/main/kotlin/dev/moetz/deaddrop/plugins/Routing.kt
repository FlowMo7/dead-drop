package dev.moetz.deaddrop.plugins

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*

private inline fun HTML.siteSkeleton(crossinline block: DIV.() -> Unit) {
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
        style {
            unsafe {
                +"""body{display: flex;min-height: 100vh;flex-direction: column;}
main{flex: 1 0 auto;}"""
            }
        }
        meta(name = "robots", content = "index, follow")
        meta(name = "og:title", content = "Dead-Drop: Send secure information")
        meta(name = "description", content = "Create one-time links for securely sending data")
        meta(name = "keywords", content = "drop,password,encrypt,secure,send")
        meta(name = "theme-color", content = "#ff9800")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
    }
    body {
        header {
            nav(classes = "orange") {
                div(classes = "nav-wrapper") {
                    span(classes = "brand-logo center") {
                        unsafe { +"Dead&nbsp;Drop" }
                    }
                }
            }
        }
        main {
            br()
            div(classes = "general-container") {
                div(classes = "container") {
                    block.invoke(this)
                }
            }
        }

        footer(classes = "page-footer orange") {
            div(classes = "container") {
                div(classes = "row") {
                    div(classes = "white-text col l6 s12") {
                        +"Open Source on "
                        a(classes = "blue-text", href = "https://gitlab.moetz.dev/florian/deaddrop") { +"Gitlab" }
                    }
                    div(classes = "col l4 offset-l2 s12") {
                        a(classes = "white-text right", href = "/info") { +"How is this safe?" }
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
                siteSkeleton {
                    div(classes = "section") {
                        id = "send_div"

                        div(classes = "row") {
                            div(classes = "col s12") {
                                +"Want to send something private? A password, love-note or something else, no-one else than the recipient should see?"
                                br()
                                br()
                                +"This service will:"
                                br()
                                unsafe { +"&bullet;&nbsp;" }
                                +"Encrypt your message in your browser with a randomly generated password."
                                br()
                                unsafe { +"&bullet;&nbsp;" }
                                +"Upload the encrypted message to the server (while not sharing the password with the server)"
                                br()
                                unsafe { +"&bullet;&nbsp;" }
                                +"Display the password as well as a link to get the message back, which you can then share with your recipient."
                            }
                        }

                        div(classes = "row") {
                            div(classes = "col s12") {
                                +"Enter your message below:"
                            }
                        }

                        div(classes = "row") {
                            div(classes = "col s12") {
                                textArea(cols = "70", rows = "8") {
                                    style = "min-height:200px;"
                                    name = "message"
                                    id = "drop_content"
                                    placeholder = "Message to encrypt"
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
                                h5(classes = "green-text") {
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

                                            +"I'm sending you some secure information."
                                            br()
                                            +"Location: "
                                            b {
                                                span {
                                                    id = "drop_share_link"
                                                }
                                            }
                                            br()
                                            +"Password: "

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
                siteSkeleton {

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
                                a(classes = "waves-effect waves-light btn orange") {
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

        get("info") {
            call.respondHtml {
                siteSkeleton {
                    div(classes = "section") {
                        h3(classes = "orange-text") { +"How is this safe?" }
                        div {
                            div(classes = "row") {
                                div("col s12") {
                                    +"Here are the steps this platform does with your message:"
                                }
                            }
                            div(classes = "row") {
                                div("col s12") {
                                    unsafe { +"&bullet;&nbsp;" }
                                    +"Once you click on "
                                    i { +"Make the drop!" }
                                    +", the message is encrypted in your browser with a password generated in your browser."
                                    br()
                                    unsafe { +"&bullet;&nbsp;" }
                                    +"This means, that the data does not leave your browser unencrypted, as well as your password."
                                    br()
                                    unsafe { +"&bullet;&nbsp;" }
                                    +"The encrypted data is then sent to the backend, where it is stored for a maximum of $keepFilesTimeInHours hours (or when the drop is fetched, whichever is earlier)."
                                    br()
                                    unsafe { +"&bullet;&nbsp;" }
                                    +"When getting the drop, the encrypted data is fetched from the server (and instantly deleted when doing so), and is only encrypted in the browser. So, also here, the inserted password never leaves the browser."
                                    br()
                                    unsafe { +"&bullet;&nbsp;" }
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
                                    +"The code is "
                                    a(
                                        classes = "orange-text",
                                        href = "https://gitlab.moetz.dev/florian/deaddrop"
                                    ) { +"open source" }
                                    +", and you can easily inspect what is going on on this website with your developer tools."
                                    br()
                                    +"Furthermore, feel free to host your "
                                    a(
                                        classes = "orange-text",
                                        href = "https://gitlab.moetz.dev/florian/deaddrop"
                                    ) { +"own instance of this service" }
                                    +", so that we do not even get to see your encrypted data at any time, so that you do not have to rely on us not trying to decrypt your data."
                                }
                            }
                        }
                    }
                }
            }
        }

        static("static") {
            preCompressed {

                resource(remotePath = "frontend.js", resource = "frontend.js")

                resource(remotePath = "drop.js", resource = "drop.js")

                resource(remotePath = "sjcl.js", resource = "sjcl/sjcl.js")

                resource(remotePath = "materialize.min.css", resource = "materialize/materialize.min.css")
            }
        }

    }
}
