import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build


object AudioUtil {
    enum class SoundMode {SPEAKER, WIRED, BLUETOOTH}

    fun getCurrentVolume(context: Context): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun setCurrentVolume(context: Context, volume: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI)
    }

    fun getSoundMode(context: Context): SoundMode {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        return audioManager?.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (deviceInfo in audioDevices) {
                    when (deviceInfo.type) {
                        AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET -> return SoundMode.WIRED
                        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> return SoundMode.BLUETOOTH
                    }
                }
            } else {
                if (audioManager.isWiredHeadsetOn) {
                    return SoundMode.WIRED
                } else if (audioManager.isBluetoothA2dpOn) {
                    return SoundMode.BLUETOOTH
                }
            }
            SoundMode.SPEAKER
        } ?: SoundMode.SPEAKER
    }
}