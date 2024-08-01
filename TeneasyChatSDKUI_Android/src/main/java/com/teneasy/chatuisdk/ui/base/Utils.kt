package com.teneasy.chatuisdk.ui.base

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.protobuf.Timestamp
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit


class Utils {

        companion object{
            var localDateFormat = "yyyy-MM-dd HH:mm:ss"
        }
     fun copyText(text: String, context: Context){
        // Get the system clipboard service
        val clipboardManager =  context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Create a new ClipData object with the text
        val clipData = ClipData.newPlainText("Copied Text", text)

        // Set the primary clip on the clipboard
        clipboardManager.setPrimaryClip(clipData)

        //Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show()
    }

    fun readConfig() {
        Constants.xToken = UserPreferences().getString(PARAM_XTOKEN, Constants.xToken)
        Constants.domain = UserPreferences().getString(PARAM_DOMAIN, Constants.domain)
        Constants.userId = UserPreferences().getInt(PARAM_USER_ID, Constants.userId)
        Constants.merchantId = UserPreferences().getInt(PARAM_MERCHANT_ID, Constants.merchantId)
        Constants.lines = UserPreferences().getString(PARAM_LINES, Constants.lines)
        Constants.cert = UserPreferences().getString(PARAM_CERT, Constants.cert)
        Constants.baseUrlImage = UserPreferences().getString(PARAM_IMAGEBASEURL, Constants.baseUrlImage)
    }

         fun closeSoftKeyboard(view: View?) {
            if (view == null || view.windowToken == null) {
                return
            }
            val imm: InputMethodManager =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

    fun convertStrToDate(datetimeString: String): Date {
        //yyyy-MM-dd'T'HH:mm:sss'Z'
        var date = Date()
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'", Locale.getDefault())
            //dateFormat.timeZone = TimeZone.getDefault()
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
             date = dateFormat.parse(datetimeString)
        }catch (ex:Exception){

        }
        return date
    }

    fun differenceInMinutes(date1: Date, date2: Date): Long {
        val diffInMillis = date2.time - date1.time
        return TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    }


    fun isMessageTimeDifferenceValid(lastMsgTime: Date?, sendingMsgTime: Date?, minutesDifference: Int): Boolean {
        if (lastMsgTime == null || sendingMsgTime == null) {
            return false
        }

        // Calculate the time 5 minutes before lastMsgTime
        val fiveMinutesInMillis = minutesDifference * 60 * 1000
        val lastMsgTimeMinusFiveMinutes = Date(lastMsgTime.time - fiveMinutesInMillis)

        // Compare the adjusted lastMsgTime with sendingMsgTime
        return lastMsgTimeMinusFiveMinutes > sendingMsgTime
    }

    fun sessionTimeout(lastMsgTime: Date?, sendingMsgTime: Date?, secondsDifference: Int): Boolean {
        if (lastMsgTime == null || sendingMsgTime == null) {
            return false
        }

        // Calculate the time 5 minutes before lastMsgTime
        val fiveMinutesInMillis = secondsDifference * 1000
        val lastMsgTimeMinusFiveMinutes = Date(lastMsgTime.time - fiveMinutesInMillis)

        // Compare the adjusted lastMsgTime with sendingMsgTime
        return lastMsgTimeMinusFiveMinutes > sendingMsgTime
    }


    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getScreenWidth(): Int {
        return Resources.getSystem().getDisplayMetrics().widthPixels
    }

    fun getScreenHeight(): Int {
        return Resources.getSystem().getDisplayMetrics().heightPixels
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun timestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'")
        val date = Date(timestamp)
        return sdf.format(date)
    }

    fun getBitmapFromFile(imageFile: File): Bitmap {
        return BitmapFactory.decodeFile(imageFile.absolutePath)
    }

    fun saveImageToGallery(context: Context, f: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_STARTED)
        //val f = File(photoPath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.setData(contentUri)
        context.sendBroadcast(mediaScanIntent)
    }

    fun compressBitmap(bitmap: Bitmap, quality: Int = 50): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    fun saveCompressedBitmapToFile(compressedData: ByteArray, outputFile: File) {
        try {
            val fos = FileOutputStream(outputFile)
            fos.write(compressedData)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("chatLibUtils", "Failed to save compressed bitmap to file")
        }
    }

    fun encodeFilePath(filePath: String): String {
        return URLEncoder.encode(filePath, StandardCharsets.UTF_8.toString())
    }

    fun timestampToString(timestamp: Timestamp): String {
        // Convert the protobuf Timestamp to milliseconds
        val millis = timestamp.seconds * 1000 + timestamp.nanos / 1_000_000

        // Create a Date object from milliseconds
        val date = Date(millis)

        // Create a SimpleDateFormat object for the desired format
        val sdf = SimpleDateFormat(localDateFormat, Locale.getDefault())

        // Set the timezone to UTC to avoid any timezone offset issues
        sdf.timeZone = TimeZone.getDefault()

        // Format the date to the desired string format
        return sdf.format(date)
    }

    /**
     * Creates a bitmap thumbnail from video uri of the scheme type content://
     */
    fun getVideoThumb(context: Context, uri: Uri): Bitmap? {
        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, uri)
            return mediaMetadataRetriever.frameAtTime
        } catch (ex: Exception) {
            Toast
                .makeText(context, "从图像获取缩略图失败", Toast.LENGTH_SHORT)
                .show()
        }
        return null

    }

    /**
     * Value of dp to value of px.
     *
     * @param dpValue The value of dp.
     * @return value of px
     */
    fun dp2px(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * Value of px to value of dp.
     *
     * @param pxValue The value of px.
     * @return value of dp
     */
    fun px2dp(pxValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        var bitmap: Bitmap? = null

        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }

        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }

        val canvas: Canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }

    fun updateLayoutParams(relativeLayout: RelativeLayout, newWidth: Int, newHeight: Int) {
        val layoutParams = relativeLayout.layoutParams
        layoutParams.width = newWidth
        layoutParams.height = newHeight
        relativeLayout.layoutParams = layoutParams
    }

    fun resizePhoto(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val aspRat = w / h
        val W = 400
        val H = W * aspRat
        val b = Bitmap.createScaledBitmap(bitmap, W, H, false)
        return b
    }
}