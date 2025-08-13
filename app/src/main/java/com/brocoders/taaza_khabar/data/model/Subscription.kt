package com.brocoders.taaza_khabar.data.model

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String = "INR",
    val durationDays: Int,
    val features: List<String>
)

data class PaymentData(
    val orderId: String,
    val amount: Double,
    val currency: String,
    val planId: String,
    val customerEmail: String?,
    val customerPhone: String?
)

object SubscriptionPlans {
    val availablePlans = listOf(
        SubscriptionPlan(
            id = "weekly",
            name = "Weekly Premium",
            description = "Premium access for 7 days",
            price = 99.0,
            durationDays = 7,
            features = listOf(
                "Full article content in WebView",
                "No advertisements",
                "Offline reading",
                "Priority news updates"
            )
        ),
        SubscriptionPlan(
            id = "monthly",
            name = "Monthly Premium",
            description = "Premium access for 30 days",
            price = 299.0,
            durationDays = 30,
            features = listOf(
                "Full article content in WebView",
                "No advertisements", 
                "Offline reading",
                "Priority news updates",
                "Exclusive premium content"
            )
        ),
        SubscriptionPlan(
            id = "yearly",
            name = "Yearly Premium",
            description = "Premium access for 365 days",
            price = 2999.0,
            durationDays = 365,
            features = listOf(
                "Full article content in WebView",
                "No advertisements",
                "Offline reading", 
                "Priority news updates",
                "Exclusive premium content",
                "Personal news curator",
                "Advanced search filters"
            )
        )
    )
    
    fun getPlanById(id: String): SubscriptionPlan? = 
        availablePlans.find { it.id == id }
} 