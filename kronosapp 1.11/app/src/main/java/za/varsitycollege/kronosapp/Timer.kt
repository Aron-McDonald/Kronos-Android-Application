package za.varsitycollege.kronosapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.ByteArrayOutputStream

@IgnoreExtraProperties
data class Timer(
    val project: String = "",
    val client: String = "",
    val category: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val description: String = "",
    val hours: Float = 0f,
    @get:Exclude var image: Bitmap? = null,
    var imageBase64: String? = null
) {
    constructor() : this("", "", "", "", "", "", "", 0f, null, null)
}

fun Bitmap.toBase64(): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun String.base64ToBitmap(): Bitmap? {
    val decodedString = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
}

fun Timer.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "project" to project,
        "client" to client,
        "category" to category,
        "date" to date,
        "startTime" to startTime,
        "endTime" to endTime,
        "description" to description,
        "hours" to hours,
        "imageBase64" to image?.toBase64()
    )
}