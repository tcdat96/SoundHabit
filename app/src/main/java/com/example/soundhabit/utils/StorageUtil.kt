import AudioUtil.SoundMode
import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

object StorageUtil {
    private const val TAG = "StorageUtil"
    private const val INVALID_VOLUME = -1

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
            return if (enabled[index] && volumes[index] != INVALID_VOLUME)
                volumes[index]
            else null
        }
    }

    @UnstableDefault
    fun saveCurrentVolume(packageName: String, soundMode: SoundMode, volume: Int) {
        getSoundProfile(packageName)?.let { profile ->
            val index = getSoundModeIndex(soundMode)
            if (profile.enabled[index]) {
                profile.volumes[index] = volume
                savePackageData(packageName, profile)
            }
        }
    }

    @UnstableDefault
    fun setPackageEnable(packageName: String, soundMode: SoundMode, enabled: Boolean) {
        getSoundProfile(packageName)?.let { profile ->
            val index = getSoundModeIndex(soundMode)
            profile.enabled[index] = enabled
            savePackageData(packageName, profile)
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
        val volumes: ArrayList<Int> = arrayListOf(-1, -1, -1),
        val enabled: ArrayList<Boolean> = arrayListOf(true, true, true))
}