package com.razumly.mvp.core.presentation.composables

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.razumly.mvp.LocalNativeViewFactory
import com.razumly.mvp.core.presentation.IPaymentProcessor
import com.razumly.mvp.core.presentation.PaymentProcessor

@Composable
actual fun PaymentProcessorButton(
    onClick: () -> Unit,
    paymentProcessor: IPaymentProcessor,
    text: String
) {
    val factory = LocalNativeViewFactory.current
    paymentProcessor as PaymentProcessor
    paymentProcessor.setNativeViewFactory(factory)

    Button(
        onClick = onClick,
    ) {
        Text(text)
    }
}