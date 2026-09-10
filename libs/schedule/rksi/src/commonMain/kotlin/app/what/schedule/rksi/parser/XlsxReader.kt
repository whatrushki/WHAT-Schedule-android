package app.what.schedule.rksi.parser

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser

interface XlsxReader {
    fun readSheets(bytes: ByteArray): Map<String, List<List<String>>>
}

class NoOpXlsxReader : XlsxReader {
    override fun readSheets(bytes: ByteArray): Map<String, List<List<String>>> = emptyMap()
}

fun parseXlsxXml(entries: Map<String, ByteArray>): Map<String, List<List<String>>> {
    val sharedStrings = mutableListOf<String>()
    val sharedStringsBytes = entries["xl/sharedStrings.xml"]
    if (sharedStringsBytes != null) {
        val xml = sharedStringsBytes.decodeToString()
        val doc = Ksoup.parse(html = xml, parser = Parser.xmlParser())
        doc.select("si").forEach { si ->
            val text = si.select("t").joinToString("") { it.text() }
            sharedStrings.add(text)
        }
    }

    val sheetNameMap = mutableMapOf<String, String>()
    val workbookBytes = entries["xl/workbook.xml"]
    if (workbookBytes != null) {
        val xml = workbookBytes.decodeToString()
        val doc = Ksoup.parse(html = xml, parser = Parser.xmlParser())
        var index = 1
        doc.select("sheet").forEach { sheetElem ->
            val name = sheetElem.attr("name")
            val sheetId = sheetElem.attr("sheetId").ifEmpty { index.toString() }
            sheetNameMap["sheet$sheetId.xml"] = name
            sheetNameMap["sheet$index.xml"] = name
            index++
        }
    }

    val resultSheets = mutableMapOf<String, List<List<String>>>()

    entries.filter { (key, _) -> key.startsWith("xl/worksheets/sheet") && key.endsWith(".xml") }
        .forEach { (key, bytes) ->
            val fileName = key.substringAfterLast("/")
            val sheetName = sheetNameMap[fileName] ?: fileName.removeSuffix(".xml")
            val xml = bytes.decodeToString()
            val doc = Ksoup.parse(html = xml, parser = Parser.xmlParser())
            val rowsList = mutableListOf<List<String>>()

            doc.select("row").forEach { rowElem ->
                val cellsMap = mutableMapOf<Int, String>()
                rowElem.select("c").forEach { cElem ->
                    val r = cElem.attr("r")
                    val colLetters = r.takeWhile { it.isLetter() }.uppercase()
                    val colIndex = if (colLetters.isNotEmpty()) {
                        colLetters.fold(0) { acc, ch -> acc * 26 + (ch - 'A' + 1) } - 1
                    } else -1
                    val type = cElem.attr("t")
                    val rawVal = cElem.selectFirst("v")?.text()?.trim() ?: ""
                    val cellVal = when (type) {
                        "s" -> {
                            val sIdx = rawVal.toIntOrNull()
                            if (sIdx != null && sIdx in sharedStrings.indices) sharedStrings[sIdx] else rawVal
                        }
                        "inlineStr" -> cElem.select("t").joinToString("") { it.text() }
                        else -> rawVal
                    }
                    if (colIndex >= 0) {
                        cellsMap[colIndex] = cellVal
                    }
                }
                val maxCol = cellsMap.keys.maxOrNull() ?: -1
                val rowCells = if (maxCol >= 0) {
                    (0..maxCol).map { cellsMap[it] ?: "" }
                } else emptyList()
                rowsList.add(rowCells)
            }
            resultSheets[sheetName] = rowsList
        }

    return resultSheets
}
