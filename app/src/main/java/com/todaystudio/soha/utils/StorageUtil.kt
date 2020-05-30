import AudioUtil.SoundMode
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

object StorageUtil {
    private const val TAG = "StorageUtil"

    private const val VOLUME_NOT_SET = -1

    private var sharedPref: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPref == null) {
            sharedPref = context.getSharedPreferences(
                context.applicationContext.packageName, Context.MODE_PRIVATE
            )
        }
    }

    private fun getSoundModeIndex(soundMode: SoundMode): Int {
        return when (soundMode) {
            SoundMode.SPEAKER -> 0
            SoundMode.WIRED -> 1
            SoundMode.BLUETOOTH -> 2
        }
    }

    @UnstableDefault
    fun getPreviousVolume(packageName: String, soundMode: SoundMode): Int? {
        return getSoundProfile(packageName)?.run {
            val index = getSoundModeIndex(soundMode)
            return if (enabled && volumes[index] >= 0) volumes[index] else null
        }
    }

    @UnstableDefault
    fun saveCurrentVolume(packageName: String, soundMode: SoundMode, volume: Int) {
        getSoundProfile(packageName)?.let { profile ->
            if (!profile.enabled) return
            val index = getSoundModeIndex(soundMode)
            profile.volumes[index] = volume
            savePackageData(packageName, profile)
            Log.d(TAG, "$packageName: $soundMode at $volume")
        }
    }

    @UnstableDefault
    fun setPackageEnable(packageName: String, enabled: Boolean) {
        getSoundProfile(packageName)?.let { profile ->
            profile.enabled = enabled
            savePackageData(packageName, profile)
            Log.d(TAG, "$packageName is ${if (enabled) "enabled" else "disabled"}")
        }
    }

    @UnstableDefault
    private fun savePackageData(packageName: String, profile: SoundProfile) {
        sharedPref?.edit()?.run {
            val json = Json.stringify(SoundProfile.serializer(), profile)
            putString(packageName, json)
            apply()
        }
    }

    @UnstableDefault
    private fun getSoundProfile(packageName: String): SoundProfile? {
        return sharedPref?.getString(packageName, "{}")?.run {
            Json.parse(SoundProfile.serializer(), this)
        }
    }

    fun hasPackage(packageName: String): Boolean {
        return sharedPref?.contains(packageName) == true
    }

    @UnstableDefault
    fun savePackage(packageName: String): SoundProfile? {
        return if (!hasPackage(packageName)) {
            val profile = SoundProfile()
            savePackageData(packageName, profile)
            profile
        } else {
            getSoundProfile(packageName)
        }
    }

    @Serializable
    data class SoundProfile(
        var enabled: Boolean = false,
        val volumes: ArrayList<Int> = arrayListOf(VOLUME_NOT_SET, VOLUME_NOT_SET, VOLUME_NOT_SET)
    )
}