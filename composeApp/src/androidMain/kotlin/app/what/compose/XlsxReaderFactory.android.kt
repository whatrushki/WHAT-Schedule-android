package app.what.compose

import app.what.schedule.rksi.parser.JvmXlsxReader
import app.what.schedule.rksi.parser.XlsxReader

actual fun createXlsxReader(): XlsxReader = JvmXlsxReader()
