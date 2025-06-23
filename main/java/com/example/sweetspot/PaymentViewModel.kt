package com.example.sweetspot

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.androidbrowserhelper.playbilling.provider.PaymentResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

class PaymentViewModel : ViewModel() {

    private val paymentDataRequestJson: JSONObject = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
        put("merchantInfo", JSONObject().apply {
            put("merchantId", "your-merchant-id") // Replace with your merchant ID
            put("merchantName", "Your Merchant Name") // Replace with your merchant name
        })
        put("transactionInfo", JSONObject().apply {
            put("totalPriceStatus", "FINAL")
            put("totalPrice", "0.00") // Placeholder; will be updated dynamically
            put("currencyCode", "USD")
            put("countryCode", "US")
        })
        put("cardRequirements", JSONObject().apply {
            put("allowedCardNetworks", JSONArray().apply {
                put("VISA")
                put("MASTERCARD")
            })
            put("billingAddressRequired", true)
        })
    }

    fun getPaymentDataRequest(totalPrice: String): PaymentDataRequest {
        paymentDataRequestJson.getJSONObject("transactionInfo").put("totalPrice", totalPrice)
        return PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
    }

    private val _paymentResult = MutableStateFlow<PaymentResult>(PaymentResult.Idle)
    val paymentResult: MutableState<PaymentResult?> = mutableStateOf(null)

    fun setPaymentResult(result: PaymentResult) {
        _paymentResult.value = result
    }

    sealed class PaymentResult {
        data class Success(val method: String) : PaymentResult()
        data class Error(val message: String) : PaymentResult()
        object Idle : PaymentResult()
    }
}
