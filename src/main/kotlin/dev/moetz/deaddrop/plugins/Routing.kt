package dev.moetz.deaddrop.plugins

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.configure(domain: String, isHttps: Boolean) {

    routing {

        get("robots.txt") {
            call.respondText(ContentType.Text.Plain) {
                "User-agent: * Disallow: /"
            }
        }

        get {
            call.respondHtml {
                head {
                    title("Dead-Drop: Send secure information")
                    script(src = "/static/script.js") {

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

                        textArea(cols = "70") {
                            name = "message"
                            id = "drop_content"
                            placeholder = "Enter your message"
                        }

                        button {
                            onClick = "sendDrop()"
                            +"Send now"
                        }

                    }


                    div {
                        id = "link_div"

                        span {
                            id = "drop_share_link"
                        }
                    }

                }
            }
        }

        get("pickup/{id}") {
            val dropId = call.parameters["id"]
            call.respondHtml {
                head {
                    title("Dead-Drop: Send secure information")
                    script(src = "/static/script.js") {

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
                        button {
                            id = "btn_get_drop"
                            onClick = "getDrop('$dropId')"
                            +"Get Drop"
                        }
                    }


                    div {
                        id = "drop_content"
                    }

                }
            }
        }

        route("static") {
            get("script.js") {
                call.respondText(contentType = ContentType.Application.JavaScript) {
                    """function sendDrop() {
    var request = new XMLHttpRequest();
    request.open("POST", '/api/drop', true);
    request.setRequestHeader('Content-Type', 'application/json');
    request.onload = function() {
        var data = JSON.parse(this.responseText);
        console.log(data);
        document.getElementById('drop_content').value = '';
        showDropLink(data.id);
    };
    request.send(document.getElementById('drop_content').value);
}

function showDropLink(id) {
    document.getElementById('send_div').style.display = 'none';
    document.getElementById('link_div').style.display = 'block';
    document.getElementById('drop_share_link').innerHTML = '${if (isHttps) "https" else "http"}://$domain/pickup/' + id + '';
}


function getDrop(id) {
    var request = new XMLHttpRequest();
    request.open("GET", '/api/drop/' + id, true);
    request.setRequestHeader('Content-Type', 'application/json');
    request.onload = function() {
        console.log(this.responseText);
        document.getElementById('drop_content').innerHTML = this.responseText;
        document.getElementById('btn_get_drop').style.display = 'none';
    };
    request.send();
}
"""
                }
            }
        }

    }
}
