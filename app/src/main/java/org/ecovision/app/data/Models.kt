package org.ecovision.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class UserRole(val code: String, val displayName: String) {
    USER("user", "Household / Seller"),
    AGENT("agent", "Pickup Agent"),
    COMPANY("company", "Buyer Company (Recycler)"),
    ADMIN("admin", "Platform Admin")
}

enum class KycStatus(val code: String) {
    PENDING("pending"),
    VERIFIED("verified"),
    REJECTED("rejected")
}

enum class PickupStatus(val code: String, val title: String) {
    REQUESTED("requested", "Pickup Requested"),
    AGENT_ASSIGNED("agent_assigned", "Agent Assigned"),
    AT_DOORSTEP("at_doorstep", "Agent at Doorstep"),
    SPOT_WEIGHED("spot_weighed", "Spot Weighed & Price Revised"),
    COMPLETED("completed", "Completed & Paid"),
    CANCELLED("cancelled", "Cancelled")
}

@Serializable
data class PlatformSettings(
    val id: String = "global",
    @SerialName("maintenance_mode") val maintenanceMode: Boolean = false,
    @SerialName("maintenance_roles") val maintenanceRoles: List<String> = emptyList(),
    @SerialName("maintenance_notice") val maintenanceNotice: String = "EcoVision platform maintenance in progress.",
    @SerialName("estimated_recovery") val estimatedRecovery: String? = null,
    @SerialName("min_pickup_threshold_amount") val minPickupThresholdAmount: Double = 100.0,
    @SerialName("min_pickup_threshold_weight") val minPickupThresholdWeight: Double = 3.0,
    @SerialName("min_redemption_coins") val minRedemptionCoins: Int = 200,
    @SerialName("coin_to_cash_rate") val coinToCashRate: Double = 0.10
)

val KeralaDistricts = listOf(
    "Malappuram",
    "Alappuzha",
    "Ernakulam",
    "Idukki",
    "Kannur",
    "Kasaragod",
    "Kollam",
    "Kottayam",
    "Kozhikode",
    "Palakkad",
    "Pathanamthitta",
    "Thiruvananthapuram",
    "Thrissur",
    "Wayanad"
)

