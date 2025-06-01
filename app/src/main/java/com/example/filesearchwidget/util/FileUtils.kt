import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object FileUtils {

    fun saveBitmapToCacheAndGetUri(context: Context, bitmap: Bitmap): Uri {
        val file = File(context.cacheDir, "thumb_${System.currentTimeMillis()}.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
}