package com.razumly.mvp.core.util

import com.razumly.mvp.core.data.dataTypes.Bounds
import dev.icerock.moko.geo.LatLng
import dev.icerock.moko.geo.LocationTracker
import io.appwrite.models.Document
import io.appwrite.serializers.DynamicLookupSerializer
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

fun Int.ceilDiv(other: Int): Int {
    return this.floorDiv(other) + this.rem(other).sign.absoluteValue
}

fun <T, R> Document<T>.convert(converter: (T) -> R): Document<R> {
    return Document(id, collectionId, databaseId, createdAt, updatedAt, permissions, converter(data))
}

fun getBounds(radius: Double, latitude: Double, longitude: Double): Bounds {
    val earthCircumference = 24902.0
    val deltaLatitude = 360.0 * radius / earthCircumference
    val deltaLongitude =
        deltaLatitude / cos((latitude.times(PI)) / 180.0)

    return Bounds(
        north = latitude + deltaLatitude,
        south = latitude - deltaLatitude,
        west = longitude - deltaLongitude,
        east = longitude + deltaLongitude,
        center = LatLng(latitude, longitude)
    )
}


fun calcDistance(start: LatLng, end: LatLng): Double {
    return try {
        acos(
            sin(start.latitude) * sin(end.latitude) + cos(start.latitude) * cos(end.latitude) * cos(
                end.longitude - start.longitude
            )
        ) * 3959
    } catch (e: Exception) {
        0.0
    }
}

suspend fun LocationTracker.getCurrentLocation(): LatLng {
    try {
        val location = getLocationsFlow().first()
        return location
    } catch (e: Exception) {
        Napier.e("Failed to get current location: ${e.message}")
        return LatLng(0.0, 0.0)
    }
}

val jsonMVP =  Json {
    encodeDefaults = true
    isLenient = true
    allowSpecialFloatingPointValues = true
    allowStructuredMapKeys = true
    prettyPrint = false
    useArrayPolymorphism = false
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

expect object Platform {
    val name: String
    val isIOS: Boolean
}