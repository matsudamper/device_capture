package net.matsudamper.device_capture.gstreamer

import org.freedesktop.gstreamer.Gst
import org.freedesktop.gstreamer.Version

object GStreamerNativeLibrary {
    fun init() {
        Gst.init(Version(1, 0))
        Runtime.getRuntime().addShutdownHook(
            object : Thread() {
                override fun run() {
                    Gst.deinit()
                }
            }
        )
    }
}