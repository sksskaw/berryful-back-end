package berryful.lounge.api.config

import com.fasterxml.jackson.core.SerializableString
import com.fasterxml.jackson.core.io.CharacterEscapes
import com.fasterxml.jackson.core.io.SerializedString
import org.apache.commons.text.StringEscapeUtils
import org.apache.commons.text.translate.AggregateTranslator
import org.apache.commons.text.translate.CharSequenceTranslator
import org.apache.commons.text.translate.EntityArrays
import org.apache.commons.text.translate.LookupTranslator
import java.util.*


class HTMLCharacterEscapes : CharacterEscapes() {
    private var asciiEscapes: IntArray = standardAsciiEscapesForJSON()
    private var translator: CharSequenceTranslator? = null

    init {
        val customMap: MutableMap<CharSequence, CharSequence> = HashMap()
        customMap["("] = "&#40;"
        customMap[")"] = "&#41;"
        customMap["#"] = "&#35;"
        customMap["\'"] = "&#39;"

        asciiEscapes['<'.code] = ESCAPE_CUSTOM
        asciiEscapes['>'.code] = ESCAPE_CUSTOM
        asciiEscapes['&'.code] = ESCAPE_CUSTOM
        asciiEscapes['('.code] = ESCAPE_CUSTOM
        asciiEscapes[')'.code] = ESCAPE_CUSTOM
        asciiEscapes['#'.code] = ESCAPE_CUSTOM
        asciiEscapes['\"'.code] = ESCAPE_CUSTOM
        asciiEscapes['\''.code] = ESCAPE_CUSTOM

        translator = AggregateTranslator(
            LookupTranslator(EntityArrays.BASIC_ESCAPE),  // <, >, &, " 는 여기에 포함됨
            LookupTranslator(EntityArrays.ISO8859_1_ESCAPE),
            LookupTranslator(EntityArrays.HTML40_EXTENDED_ESCAPE),
            LookupTranslator(customMap)
        )
    }

    override fun getEscapeCodesForAscii(): IntArray {
        return asciiEscapes

    }

    override fun getEscapeSequence(ch: Int): SerializableString {
        val serializedString: SerializedString
        val charAt = ch.toChar()

        // 이모지 변환
        if (Character.isHighSurrogate(charAt) || Character.isLowSurrogate(charAt)) {
            val stringBuilder = StringBuilder()
            stringBuilder.append("\\u")
            stringBuilder.append(String.format("%04x", ch))
            serializedString = SerializedString(stringBuilder.toString())
        } else {
            serializedString = SerializedString(translator?.translate(ch.toChar().toString()))
        }

        return serializedString
    }
}