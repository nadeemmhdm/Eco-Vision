package org.ecovision.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

object SupabaseClient {
    private const val BASE_URL = "https://uarkttbfqmfgcewddvtn.supabase.co"
    private const val REST_URL = "$BASE_URL/rest/v1"
    private const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVhcmt0dGJmcW1mZ2Nld2RkdnRuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODU0NjA1MjYsImV4cCI6MjEwMTAzNjUyNn0.PLuujmtJ4AlQoI2q3xYoV-EBbtbO1sjIqde72GocVB0"

    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.supabaseHeaders(preferReturn: Boolean = false) {
        header("apikey", ANON_KEY)
        header("Authorization", "Bearer $ANON_KEY")
        if (preferReturn) {
            header("Prefer", "return=representation")
        }
    }

    // --- Platform Settings & Maintenance ---
    suspend fun getPlatformSettings(): PlatformSettings? {
        return try {
            val list = httpClient.get("$REST_URL/platform_settings") {
                supabaseHeaders()
                parameter("id", "eq.global")
            }.body<List<PlatformSettings>>()
            list.firstOrNull() ?: PlatformSettings()
        } catch (e: Exception) {
            e.printStackTrace()
            PlatformSettings()
        }
    }

    suspend fun updatePlatformSettings(settings: PlatformSettings): Boolean {
        return try {
            httpClient.patch("$REST_URL/platform_settings") {
                supabaseHeaders()
                parameter("id", "eq.global")
                contentType(ContentType.Application.Json)
                setBody(settings)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- Auth & Profiles ---
    suspend fun findProfileByUsername(username: String): Profile? {
        return try {
            val list = httpClient.get("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("username", "eq.$username")
            }.body<List<Profile>>()
            list.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun findProfileByMobile(mobile: String): Profile? {
        return try {
            val list = httpClient.get("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("mobile", "eq.$mobile")
            }.body<List<Profile>>()
            list.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createProfile(profile: Profile, agentDetails: AgentDetails? = null, companyDetails: CompanyDetails? = null): Profile? {
        return try {
            val created = httpClient.post("$REST_URL/profiles") {
                supabaseHeaders(preferReturn = true)
                contentType(ContentType.Application.Json)
                setBody(profile)
            }.body<List<Profile>>().firstOrNull() ?: profile

            if (agentDetails != null) {
                httpClient.post("$REST_URL/agent_details") {
                    supabaseHeaders()
                    contentType(ContentType.Application.Json)
                    setBody(agentDetails.copy(agentId = created.id))
                }
            }
            if (companyDetails != null) {
                httpClient.post("$REST_URL/company_details") {
                    supabaseHeaders()
                    contentType(ContentType.Application.Json)
                    setBody(companyDetails.copy(companyId = created.id))
                }
            }
            created
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAgentDetails(agentId: String): AgentDetails? {
        return try {
            val list = httpClient.get("$REST_URL/agent_details") {
                supabaseHeaders()
                parameter("agent_id", "eq.$agentId")
            }.body<List<AgentDetails>>()
            list.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCompanyDetails(companyId: String): CompanyDetails? {
        return try {
            val list = httpClient.get("$REST_URL/company_details") {
                supabaseHeaders()
                parameter("company_id", "eq.$companyId")
            }.body<List<CompanyDetails>>()
            list.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // --- Waste Categories & Slabs ---
    suspend fun getCategories(): List<WasteCategory> {
        return try {
            httpClient.get("$REST_URL/waste_categories") {
                supabaseHeaders()
                parameter("order", "is_recyclable.desc,base_rate_per_kg.desc")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPricingSlabs(): List<WeightPricingSlab> {
        return try {
            httpClient.get("$REST_URL/weight_pricing_slabs") {
                supabaseHeaders()
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun calculateSlabRate(slabs: List<WeightPricingSlab>, categoryId: String, weightKg: Double, defaultRate: Double): Double {
        val catSlabs = slabs.filter { it.categoryId == categoryId }
        val matched = catSlabs.firstOrNull { slab ->
            weightKg >= slab.minKg && (slab.maxKg == null || weightKg < slab.maxKg)
        }
        return matched?.ratePerKg ?: defaultRate
    }

    // --- Virtual Bin ---
    suspend fun getVirtualBin(userId: String): List<VirtualBinItem> {
        return try {
            httpClient.get("$REST_URL/virtual_bin_items") {
                supabaseHeaders()
                parameter("user_id", "eq.$userId")
                parameter("is_in_request", "eq.false")
                parameter("order", "created_at.desc")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addToVirtualBin(item: VirtualBinItem): Boolean {
        return try {
            httpClient.post("$REST_URL/virtual_bin_items") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(item)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteVirtualBinItem(id: String): Boolean {
        return try {
            httpClient.delete("$REST_URL/virtual_bin_items") {
                supabaseHeaders()
                parameter("id", "eq.$id")
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- Pickup Requests ---
    suspend fun createPickupRequest(
        userId: String,
        pincode: String,
        address: String,
        totalWeight: Double,
        totalAmount: Double,
        binItemIds: List<String>
    ): PickupRequest? {
        return try {
            val req = PickupRequest(
                id = UUID.randomUUID().toString(),
                userId = userId,
                status = "requested",
                totalEstimatedWeight = totalWeight,
                totalEstimatedAmount = totalAmount,
                pincode = pincode,
                pickupAddress = address
            )
            val created = httpClient.post("$REST_URL/pickup_requests") {
                supabaseHeaders(preferReturn = true)
                contentType(ContentType.Application.Json)
                setBody(req)
            }.body<List<PickupRequest>>().firstOrNull() ?: req

            // Mark bin items as in request
            for (id in binItemIds) {
                httpClient.patch("$REST_URL/virtual_bin_items") {
                    supabaseHeaders()
                    parameter("id", "eq.$id")
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject { put("is_in_request", true) })
                }
            }
            created
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getUserPickups(userId: String): List<PickupRequest> {
        return try {
            httpClient.get("$REST_URL/pickup_requests") {
                supabaseHeaders()
                parameter("user_id", "eq.$userId")
                parameter("order", "created_at.desc")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAgentAvailablePickups(zones: List<String>): List<PickupRequest> {
        return try {
            // Pickups in requested state
            httpClient.get("$REST_URL/pickup_requests") {
                supabaseHeaders()
                parameter("status", "eq.requested")
                parameter("order", "created_at.desc")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAgentActivePickups(agentId: String): List<PickupRequest> {
        return try {
            httpClient.get("$REST_URL/pickup_requests") {
                supabaseHeaders()
                parameter("agent_id", "eq.$agentId")
                parameter("order", "created_at.desc")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun acceptPickup(requestId: String, agentId: String): Boolean {
        return try {
            httpClient.patch("$REST_URL/pickup_requests") {
                supabaseHeaders()
                parameter("id", "eq.$requestId")
                parameter("status", "eq.requested") // atomic check
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("agent_id", agentId)
                    put("status", "agent_assigned")
                    put("assigned_at", java.time.Instant.now().toString())
                })
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markAtDoorstep(requestId: String): Boolean {
        return try {
            httpClient.patch("$REST_URL/pickup_requests") {
                supabaseHeaders()
                parameter("id", "eq.$requestId")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("status", "at_doorstep")
                })
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateSpotWeightAndPrice(requestId: String, actualWeight: Double, finalAmount: Double): Boolean {
        return try {
            httpClient.patch("$REST_URL/pickup_requests") {
                supabaseHeaders()
                parameter("id", "eq.$requestId")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("actual_verified_weight", actualWeight)
                    put("final_cash_amount", finalAmount)
                    put("status", "spot_weighed")
                })
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun completePickupAndPayCash(
        request: PickupRequest,
        agentId: String,
        actualWeight: Double,
        finalAmount: Double
    ): Boolean {
        return try {
            // 1. Mark request completed
            httpClient.patch("$REST_URL/pickup_requests") {
                supabaseHeaders()
                parameter("id", "eq.${request.id}")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("actual_verified_weight", actualWeight)
                    put("final_cash_amount", finalAmount)
                    put("status", "completed")
                    put("completed_at", java.time.Instant.now().toString())
                })
            }

            // 2. Add to Godown Inventory (Pending Acceptance)
            val godownItem = GodownInventoryItem(
                id = UUID.randomUUID().toString(),
                pickupRequestId = request.id,
                agentId = agentId,
                categoryId = "pet_plastic", // default or pooled category
                verifiedWeightKg = actualWeight,
                status = "pending_acceptance"
            )
            httpClient.post("$REST_URL/godown_inventory") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(godownItem)
            }

            // 3. Award Leaf Coins (e.g. 10 coins per kg verified)
            val coinsEarned = (actualWeight * 10).toInt().coerceAtLeast(10)
            httpClient.post("$REST_URL/leaf_coin_transactions") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(LeafCoinTransaction(
                    id = UUID.randomUUID().toString(),
                    userId = request.userId,
                    amount = coinsEarned,
                    type = "waste_recycled",
                    description = "Verified sale of ${actualWeight} kg recyclables"
                ))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- Godown & Bulk Marketplace ---
    suspend fun getGodownStockForAgent(agentId: String): List<GodownInventoryItem> {
        return try {
            httpClient.get("$REST_URL/godown_inventory") {
                supabaseHeaders()
                parameter("agent_id", "eq.$agentId")
                parameter("order", "created_at.desc")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun acceptToGodownStock(itemId: String): Boolean {
        return try {
            httpClient.patch("$REST_URL/godown_inventory") {
                supabaseHeaders()
                parameter("id", "eq.$itemId")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("status", "godown_accepted")
                    put("accepted_at", java.time.Instant.now().toString())
                })
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getVerifiedGodownFeed(): List<GodownInventoryItem> {
        return try {
            httpClient.get("$REST_URL/godown_inventory") {
                supabaseHeaders()
                parameter("status", "eq.godown_accepted")
                parameter("order", "created_at.desc")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createBulkPurchaseOrder(
        companyId: String,
        categoryId: String,
        weightKg: Double,
        totalAmount: Double
    ): BulkOrder? {
        return try {
            val manifest = "EV-MANIFEST-" + System.currentTimeMillis().toString().takeLast(8)
            val carbonOffset = weightKg * 1.85 // 1.85 kg CO2 saved per kg recycled
            val order = BulkOrder(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                categoryId = categoryId,
                totalWeightKg = weightKg,
                totalAmount = totalAmount,
                status = "confirmed",
                manifestNumber = manifest,
                esgCarbonOffsetKg = carbonOffset
            )
            httpClient.post("$REST_URL/bulk_orders") {
                supabaseHeaders(preferReturn = true)
                contentType(ContentType.Application.Json)
                setBody(order)
            }.body<List<BulkOrder>>().firstOrNull() ?: order
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCompanyBulkOrders(companyId: String): List<BulkOrder> {
        return try {
            httpClient.get("$REST_URL/bulk_orders") {
                supabaseHeaders()
                parameter("company_id", "eq.$companyId")
                parameter("order", "created_at.desc")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Upcycled P2P Marketplace ---
    suspend fun getMarketplaceProducts(): List<MarketplaceProduct> {
        return try {
            httpClient.get("$REST_URL/marketplace_products") {
                supabaseHeaders()
                parameter("order", "created_at.desc")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createMarketplaceProduct(product: MarketplaceProduct): Boolean {
        return try {
            httpClient.post("$REST_URL/marketplace_products") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(product)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun submitBargainOffer(offer: BargainOffer): Boolean {
        return try {
            httpClient.post("$REST_URL/bargain_offers") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(offer)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- Reviews, Reports & Leaf Coins ---
    suspend fun submitReview(review: AgentReview): Boolean {
        return try {
            httpClient.post("$REST_URL/agent_reviews") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(review)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun submitReport(report: AgentReport): Boolean {
        return try {
            httpClient.post("$REST_URL/agent_reports") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(report)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLeafCoinTransactions(userId: String): List<LeafCoinTransaction> {
        return try {
            httpClient.get("$REST_URL/leaf_coin_transactions") {
                supabaseHeaders()
                parameter("user_id", "eq.$userId")
                parameter("order", "created_at.desc")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun redeemLeafCoins(userId: String, coins: Int, type: String): Boolean {
        return try {
            httpClient.post("$REST_URL/leaf_coin_transactions") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(LeafCoinTransaction(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    amount = -coins,
                    type = type,
                    description = "Redeemed $coins Leaf Coins for $type"
                ))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- Admin Governance Desk ---
    suspend fun getAllProfiles(): List<Profile> {
        return try {
            httpClient.get("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("order", "created_at.desc")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateKycStatus(profileId: String, status: String): Boolean {
        return try {
            httpClient.patch("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("id", "eq.$profileId")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("kyc_status", status) })
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updatePricingSlabRate(slabId: String, newRate: Double): Boolean {
        return try {
            httpClient.patch("$REST_URL/weight_pricing_slabs") {
                supabaseHeaders()
                parameter("id", "eq.$slabId")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("rate_per_kg", newRate) })
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
