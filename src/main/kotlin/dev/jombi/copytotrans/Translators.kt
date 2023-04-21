package dev.jombi.copytotrans

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

interface Translator {
    fun translate(target: String): String
}

interface Translators {
    val translator: Translator
    fun translate(string: String): String = translator.translate(string)

    object Google : Translators {
        override val translator = GoogleTranslate()
    }
    object Papago : Translators {
        override val translator = PapagoTranslate()
    }
}

class PapagoTranslate : Translator {
    val from = "auto"
    val to = "ko"
    override fun translate(target: String): String {
        val con = URL("https://openapi.naver.com/v1/papago/n2mt").openConnection() as HttpURLConnection
        con.doOutput = true
        con.doInput = true
        con.requestMethod = "POST"
        con.addRequestProperty("X-Naver-Client-Id", getPapagoApiKey())
        con.addRequestProperty("X-Naver-Client-Secret", getPapagoApiSecret())
        val body = buildUrlEncoded("source" to from, "target" to to, "text" to target)
        con.outputStream.use {
            it.write(body.toByteArray())
            it.flush()
        }
        con.connect()
        return if (con.responseCode !in 200..299) {
            val err = mapper.readTree(con.errorStream)
            if (err.has("errorMessage")) err["errorMessage"].asText() else "${con.responseCode}"
        } else {
            val res = mapper.readTree(con.inputStream)
            res["message"]["result"]["translatedText"].asText()
        }
    }

}

class GoogleTranslate : Translator {
    val IP_REGEX = Regex("IP address: (.+?)<br>")
    val TIME_REGEX = Regex("Time: (.+?)<br>")
    val URL_REGEX = Regex("URL: (.+?)<br>")
    val AMP_REGEX = Regex("&amp;")
    val url = arrayOf(
        "https://translate.google.com/translate_a/single",
        "?client=at",
        "&dt=t",  // return sentences
        "&dt=rm", // add translit to sentences
        "&dj=1",  // result as pretty json instead of deep nested arrays;
    ).joinToString("")

    val from = "auto"
    val to = "ko"
    override fun translate(target: String): String {
        val params = buildUrlEncoded("sl" to from, "tl" to to, "q" to target)
        val con = URL(url).openConnection() as HttpURLConnection
        con.requestMethod = "POST"
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
        con.doOutput = true
        con.doInput = true
        con.outputStream.use {
            it.write(params.toByteArray())
            it.flush()
        }
        con.connect()
        return if (con.responseCode !in 200..299) {
            val base = String(con.errorStream.readBytes())
            if (con.responseCode == 429) buildManyRequest(base) else "Status: ${con.responseCode}"
        } else {
            val t = mapper.readTree(con.inputStream)
            if (!t.has("sentences")) ""
            else t["sentences"].first()["trans"].asText()
        }
    }

    fun buildManyRequest(string: String): String {
        val ip = IP_REGEX.find(string)?.groupValues?.firstOrNull() ?: ""
        val time = TIME_REGEX.find(string)?.groupValues?.firstOrNull() ?: ""
        val url = (URL_REGEX.find(string)?.groupValues?.firstOrNull() ?: "").replace(AMP_REGEX, "&")
        return "ip = $ip, time = $time, url = $url"
    }
}

fun ObjectMapper.buildJson(vararg pairs: Pair<String, Any?>): String = writeValueAsString(mapOf(*pairs))
fun buildUrlEncoded(vararg pairs: Pair<String, Comparable<*>>) = pairs.joinToString("&") { "${URLEncoder.encode(it.first, "utf-8")}=${URLEncoder.encode("${it.second}", "utf-8")}" }
/*
export class Translator {
  protected options: typeof defaults & TranslateOptions;

  constructor(protected inputText: string, options?: TranslateOptions) {
    this.options = Object.assign({}, defaults, options);
  }

  async translate() {
    const url = this.buildUrl();
    const fetchOptions = this.buildFetchOptions();
    const res = await fetch(url, fetchOptions);
    if (!res.ok) throw await this.buildError(res);
    const raw = await res.json() as RawResponse;
    const text = this.buildResText(raw);
    return { text, raw };
  }

  protected buildUrl() {
    const { host } = this.options;
    return [
      `https://${host}/translate_a/single`,
      '?client=at',
      '&dt=t',  // return sentences
      '&dt=rm', // add translit to sentences
      '&dj=1',  // result as pretty json instead of deep nested arrays
    ].join('');
  }

  protected buildBody() {
    const { from, to } = this.options;
    const params = {
      sl: from,
      tl: to,
      q: this.inputText,
    };
    return new URLSearchParams(params).toString();
  }

  protected buildFetchOptions() {
    const { fetchOptions } = this.options;
    const res = Object.assign({}, fetchOptions);
    res.method = 'POST';
    res.headers = Object.assign({}, res.headers, {
      'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8',
    });
    res.body = this.buildBody();
    return res;
  }

  protected buildResText({ sentences }: RawResponse) {
    return sentences
      .filter((s): s is Sentence => 'trans' in s)
      .map(s => s.trans)
      .join('');
  }

  protected async buildError(res: Response) {
    if (res.status === 429) {
      const text = await res.text();
      const { ip, time, url } = extractTooManyRequestsInfo(text);
      const message = `${res.statusText} IP: ${ip}, Time: ${time}, Url: ${url}`;
      return createHttpError(res.status, message);
    } else {
      return createHttpError(res.status, res.statusText);
    }
  }
}
*/