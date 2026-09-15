package com.example.billing

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.android.vending.billing.IInAppBillingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MyketBillingManager(private val context: Context) {

    companion object {
        private const val TAG = "MyketBilling"

        const val SKU_LIFETIME = "fullyversion"
        const val MYKET_PACKAGE = "ir.mservices.market"
        const val MYKET_BIND_ACTION = "ir.mservices.market.InAppBillingService.BIND"
        const val PLAY_BIND_ACTION = "com.android.vending.billing.InAppBillingService.BIND"

        const val PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDdtxy0r0wFEfNDFWzkundpWDjJIH1mEGL7w3MY6J1+de2TwlDg9Xb2JS6ZnlM0Nefmh/6jTDimXvhTNEXcvb15Sf78B3gWBJWofgFrZpeEGCWBkuS1Bol9dMV4Vhv2H4xheC7Vl2KvkSYnQ6mIAddPTlRY0mk53jMRwrkryzS4OwIDAQAB"

        const val BILLING_RESPONSE_RESULT_OK = 0
        const val BILLING_RESPONSE_RESULT_USER_CANCELED = 1
        const val BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2
        const val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3
        const val BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4
        const val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5
        const val BILLING_RESPONSE_RESULT_ERROR = 6
        const val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7
        const val BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8

        const val RESPONSE_CODE = "RESPONSE_CODE"
        const val RESPONSE_BUY_INTENT = "BUY_INTENT"
        const val RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"
        const val RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE"
        const val RESPONSE_INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST"
        const val RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
        const val RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST"

        const val RC_MYKET_PURCHASE = 10099
    }

    private var billingService: IInAppBillingService? = null
    private var isBound = false
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private var onConnectedCallback: (() -> Unit)? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Myket Billing Service Connected")
            billingService = IInAppBillingService.Stub.asInterface(service)
            _isServiceConnected.value = true
            isBound = true
            // Automatically check purchases on connect
            queryPurchasesAsync(null)
            onConnectedCallback?.invoke()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Myket Billing Service Disconnected")
            billingService = null
            _isServiceConnected.value = false
            isBound = false
        }
    }

    fun startConnection(onConnected: (() -> Unit)? = null) {
        onConnectedCallback = onConnected
        if (isBound && billingService != null) {
            onConnected?.invoke()
            return
        }

        try {
            val intent = Intent(MYKET_BIND_ACTION).apply {
                setPackage(MYKET_PACKAGE)
            }
            val bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (!bound) {
                // Fallback attempt with play bind action targeted to Myket
                val fallbackIntent = Intent(PLAY_BIND_ACTION).apply {
                    setPackage(MYKET_PACKAGE)
                }
                context.bindService(fallbackIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding to Myket service", e)
        }
    }

    fun endConnection() {
        if (isBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding Myket service", e)
            }
            isBound = false
            billingService = null
            _isServiceConnected.value = false
        }
    }

    fun isMyketInstalled(): Boolean {
        return try {
            val pm = context.packageManager
            pm.getPackageInfo(MYKET_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openMyketInstallPage(activity: Activity) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myket.ir/app/$MYKET_PACKAGE"))
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not open Myket page", e)
        }
    }

    /**
     * Queries previously owned items from Myket.
     */
    fun queryPurchasesAsync(onResult: ((Boolean) -> Unit)?) {
        scope.launch {
            val service = billingService
            if (service == null) {
                withContext(Dispatchers.Main) { onResult?.invoke(false) }
                return@launch
            }

            try {
                val ownedBundle: Bundle = service.getPurchases(3, context.packageName, "inapp", null)
                val responseCode = ownedBundle.getInt(RESPONSE_CODE)
                if (responseCode == BILLING_RESPONSE_RESULT_OK) {
                    val ownedSkus = ownedBundle.getStringArrayList(RESPONSE_INAPP_ITEM_LIST) ?: arrayListOf()
                    val purchaseDataList = ownedBundle.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST) ?: arrayListOf()
                    val signatureList = ownedBundle.getStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST) ?: arrayListOf()

                    var isLifetimeOwned = false
                    for (i in 0 until ownedSkus.size) {
                        val sku = ownedSkus[i]
                        if (sku == SKU_LIFETIME) {
                            val purchaseData = purchaseDataList.getOrNull(i)
                            val signature = signatureList.getOrNull(i)
                            if (purchaseData != null && signature != null) {
                                val verified = Security.verifyPurchase(PUBLIC_KEY, purchaseData, signature)
                                if (verified) {
                                    isLifetimeOwned = true
                                    break
                                }
                            } else {
                                isLifetimeOwned = true
                                break
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        onResult?.invoke(isLifetimeOwned)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult?.invoke(false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception querying purchases", e)
                withContext(Dispatchers.Main) {
                    onResult?.invoke(false)
                }
            }
        }
    }

    /**
     * Initiates the purchase flow for [SKU_LIFETIME].
     * Returns a [PendingIntent] to be launched by the calling Activity.
     */
    fun launchPurchaseFlow(
        activity: Activity,
        onPendingIntentReady: (PendingIntent) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isMyketInstalled()) {
            onError("برنامه مایکت روی دستگاه شما نصب نیست. لطفاً ابتدا مایکت را نصب کنید.")
            return
        }

        val service = billingService
        if (service == null) {
            startConnection()
            onError("در حال برقراری اتصال به مایکت... لطفاً چند لحظه بعد مجدداً تلاش کنید.")
            return
        }

        scope.launch {
            try {
                val payload = "payload_${System.currentTimeMillis()}_${context.packageName}"
                val buyIntentBundle = service.getBuyIntent(
                    3,
                    context.packageName,
                    SKU_LIFETIME,
                    "inapp",
                    payload
                )

                val responseCode = buyIntentBundle.getInt(RESPONSE_CODE)
                if (responseCode == BILLING_RESPONSE_RESULT_OK) {
                    val pendingIntent = buyIntentBundle.getParcelable<PendingIntent>(RESPONSE_BUY_INTENT)
                    if (pendingIntent != null) {
                        withContext(Dispatchers.Main) {
                            onPendingIntentReady(pendingIntent)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onError("خطا در دریافت اطلاعات پرداخت از مایکت.")
                        }
                    }
                } else if (responseCode == BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
                    withContext(Dispatchers.Main) {
                        onError("شما قبلاً این اشتراک دائمی را خریداری کرده‌اید.")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError("خطا در درگاه مایکت (کد: $responseCode)")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting buy intent", e)
                withContext(Dispatchers.Main) {
                    onError("خطای سیستمی در آغاز پرداخت: ${e.localizedMessage ?: "نامشخص"}")
                }
            }
        }
    }

    /**
     * Handles the onActivityResult response from Myket payment.
     */
    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        if (requestCode != RC_MYKET_PURCHASE) return false

        if (data == null) {
            onError("اطلاعات پرداخت دریافت نشد.")
            return true
        }

        val responseCode = data.getIntExtra(RESPONSE_CODE, 0)
        if (resultCode == Activity.RESULT_OK && responseCode == BILLING_RESPONSE_RESULT_OK) {
            val purchaseData = data.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA)
            val dataSignature = data.getStringExtra(RESPONSE_INAPP_SIGNATURE)

            if (purchaseData.isNullOrBlank() || dataSignature.isNullOrBlank()) {
                onError("اطلاعات یا امضای خرید نامعتبر است.")
                return true
            }

            val verified = Security.verifyPurchase(PUBLIC_KEY, purchaseData, dataSignature)
            if (verified) {
                try {
                    val json = JSONObject(purchaseData)
                    val productId = json.optString("productId")
                    if (productId == SKU_LIFETIME) {
                        onSuccess()
                    } else {
                        onError("شناسه محصول تطابق ندارد.")
                    }
                } catch (e: Exception) {
                    onSuccess()
                }
            } else {
                onError("تأیید امنیتی کلید مایکت ناموفق بود.")
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            onError("عملیات پرداخت توسط کاربر لغو شد.")
        } else if (responseCode == BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
            onSuccess() // Already owned
        } else {
            onError("پرداخت انجام نشد (کد: $responseCode)")
        }

        return true
    }
}
