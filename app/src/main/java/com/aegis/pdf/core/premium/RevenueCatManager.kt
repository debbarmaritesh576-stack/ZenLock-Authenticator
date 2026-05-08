package com.aegis.pdf.core.premium

import android.app.Activity
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.models.StoreTransaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevenueCatManager @Inject constructor() {

    private val apiKey = "YOUR_REVENUECAT_API_KEY"

    fun initialize(context: android.content.Context) {
        Purchases.configure(
            PurchasesConfiguration.Builder(context, apiKey).build()
        )
    }

    fun getOfferings(
        onSuccess: (List<StoreProduct>) -> Unit,
        onError: (String) -> Unit
    ) {
        Purchases.sharedInstance.getOfferingsWith({ error ->
            onError(error.message)
        }) { offerings ->
            val products = offerings.current?.availablePackages?.map { it.product } ?: emptyList()
            onSuccess(products)
        }
    }

    fun purchase(
        activity: Activity,
        product: StoreProduct,
        onSuccess: (StoreTransaction) -> Unit,
        onError: (String) -> Unit
    ) {
        Purchases.sharedInstance.purchaseWith(
            PurchaseParams.Builder(activity, product).build(),
            onError = { error -> onError(error.message) }
        ) { transaction, customerInfo ->
            if (customerInfo.entitlements.active.isNotEmpty()) {
                onSuccess(transaction)
            }
        }
    }

    fun checkPremiumStatus(onResult: (Boolean) -> Unit) {
        Purchases.sharedInstance.getCustomerInfoWith({ error ->
            onResult(false)
        }) { customerInfo ->
            onResult(customerInfo.entitlements.active.isNotEmpty())
        }
    }

    fun restorePurchases(onComplete: (Boolean) -> Unit) {
        Purchases.sharedInstance.restorePurchasesWith({ error ->
            onComplete(false)
        }) { customerInfo ->
            onComplete(customerInfo.entitlements.active.isNotEmpty())
        }
    }
}