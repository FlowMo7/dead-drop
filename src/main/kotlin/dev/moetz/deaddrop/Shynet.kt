package dev.moetz.deaddrop

import kotlinx.html.BODY
import kotlinx.html.img
import kotlinx.html.noScript
import kotlinx.html.script

class Shynet(
    private val host: String?,
    private val id: String?,
    private val doNotTrack: Boolean = false,
) {


    fun attachToBody(body: BODY) {
        if (doNotTrack == false && host != null && host.isNotBlank() && id != null && id.isNotBlank()) {
            body.apply {
                noScript {
                    img {
                        src = "https://$host/ingress/$id/pixel.gif"
                    }
                }
                script {
                    defer = true
                    src = "https://$host/ingress/$id/script.js"
                }
            }
        }
    }

}