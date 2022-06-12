package net.matsudamper.device_capture.gstreamer.video

import kotlinx.coroutines.flow.StateFlow
import org.freedesktop.gstreamer.*
import java.awt.image.BufferedImage

class GstVideoDeviceStreamingManager(private val devicePath: String) : AutoCloseable {
    private val pipeline: Pipeline = Pipeline()

    private val source = createSource(devicePath)
    private val scale = ElementFactory.make("videoscale", "videoscale")
    private val convert = ElementFactory.make("videoconvert", "videoconvert")
    private val sink = VideoSink("sink")

    private val elements = listOf(source, convert, scale, sink)

    init {
        println("DevicePath: $devicePath")

        runCatching {
            pipeline.also {
                it.addMany(*elements.toTypedArray())
                Element.linkMany(*elements.toTypedArray())
            }
            pipeline.play()
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun getImageFlow(): StateFlow<BufferedImage> {
        return sink.imageFlow
    }

    override fun close() {
        elements.forEach { it.close() }
        pipeline.stop()
        pipeline.close()
        pipeline.dispose()
    }

    private fun createSource(devicePath: String): Bin {
        return Gst.parseBinFromDescription(
            """mfvideosrc device-path=${devicePath}""",
            true
        )
    }
}