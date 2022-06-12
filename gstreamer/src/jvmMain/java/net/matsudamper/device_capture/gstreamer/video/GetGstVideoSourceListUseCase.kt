package net.matsudamper.device_capture.gstreamer.video

import org.freedesktop.gstreamer.Caps
import org.freedesktop.gstreamer.Structure
import org.freedesktop.gstreamer.lowlevel.GstDeviceMonitorAPI

class GetGstVideoSourceListUseCase {
    fun getMfVideoSource(): List<VideoSource> {
        val monitor = GstDeviceMonitorAPI.GSTDEVICEMONITOR_API.gst_device_monitor_new()
        GstDeviceMonitorAPI.GSTDEVICEMONITOR_API.gst_device_monitor_add_filter(
            monitor,
            "Source/Video",
            Caps("video/x-raw")
        )
        GstDeviceMonitorAPI.GSTDEVICEMONITOR_API.gst_device_monitor_start(monitor)

        return try {
            monitor.devices.mapNotNull { device ->
                val properties: Structure? = device.properties
                if (properties != null && properties.hasField("device.api")) {
                    if (properties.getString("device.api") == "mediafoundation") {
                        return@mapNotNull VideoSource(
                            name = device.name,
                            displayName = device.displayName,
                            devicePath = device.getAsString("device-path"),
                        )
                    }
                }
                null
            }
        } finally {
            monitor.dispose()
        }
    }

    data class VideoSource(
        val name: String,
        val displayName: String,
        val devicePath: String,
    )
}
