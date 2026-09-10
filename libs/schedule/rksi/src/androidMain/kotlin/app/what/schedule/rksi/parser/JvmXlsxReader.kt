package app.what.schedule.rksi.parser

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

class JvmXlsxReader : XlsxReader {
    override fun readSheets(bytes: ByteArray): Map<String, List<List<String>>> {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    entries[entry.name] = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return parseXlsxXml(entries)
    }
}
