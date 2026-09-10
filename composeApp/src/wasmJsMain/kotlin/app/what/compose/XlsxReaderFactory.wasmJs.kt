package app.what.compose

import app.what.schedule.rksi.parser.NoOpXlsxReader
import app.what.schedule.rksi.parser.XlsxReader

actual fun createXlsxReader(): XlsxReader = NoOpXlsxReader()
