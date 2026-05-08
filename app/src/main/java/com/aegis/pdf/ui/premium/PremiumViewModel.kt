package com.aegis.pdf.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.premium.PremiumManager
import com.aegis.pdf.core.premium.SubscriptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumManager: PremiumManager,
    private val subscriptionManager: SubscriptionManager
) : ViewModel() {

    private val _isPremium = MutableStateFlow(premiumManager.isPremium)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _products = MutableStateFlow<List<String>>(listOf("monthly", "yearly", "lifetime"))
    val products: StateFlow<List<String>> = _products.asStateFlow()

    fun subscribe(plan: String) {
        viewModelScope.launch {
            val premiumPlan = when (plan) {
                "monthly" -> PremiumManager.Plan.MONTHLY
                "yearly" -> PremiumManager.Plan.YEARLY
                "lifetime" -> PremiumManager.Plan.LIFETIME
                else -> return@launch
            }
            premiumManager.upgradeToPremium(premiumPlan, "test_token_$plan")
            _isPremium.value = true
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _isPremium.value = premiumManager.isPremium
        }
    }
}