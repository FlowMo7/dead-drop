package dev.moetz.deaddrop.template

import kotlinx.html.*

class InfoTemplate(
    pathPrefix: String?,
    showGithubLinkInFooter: Boolean,
    colorCode: String,
    showLinkToInfoPage: Boolean = true,
    siteTitle: String,
    siteTitleShort: String,
    private val keepFilesTimeInHours: Int,
) : SiteTemplate(
    pathPrefix = pathPrefix,
    showGithubLinkInFooter = showGithubLinkInFooter,
    colorCode = colorCode,
    showLinkToInfoPage = showLinkToInfoPage,
    siteTitle = siteTitle,
    siteTitleShort = siteTitleShort,
) {

    override fun FlowContent.content() {
        div(classes = "section") {
            h3 { +"How is this safe?" }
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

                        +"The encryption algorithm used is "
                        a(href = "https://github.com/bitwiseshiftleft/sjcl") {
                            target = "_blank"
                            +"github.com/bitwiseshiftleft/sjcl"
                        }
                        +", which is a JavaScript crypto library developed at Stanford."
                        br()
                        +"The code is "
                        a(href = "https://github.com/FlowMo7/dead-drop") {
                            target = "_blank"
                            +"open source"
                        }
                        +", and you can easily inspect what is going on on this website with your developer tools."
                        br()
                        +"Furthermore, feel free to host your "
                        a(href = "https://github.com/FlowMo7/dead-drop") {
                            target = "_blank"
                            +"own instance of this service"
                        }
                        +", so that we do not even get to see your encrypted data at any time, so that you do not have to rely on us not trying to decrypt your data."
                    }
                }
            }
        }
    }

}