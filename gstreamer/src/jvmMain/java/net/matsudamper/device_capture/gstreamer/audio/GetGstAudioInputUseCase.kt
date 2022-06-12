package net.matsudamper.device_capture.gstreamer.audio

import org.freedesktop.gstreamer.Caps
import org.freedesktop.gstreamer.Structure
import org.freedesktop.gstreamer.lowlevel.GstDeviceMonitorAPI

class GetGstAudioInputUseCase {

    fun getWasApi2SourceList(): List<AudioInputSource> {
        val monitor = GstDeviceMonitorAPI.GSTDEVICEMONITOR_API.gst_device_monitor_new()
        GstDeviceMonitorAPI.GSTDEVICEMONITOR_API.gst_device_monitor_add_filter(
            monitor,
            "Audio/Source",
            Caps("audio/x-raw")
        )
        GstDeviceMonitorAPI.GSTDEVICEMONITOR_API.gst_device_monitor_start(monitor)

        return try {
            monitor.devices.mapNotNull { device ->
                val properties: Structure = device.properties ?: return@mapNotNull null
                if (properties.getString("device.api") != "wasapi2") return@mapNotNull null

                return@mapNotNull AudioInputSource(
                    name = device.name,
                    displayName = device.displayName,
                    deviceId = device.getAsString("device"),
                )
            }
        } finally {
            monitor.dispose()
        }
    }

    data class AudioInputSource(
        val name: String,
        val displayName: String,
        val deviceId: String,
    )
}