package dev.moetz.deaddrop

fun combinePartsToFullUrl(isHttps: Boolean, domain: String, pathPrefix: String?): String {
    return buildString {

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

fun combinePartsToPathPrefix(pathPrefix: String?): String {
    return buildString {
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