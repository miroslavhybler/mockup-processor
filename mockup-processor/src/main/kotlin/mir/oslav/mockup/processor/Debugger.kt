@file:Suppress("ConstPropertyName")

package mir.oslav.mockup.processor

import mir.oslav.mockup.processor.Debugger.outputStream
import java.io.OutputStream


/**
 * Debugger for creating longs into generated code. Should be always dissabled for release.
 * @since 1.1.3
 * @author Miroslav Hýbler <br>
 * created on 02.01.2024
 */
object Debugger {


    /**
     * OutputStream used to write logs
     * @since 1.1.3
     */
    private var outputStream: OutputStream? = null


    /**
     * True if writing logs into generated file through [outputStream] is enabled, false otherwise. This
     * property is never changing in code, it must be set constantly.
     * @since 1.1.3
     */
    const val isDebugEnabled: Boolean = false


    /**
     * Stores [outputStream] for later debug writes when [isDebugEnabled] is true.
     * @param outputStream Stream created by KSP for debug output.
     * @since 1.1.3
     */
    fun setOutputStream(outputStream: OutputStream) {
        if (isDebugEnabled) {
            this.outputStream = outputStream
        }
    }


    /**
     * Writes [text] as a generated-file comment when debugging is enabled.
     * @param text Message to append to the debug output.
     * @since 1.1.3
     */
    fun write(text: String) {
        if (isDebugEnabled) {
            outputStream?.write("//     $text\n".toByteArray())
        }
    }


    /**
     * Closes the debug output stream when one was opened.
     * @since 1.1.3
     */
    fun close() {
        outputStream?.close()
    }
}
