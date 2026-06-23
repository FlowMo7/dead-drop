package dev.moetz.deaddrop

import dev.moetz.deaddrop.Localization.Language
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

private val String.asLanguage: Language?
    get() {
        return if (this.contains("-")) {
            Language.entries.firstOrNull {
                it.identifier.equals(
                    this.substringBefore("-").trim(),
                    ignoreCase = true
                )
            }
        } else {
            Language.entries.firstOrNull { it.identifier.equals(this.trim(), ignoreCase = true) }
        }
    }

class Localization(
    val currentLanguage: Language,
) {

    constructor(locale: String?) : this(locale?.asLanguage ?: Language.EN)

    private val json: Json = Json

    enum class Language(val identifier: String) {
        EN("en"),
        DE("de"),
    }



    @OptIn(ExperimentalSerializationApi::class)
    private val entries: Map<String, String> by lazy {
        this.javaClass.getResourceAsStream("./locales/${currentLanguage.identifier}/strings.json")!!.use { stream ->
            json.decodeFromStream(
                MapSerializer<String, String>(String.serializer(), String.serializer()),
                stream,
            )
        }
    }

    operator fun get(key: String): String {
        return entries[key].orEmpty()
    }

    fun get(key: String, vararg arguments: Any): String {
        var entry = entries[key].orEmpty()
        arguments.forEachIndexed { index, value ->
            val number = index + 1
            entry = when (value) {
                is Int, is Long -> entry.replace("%$number${'$'}d", value.toString())
                is Float, is Double -> entry.replace("%$number${'$'}f", value.toString())
                else -> entry.replace("%$number${'$'}s", value.toString())
            }
        }
        return entry
    }


}