import android.content.Context
import android.content.SharedPreferences

object StorageUtil {
    private const val INVALID_VOLUME = -1

    private var sharedPref : SharedPreferences? = null

    fun init(context: Context) {
        sharedPref = context.getSharedPreferences(
            context.applicationContext.packageName, Context.MODE_PRIVATE)
    }

    fun getPreviousVolume(packageName: String): Int? {
        return sharedPref?.getInt(packageName, INVALID_VOLUME)?.let {
            if (it != INVALID_VOLUME) it else null
        }
    }

    fun saveCurrentVolume(packageName: String, volume: Int) {
        sharedPref?.edit()?.run {
            putInt(packageName, volume)
            apply()
        }
    }
}