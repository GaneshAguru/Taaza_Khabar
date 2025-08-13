package com.brocoders.taaza_khabar.data.service

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.brocoders.taaza_khabar.data.model.PaymentData
import com.brocoders.taaza_khabar.data.model.SubscriptionPlan
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentService @Inject constructor(
    private val context: Context
) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("subscription_prefs", Context.MODE_PRIVATE)
    
    // Store callbacks for payment result handling
    private var currentPlan: SubscriptionPlan? = null
    private var currentSuccessCallback: ((String) -> Unit)? = null
    private var currentFailureCallback: ((String) -> Unit)? = null
    
    companion object {
        private const val RAZORPAY_KEY = "rzp_test_1DP5mmOlF5G5ag" // Replace with your actual key
        private const val PREF_SUBSCRIPTION_ACTIVE = "subscription_active"
        private const val PREF_SUBSCRIPTION_END_DATE = "subscription_end_date"
        private const val PREF_SUBSCRIPTION_PLAN = "subscription_plan"
    }
    
    // Methods to handle payment results (call these from MainActivity)
    fun handlePaymentSuccess(paymentId: String) {
        currentPlan?.let { plan ->
            try {
                val endDate = System.currentTimeMillis() + (plan.durationDays * 24 * 60 * 60 * 1000L)
                
                with(sharedPreferences.edit()) {
                    putBoolean(PREF_SUBSCRIPTION_ACTIVE, true)
                    putLong(PREF_SUBSCRIPTION_END_DATE, endDate)
                    putString(PREF_SUBSCRIPTION_PLAN, plan.id)
                    apply()
                }
                
                currentSuccessCallback?.invoke(paymentId)
                Log.d("PaymentService", "Subscription activated for plan: ${plan.id}")
            } catch (e: Exception) {
                Log.e("PaymentService", "Error handling payment success", e)
                currentFailureCallback?.invoke("Failed to activate subscription")
            } finally {
                clearCallbacks()
            }
        }
    }
    
    fun handlePaymentFailure(code: Int, response: String) {
        val errorMessage = when (code) {
            1 -> "Payment cancelled by user"
            2 -> "Network error occurred"
            else -> "Payment failed: $response"
        }
        currentFailureCallback?.invoke(errorMessage)
        clearCallbacks()
    }
    
    private fun clearCallbacks() {
        currentPlan = null
        currentSuccessCallback = null
        currentFailureCallback = null
    }
    
    fun initializePayment(
        activity: Activity,
        plan: SubscriptionPlan,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val checkout = Checkout()
            checkout.setKeyID(RAZORPAY_KEY)
            
            val options = JSONObject()
            options.put("name", "Taaza Khabar Premium")
            options.put("description", plan.description)
            options.put("theme.color", "#3399cc")
            options.put("currency", "INR")
            options.put("amount", (plan.price * 100).toInt()) // Amount in paise
            options.put("order_id", "order_${System.currentTimeMillis()}")
            
            val preFill = JSONObject()
            preFill.put("email", "test@razorpay.com")
            preFill.put("contact", "+919876543210")
            options.put("prefill", preFill)
            
            // Store current plan for success handling
            currentPlan = plan
            currentSuccessCallback = onSuccess
            currentFailureCallback = onFailure
            
            checkout.open(activity, options)
            
        } catch (e: Exception) {
            Log.e("PaymentService", "Error in payment initialization", e)
            onFailure("Payment initialization failed: ${e.message}")
        }
    }
    
    private fun createPaymentData(
        plan: SubscriptionPlan,
        customerEmail: String?,
        customerPhone: String?
    ): PaymentData {
        return PaymentData(
            orderId = generateOrderId(),
            amount = plan.price,
            currency = plan.currency,
            planId = plan.id,
            customerEmail = customerEmail,
            customerPhone = customerPhone
        )
    }
    
    private fun createRazorpayOptions(paymentData: PaymentData): JSONObject {
        val options = JSONObject()
        
        options.put("name", "Taaza Khabar Premium")
        options.put("description", "Premium subscription")
        options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
        options.put("order_id", paymentData.orderId)
        options.put("theme.color", "#3399cc")
        options.put("currency", paymentData.currency)
        options.put("amount", (paymentData.amount * 100).toInt()) // Amount in paise
        
        val preFill = JSONObject()
        paymentData.customerEmail?.let { preFill.put("email", it) }
        paymentData.customerPhone?.let { preFill.put("contact", it) }
        options.put("prefill", preFill)
        
        val retry = JSONObject()
        retry.put("enabled", true)
        retry.put("max_count", 3)
        options.put("retry", retry)
        
        return options
    }
    
    private fun openPaymentGateway(
        activity: Activity,
        options: JSONObject,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val checkout = Checkout()
            checkout.setKeyID(RAZORPAY_KEY)
            
            // Create a temporary listener
            val paymentListener = object : PaymentResultListener {
                override fun onPaymentSuccess(paymentId: String) {
                    handlePaymentSuccess(paymentId, options.getString("order_id"))
                    onSuccess(paymentId)
                }
                
                override fun onPaymentError(code: Int, response: String) {
                    Log.e("PaymentService", "Payment failed: $code - $response")
                    onFailure("Payment failed: $response")
                }
            }
            
            // This is a simplified approach - in production, you'd want to properly handle this
            checkout.open(activity, options)
            
        } catch (e: Exception) {
            Log.e("PaymentService", "Error opening payment gateway", e)
            onFailure("Failed to open payment gateway: ${e.message}")
        }
    }
    
    private fun handlePaymentSuccess(paymentId: String, orderId: String) {
        try {
            // In a real app, you'd verify the payment with your backend
            // For demo purposes, we'll store the subscription locally
            
            val planId = extractPlanIdFromOrder(orderId)
            val plan = com.brocoders.taaza_khabar.data.model.SubscriptionPlans.getPlanById(planId)
            
            plan?.let {
                val endDate = System.currentTimeMillis() + (it.durationDays * 24 * 60 * 60 * 1000L)
                
                sharedPreferences.edit()
                    .putBoolean(PREF_SUBSCRIPTION_ACTIVE, true)
                    .putLong(PREF_SUBSCRIPTION_END_DATE, endDate)
                    .putString(PREF_SUBSCRIPTION_PLAN, it.id)
                    .apply()
                
                Log.d("PaymentService", "Subscription activated for plan: ${it.name}")
            }
            
        } catch (e: Exception) {
            Log.e("PaymentService", "Error handling payment success", e)
        }
    }
    
    fun isSubscriptionActive(): Boolean {
        val isActive = sharedPreferences.getBoolean(PREF_SUBSCRIPTION_ACTIVE, false)
        val endDate = sharedPreferences.getLong(PREF_SUBSCRIPTION_END_DATE, 0)
        val currentTime = System.currentTimeMillis()
        
        return isActive && currentTime < endDate
    }
    
    fun getActiveSubscriptionPlan(): SubscriptionPlan? {
        if (!isSubscriptionActive()) return null
        
        val planId = sharedPreferences.getString(PREF_SUBSCRIPTION_PLAN, null)
        return planId?.let { 
            com.brocoders.taaza_khabar.data.model.SubscriptionPlans.getPlanById(it) 
        }
    }
    
    fun getSubscriptionEndDate(): Long {
        return sharedPreferences.getLong(PREF_SUBSCRIPTION_END_DATE, 0)
    }
    
    fun cancelSubscription() {
        sharedPreferences.edit()
            .putBoolean(PREF_SUBSCRIPTION_ACTIVE, false)
            .remove(PREF_SUBSCRIPTION_END_DATE)
            .remove(PREF_SUBSCRIPTION_PLAN)
            .apply()
    }
    
    private fun generateOrderId(): String {
        return "order_${System.currentTimeMillis()}_${Random().nextInt(10000)}"
    }
    
    private fun extractPlanIdFromOrder(orderId: String): String {
        // This is a simple implementation - in production, you'd have better order tracking
        return "monthly" // Default plan for demo
    }
} 