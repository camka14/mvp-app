@file:OptIn(ExperimentalTime::class)

package com.razumly.mvp.core.presentation.composables

import com.razumly.mvp.core.data.dataTypes.EventAbs
import com.razumly.mvp.core.data.dataTypes.MVPPlace
import com.razumly.mvp.core.presentation.PaymentResult
import com.razumly.mvp.eventMap.MapComponent
import dev.icerock.moko.geo.LatLng
import kotlin.time.Instant
import platform.UIKit.UIViewController
import kotlin.time.ExperimentalTime

interface NativeViewFactory {
    fun createNativeMapView(
        component: MapComponent,
        onEventSelected: (event: EventAbs) -> Unit,
        onPlaceSelected: (place: MVPPlace) -> Unit,
        canClickPOI: Boolean,
        focusedLocation: LatLng?,
        focusedEvent: EventAbs?,
        revealCenterX: Double,
        revealCenterY: Double
    ): UIViewController

    fun updateNativeMapView(
        viewController: UIViewController,
        component: MapComponent,
        onEventSelected: (event: EventAbs) -> Unit,
        onPlaceSelected: (place: MVPPlace) -> Unit,
        canClickPOI: Boolean,
        focusedLocation: LatLng?,
        focusedEvent: EventAbs?,
        revealCenterX: Double,
        revealCenterY: Double
    )

    fun createNativePlatformDatePicker(
        initialDate: Instant,
        minDate: Instant,
        maxDate: Instant,
        getTime: Boolean,
        onDateSelected: (Instant?) -> Unit,
        onDismissRequest: () -> Unit
    )

    fun presentStripePaymentSheet(
        publishableKey: String,
        customerId: String,
        ephemeralKey: String,
        paymentIntent: String,
        onPaymentResult: (PaymentResult) -> Unit
    )
}