package net.matsudamper.device_capture.gstreamer.video

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.freedesktop.gstreamer.Caps
import org.freedesktop.gstreamer.FlowReturn
import org.freedesktop.gstreamer.Sample
import org.freedesktop.gstreamer.elements.AppSink
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import kotlin.coroutines.CoroutineContext

class VideoSink(name: String) : AppSink(name), AutoCloseable, CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = job + Dispatchers.Default

    private val sampleFlow: Channel<Sample?> = Channel(capacity = Channel.RENDEZVOUS)

    private val _imageFlow: MutableStateFlow<BufferedImage> = MutableStateFlow(
        BufferedImage(1920, 1080, BufferedImage.TYPE_BYTE_INDEXED)
    )
    val imageFlow: StateFlow<BufferedImage> = _imageFlow

    private val listener = NEW_SAMPLE { appSink ->
        val sample = appSink.pullSample()
            ?: return@NEW_SAMPLE FlowReturn.OK

        if (sampleFlow.trySend(sample).isFailure) {
            sample.dispose()
        }
        FlowReturn.OK
    }
    private val listener2 = NEW_PREROLL { appSink ->
        val sample = appSink.pullPreroll()
            ?: return@NEW_PREROLL FlowReturn.OK

        if (sampleFlow.trySend(sample).isFailure) {
            sample.dispose()
        }

        FlowReturn.OK
    }

    init {
        caps = Caps.fromString("video/x-raw,format=RGBx,width=1280,height=720")
        set("emit-signals", true)
        connect(listener)
        connect(listener2)
        launch(Dispatchers.IO) {
            sampleFlow.receiveAsFlow().filterNotNull().collect { sample ->
                runCatching {
                    val caps = sample.caps.getStructure(0)
                    val width = caps.getInteger("width")
                    val height = caps.getInteger("height")

                    val buffer = sample.buffer
                    val intbuffer = buffer.map(false).asIntBuffer()

                    val tmp = BufferedImage(width, height, BufferedImage.TYPE_INT_BGR).also {
                        it.accelerationPriority = 0.0f
                    }
                    val pixels = (tmp.raster.dataBuffer as DataBufferInt).data
                    intbuffer.get(pixels, 0, intbuffer.limit())

                    _imageFlow.value = tmp
                    sample.dispose()
                    buffer.unmap()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    override fun close() {
        println("Appshink close")
        job.cancel()
        super.close()
    }
}