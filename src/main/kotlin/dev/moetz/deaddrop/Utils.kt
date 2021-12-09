package dev.moetz.deaddrop

fun combinePartsToUrl(isHttps: Boolean, domain: String, pathPrefix: String?, useRelativePaths: Boolean): String {
    return buildString {
        if(useRelativePaths) {
            if (pathPrefix != null) {
                if (pathPrefix.startsWith('/').not()) {
                    append('/')
                }
                append(pathPrefix)
                if (pathPrefix.endsWith('/').not()) {
                    append("/")
                }
            } else {
                append("/")
            }
        } else {
            if (isHttps) {
                append("https://")
            } else {
                append("http://")
            }
            append(domain)
            if (pathPrefix != null) {
                if (pathPrefix.startsWith('/').not()) {
                    append('/')
                }
                append(pathPrefix)
                if (pathPrefix.endsWith('/').not()) {
                    append("/")
                }
            } else {
                append("/")
            }
        }
    }
}