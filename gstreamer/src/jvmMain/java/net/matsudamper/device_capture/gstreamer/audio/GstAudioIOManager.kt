package net.matsudamper.device_capture.gstreamer.audio

import org.freedesktop.gstreamer.Element
import org.freedesktop.gstreamer.ElementFactory
import org.freedesktop.gstreamer.Gst
import org.freedesktop.gstreamer.Pipeline

class GstAudioIOManager(
    private val wasapi2InputId: String,
    private val wasapi2OutputId: String?,
) : AutoCloseable {
    private var elements: MutableList<Element> = mutableListOf()
    private var pipeline: Pipeline? = null

    init {
        println("open Audio: input=$wasapi2InputId, output,$wasapi2OutputId")
        val wasapi2src = """wasapi2src device=$wasapi2InputId"""

        val source = Gst.parseBinFromDescription(
            wasapi2src,
            true
        )

        val audioConvert = ElementFactory.make("audioconvert", "audioconvert")
        val audioResample = ElementFactory.make("audioresample", "audioresample")

        val audioSink = if (wasapi2OutputId == null) {
            Gst.parseBinFromDescription(
                """autoaudiosink""",
                true
            )
        } else {
            Gst.parseBinFromDescription(
                """wasapi2sink device=$wasapi2OutputId""",
                true
            )
        }

        runCatching {
            elements.clear()
            elements.addAll(
                listOf(source, audioConvert, audioResample, audioSink)
            )
        }

        val pipeline = Pipeline("pipeline").also {
            it.addMany(*elements.toTypedArray())
            Element.linkMany(*elements.toTypedArray())
        }

        this.pipeline = pipeline
        pipeline.stop()
        pipeline.play()
    }

    override fun close() {
        println("close input:$wasapi2InputId output:$wasapi2OutputId")
        pipeline?.stop()
        elements.map {
            it.stop()
        }

        pipeline?.close()
        elements.map {
            it.close()
        }
    }
}