@Serializable
data class Profile(
    val id: String,
    val username: String? = null,
    val mobile: String,
    @SerialName("full_name") val fullName: String,
    val email: String? = null,
    @SerialName("password_hash") val passwordHash: String = "",
    val role: String = "user",
    @SerialName("kyc_status") val kycStatus: String = "pending",
    @SerialName("kyc_document_type") val kycDocumentType: String? = null,
    @SerialName("kyc_document_number") val kycDocumentNumber: String? = null,
    @SerialName("kyc_document_url") val kycDocumentUrl: String? = null,
    val district: String? = "Malappuram",
    @SerialName("default_address") val defaultAddress: String? = null,
    val pincode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("device_info") val deviceInfo: String? = null,
    @SerialName("device_ip") val deviceIp: String? = null,
    @SerialName("kyc_rejection_reason") val kycRejectionReason: String? = null,
    @SerialName("kyc_attempt_count") val kycAttemptCount: Int = 0,
    @SerialName("kyc_last_rejected_at") val kycLastRejectedAt: String? = null,
    @SerialName("upi_id") val upiId: String? = null,
    @SerialName("upi_holder_name") val upiHolderName: String? = null,
    @SerialName("referral_code") val referralCode: String? = null,
    @SerialName("referred_by") val referredBy: String? = null,
    @SerialName("leaf_coins") val leafCoins: Int = 50,
    @SerialName("totp_enabled") val totpEnabled: Boolean = false,
    @SerialName("two_factor_secret") val twoFactorSecret: String? = null,
    @SerialName("email_otp_enabled") val emailOtpEnabled: Boolean = false,
    @SerialName("account_status") val accountStatus: String = "active", // "active", "deactivated"
    @SerialName("scheduled_deletion_at") val scheduledDeletionAt: String? = null,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AdminNotification(
    val id: String = "",
    val title: String,
    val message: String,
    @SerialName("target_role") val targetRole: String = "all",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CashRedemption(
    val id: String = "",
    @SerialName("user_id") val userId: String,
    @SerialName("coins_amount") val coinsAmount: Int,
    @SerialName("cash_amount") val cashAmount: Double,
    @SerialName("upi_id") val upiId: String,
    @SerialName("holder_name") val holderName: String,
    val status: String = "waitlist", // "waitlist", "processing", "completed", "rejected"
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("settled_at") val settledAt: String? = null
)

@Serializable
data class VehicleChangeRequest(
    val id: String = "",
    @SerialName("agent_id") val agentId: String,
    @SerialName("old_vehicle_number") val oldVehicleNumber: String? = null,
    @SerialName("new_vehicle_number") val newVehicleNumber: String,
    @SerialName("vehicle_photo_url") val vehiclePhotoUrl: String? = null,
    val status: String = "pending", // "pending", "approved", "rejected"
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("reviewed_at") val reviewedAt: String? = null
)

@Serializable
data class AgentDetails(
    @SerialName("agent_id") val agentId: String,
    @SerialName("vehicle_number") val vehicleNumber: String,
    @SerialName("vehicle_photo_url") val vehiclePhotoUrl: String? = null,
    @SerialName("rc_book_url") val rcBookUrl: String? = null,
    @SerialName("assigned_zones") val assignedZones: List<String> = emptyList(),
    @SerialName("allowed_days") val allowedDays: List<String> = emptyList(),
    @SerialName("average_rating") val averageRating: Double = 5.0,
    @SerialName("total_ratings") val totalRatings: Int = 0,
    @SerialName("is_available") val isAvailable: Boolean = true
)

@Serializable
data class CompanyDetails(
    @SerialName("company_id") val companyId: String,
    @SerialName("company_name") val companyName: String,
    @SerialName("gst_number") val gstNumber: String,
    @SerialName("business_reg_id") val businessRegId: String? = null,
    @SerialName("authorized_signatory") val authorizedSignatory: String? = null,
    @SerialName("materials_of_interest") val materialsOfInterest: List<String> = emptyList()
)

@Serializable
data class WasteCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("is_recyclable") val isRecyclable: Boolean = true,
    @SerialName("base_rate_per_kg") val baseRatePerKg: Double,
    val unit: String = "kg",
    @SerialName("diy_suggestions") val diySuggestions: List<String> = emptyList(),
    @SerialName("icon_name") val iconName: String = "recycle"
)

@Serializable
data class WeightPricingSlab(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("tier_name") val tierName: String,
    @SerialName("min_kg") val minKg: Double = 0.0,
    @SerialName("max_kg") val maxKg: Double? = null,
    @SerialName("rate_per_kg") val ratePerKg: Double
)

@Serializable
data class VirtualBinItem(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("ai_confidence") val aiConfidence: Double,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("estimated_weight_kg") val estimatedWeightKg: Double,
    @SerialName("estimated_price") val estimatedPrice: Double,
    @SerialName("is_in_request") val isInRequest: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AdminNotification(
    val id: String = "",
    val title: String,
    val message: String,
    @SerialName("target_role") val targetRole: String = "all",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class PickupRequest(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("agent_id") val agentId: String? = null,
    val status: String = "requested",
    @SerialName("total_estimated_weight") val totalEstimatedWeight: Double = 0.0,
    @SerialName("total_estimated_amount") val totalEstimatedAmount: Double = 0.0,
    @SerialName("actual_verified_weight") val actualVerifiedWeight: Double? = null,
    @SerialName("final_cash_amount") val finalCashAmount: Double? = null,
    val pincode: String,
    @SerialName("pickup_address") val pickupAddress: String,
    @SerialName("assigned_at") val assignedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // Joined fields for UI convenience
    val user: Profile? = null,
    val agent: Profile? = null,
    val agentDetails: AgentDetails? = null
)

@Serializable
data class GodownInventoryItem(
    val id: String,
    @SerialName("pickup_request_id") val pickupRequestId: String? = null,
    @SerialName("agent_id") val agentId: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("verified_weight_kg") val verifiedWeightKg: Double,
    val status: String = "pending_acceptance",
    @SerialName("accepted_at") val acceptedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AdminNotification(
    val id: String = "",
    val title: String,
    val message: String,
    @SerialName("target_role") val targetRole: String = "all",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class BulkOrder(
    val id: String,
    @SerialName("company_id") val companyId: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("total_weight_kg") val totalWeightKg: Double,
    @SerialName("total_amount") val totalAmount: Double,
    val status: String = "ordered",
    @SerialName("manifest_number") val manifestNumber: String,
    @SerialName("esg_carbon_offset_kg") val esgCarbonOffsetKg: Double = 0.0,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AdminNotification(
    val id: String = "",
    val title: String,
    val message: String,
    @SerialName("target_role") val targetRole: String = "all",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class MarketplaceProduct(
    val id: String,
    @SerialName("seller_id") val sellerId: String,
    val title: String,
    val description: String,
    @SerialName("source_waste_material") val sourceWasteMaterial: String,
    @SerialName("asking_price") val askingPrice: Double,
    @SerialName("image_url") val imageUrl: String? = null,
    val images: List<String> = emptyList(),
    val status: String = "active",
    @SerialName("created_at") val createdAt: String? = null,
    val seller: Profile? = null
)

@Serializable
data class BargainOffer(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("offer_price") val offerPrice: Double,
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null,
    val buyer: Profile? = null
)

@Serializable
data class AgentReview(
    val id: String,
    @SerialName("pickup_request_id") val pickupRequestId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("agent_id") val agentId: String,
    val rating: Int,
    @SerialName("review_text") val reviewText: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AdminNotification(
    val id: String = "",
    val title: String,
    val message: String,
    @SerialName("target_role") val targetRole: String = "all",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AgentReport(
    val id: String,
    @SerialName("pickup_request_id") val pickupRequestId: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("agent_id") val agentId: String,
    val reason: String,
    val details: String,
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AdminNotification(
    val id: String = "",
    val title: String,
    val message: String,
    @SerialName("target_role") val targetRole: String = "all",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class LeafCoinTransaction(
    val id: String,
    @SerialName("user_id") val userId: String,
    val amount: Int,
    val type: String,
    val description: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AdminNotification(
    val id: String = "",
    val title: String,
    val message: String,
    @SerialName("target_role") val targetRole: String = "all",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class YouTubeUpcycleVideo(
    val title: String,
    val channel: String,
    val duration: String,
    val videoId: String,
    val thumbnailUrl: String,
    val description: String = ""
)

// AI Detection Output
data class AiDetectionResult(
    val categoryId: String,
    val categoryName: String,
    val wasteName: String = "Scanned Waste Item",
    val wasteType: String = "Recyclable Scrap",
    val material: String = "Clean Recyclable",
    val confidence: Double = 96.0,
    val isRecyclable: Boolean = true,
    val estimatedBasePrice: Double = 15.0,
    val estimatedWeightKg: Double = 1.5,
    val diyRecommendations: List<String> = emptyList(),
    val upcycleVideos: List<YouTubeUpcycleVideo> = emptyList(),
    val imageUrl: String? = null
)

// Vouchers for Coins
@Serializable
data class EcoVoucher(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("coin_cost") val coinCost: Int = 100,
    @SerialName("discount_value") val discountValue: Double = 10.0,
    @SerialName("brand_name") val brandName: String = "EcoPartner",
    val category: String = "shopping",
    @SerialName("is_active") val isActive: Boolean = true
)

// User Voucher Redemptions (Waiting List & Delivery)
@Serializable
data class VoucherRedemption(
    val id: String = "",
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("user_email") val userEmail: String? = null,
    @SerialName("voucher_id") val voucherId: String,
    @SerialName("voucher_title") val voucherTitle: String,
    @SerialName("brand_name") val brandName: String = "EcoPartner",
    @SerialName("coin_cost") val coinCost: Int,
    @SerialName("voucher_details") val voucherDetails: String? = null, // Admin-entered code, link, PIN or details
    val status: String = "waitlist", // "waitlist", "completed", "rejected"
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("fulfilled_at") val fulfilledAt: String? = null
)

// Marketplace Cart & Orders
data class CartItem(
    val product: MarketplaceProduct,
    var quantity: Int = 1
)

@Serializable
data class MarketplaceOrder(
    val id: String,
    @SerialName("user_id") val userId: String,
    val subtotal: Double,
    @SerialName("delivery_fee") val deliveryFee: Double,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("delivery_address") val deliveryAddress: String,
    val status: String = "placed", // 'placed', 'assigned', 'picked_up', 'delivered'
    @SerialName("assigned_agent_id") val assignedAgentId: String? = null,
    @SerialName("payment_method") val paymentMethod: String = "online",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AdminNotification(
    val id: String = "",
    val title: String,
    val message: String,
    @SerialName("target_role") val targetRole: String = "all",
    @SerialName("created_at") val createdAt: String? = null
)
