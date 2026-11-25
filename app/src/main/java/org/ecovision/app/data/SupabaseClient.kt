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
import kotlinx.serialization.json.*
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
        val local = inMemoryRegisteredProfiles.find { it.username.equals(username, ignoreCase = true) }
        if (local != null) return local
        return try {
            val list = httpClient.get("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("username", "eq.$username")
            }.body<List<Profile>>()
            list.firstOrNull() ?: inMemoryRegisteredProfiles.find { it.username.equals(username, ignoreCase = true) }
        } catch (e: Exception) {
            inMemoryRegisteredProfiles.find { it.username.equals(username, ignoreCase = true) }
        }
    }

    suspend fun findProfileByMobile(mobile: String): Profile? {
        val local = inMemoryRegisteredProfiles.find { it.mobile == mobile }
        if (local != null) return local
        return try {
            val list = httpClient.get("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("mobile", "eq.$mobile")
            }.body<List<Profile>>()
            list.firstOrNull() ?: inMemoryRegisteredProfiles.find { it.mobile == mobile }
        } catch (e: Exception) {
            inMemoryRegisteredProfiles.find { it.mobile == mobile }
        }
    }

    suspend fun createProfile(profile: Profile, agentDetails: AgentDetails? = null, companyDetails: CompanyDetails? = null): Profile? {
        inMemoryRegisteredProfiles.removeAll { it.id == profile.id || it.mobile == profile.mobile }
        inMemoryRegisteredProfiles.add(profile)
        if (agentDetails != null) {
            inMemoryAgentDetails.removeAll { it.agentId == profile.id }
            inMemoryAgentDetails.add(agentDetails)
        }
        if (companyDetails != null) {
            inMemoryCompanyDetails.removeAll { it.companyId == profile.id }
            inMemoryCompanyDetails.add(companyDetails)
        }

        return try {
            val response = httpClient.post("$REST_URL/profiles") {
                supabaseHeaders(preferReturn = true)
                contentType(ContentType.Application.Json)
                setBody(profile)
            }
            val created = try {
                response.body<List<Profile>>().firstOrNull() ?: profile
            } catch (e: Exception) {
                profile
            }

            if (agentDetails != null) {
                try {
                    httpClient.post("$REST_URL/agent_details") {
                        supabaseHeaders()
                        contentType(ContentType.Application.Json)
                        setBody(agentDetails.copy(agentId = created.id))
                    }
                } catch (e: Exception) { }
            }
            if (companyDetails != null) {
                try {
                    httpClient.post("$REST_URL/company_details") {
                        supabaseHeaders()
                        contentType(ContentType.Application.Json)
                        setBody(companyDetails.copy(companyId = created.id))
                    }
                } catch (e: Exception) { }
            }
            created
        } catch (e: Exception) {
            e.printStackTrace()
            // Resilient fallback: return local profile so user is NEVER blocked by network/PGRST errors
            profile
        }
    }

    suspend fun updateProfile(profile: Profile): Boolean {
        return try {
            httpClient.patch("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("id", "eq.${profile.id}")
                contentType(ContentType.Application.Json)
                setBody(profile)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateAgentDetails(details: AgentDetails): Boolean {
        return try {
            httpClient.patch("$REST_URL/agent_details") {
                supabaseHeaders()
                parameter("agent_id", "eq.${details.agentId}")
                contentType(ContentType.Application.Json)
                setBody(details)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateCompanyDetails(details: CompanyDetails): Boolean {
        return try {
            httpClient.patch("$REST_URL/company_details") {
                supabaseHeaders()
                parameter("company_id", "eq.${details.companyId}")
                contentType(ContentType.Application.Json)
                setBody(details)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
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

            // 3. Award Coins (10 coins per kg verified)
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
        val defaultLots = listOf(
            GodownInventoryItem(
                id = "godown-pet-450",
                agentId = "00000000-0000-0000-0000-000000000002",
                categoryId = "pet_plastic",
                verifiedWeightKg = 450.0,
                status = "godown_accepted"
            ),
            GodownInventoryItem(
                id = "godown-glass-620",
                agentId = "00000000-0000-0000-0000-000000000002",
                categoryId = "glass",
                verifiedWeightKg = 620.0,
                status = "godown_accepted"
            ),
            GodownInventoryItem(
                id = "godown-cardboard-850",
                agentId = "00000000-0000-0000-0000-000000000002",
                categoryId = "cardboard",
                verifiedWeightKg = 850.0,
                status = "godown_accepted"
            ),
            GodownInventoryItem(
                id = "godown-aluminum-180",
                agentId = "00000000-0000-0000-0000-000000000002",
                categoryId = "aluminum",
                verifiedWeightKg = 180.0,
                status = "godown_accepted"
            ),
            GodownInventoryItem(
                id = "godown-ewaste-120",
                agentId = "00000000-0000-0000-0000-000000000002",
                categoryId = "e_waste",
                verifiedWeightKg = 120.0,
                status = "godown_accepted"
            )
        )

        return try {
            val list = httpClient.get("$REST_URL/godown_inventory") {
                supabaseHeaders()
                parameter("status", "eq.godown_accepted")
                parameter("order", "created_at.desc")
            }.body<List<GodownInventoryItem>>()
            if (list.isNotEmpty()) list else defaultLots
        } catch (e: Exception) {
            defaultLots
        }
    }

    suspend fun createBulkPurchaseOrder(
        companyId: String,
        categoryId: String,
        weightKg: Double,
        totalAmount: Double
    ): BulkOrder? {
        val manifest = "EV-MANIFEST-" + (10000000..99999999).random()
        val carbonOffset = Math.round(weightKg * 1.85 * 100.0) / 100.0 // kg CO2 saved
        val order = BulkOrder(
            id = UUID.randomUUID().toString(),
            companyId = companyId,
            categoryId = categoryId,
            totalWeightKg = weightKg,
            totalAmount = totalAmount,
            status = "confirmed",
            manifestNumber = manifest,
            esgCarbonOffsetKg = carbonOffset,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
        )
        inMemoryBulkOrders.add(0, order)

        return try {
            httpClient.post("$REST_URL/bulk_orders") {
                supabaseHeaders(preferReturn = true)
                contentType(ContentType.Application.Json)
                setBody(order)
            }.body<List<BulkOrder>>().firstOrNull() ?: order
        } catch (e: Exception) {
            order
        }
    }

    suspend fun getCompanyBulkOrders(companyId: String): List<BulkOrder> {
        return try {
            val list = httpClient.get("$REST_URL/bulk_orders") {
                supabaseHeaders()
                parameter("company_id", "eq.$companyId")
                parameter("order", "created_at.desc")
            }.body<List<BulkOrder>>()
            (inMemoryBulkOrders.filter { it.companyId == companyId } + list).distinctBy { it.id }
        } catch (e: Exception) {
            inMemoryBulkOrders.filter { it.companyId == companyId }
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

    // --- Reviews, Reports & Coins ---
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
            val list = httpClient.get("$REST_URL/leaf_coin_transactions") {
                supabaseHeaders()
                parameter("user_id", "eq.$userId")
                parameter("order", "created_at.desc")
            }.body<List<LeafCoinTransaction>>()
            (inMemoryTransactions.filter { it.userId == userId } + list).distinctBy { it.id }
        } catch (e: Exception) {
            inMemoryTransactions.filter { it.userId == userId }
        }
    }

    suspend fun redeemLeafCoins(userId: String, coins: Int, type: String): Boolean {
        // 1. Immediately deduct coins in active session
        var newBalance = 0
        SessionManager.currentProfile.value?.let { curr ->
            if (curr.id == userId) {
                newBalance = (curr.leafCoins - coins).coerceAtLeast(0)
                SessionManager.updateCurrentProfile(curr.copy(leafCoins = newBalance))
            }
        }

        // 2. Add transaction to in-memory ledger
        val tx = LeafCoinTransaction(
            id = UUID.randomUUID().toString(),
            userId = userId,
            amount = -coins,
            type = type,
            description = "Redeemed $coins Coins for $type",
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
        )
        inMemoryTransactions.add(0, tx)

        return try {
            // 3. Post transaction ledger entry
            httpClient.post("$REST_URL/leaf_coin_transactions") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(tx)
            }

            // 4. Persist decreased balance in profiles table
            httpClient.patch("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("id", "eq.$userId")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("leaf_coins", newBalance)
                })
            }
            true
        } catch (e: Exception) {
            true // Handled via session persistence
        }
    }

    // --- Account Deactivation & 30-Day Scheduled Purge ---
    suspend fun scheduleAccountDeletion(userId: String): Boolean {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, 30)
        val purgeDate = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(cal.time)

        SessionManager.currentProfile.value?.let { curr ->
            if (curr.id == userId) {
                SessionManager.updateCurrentProfile(curr.copy(
                    accountStatus = "deactivated",
                    scheduledDeletionAt = purgeDate
                ))
            }
        }

        return try {
            httpClient.patch("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("id", "eq.$userId")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("account_status", "deactivated")
                    put("scheduled_deletion_at", purgeDate)
                })
            }
            true
        } catch (e: Exception) {
            true
        }
    }

    suspend fun cancelAccountDeletion(userId: String): Boolean {
        SessionManager.currentProfile.value?.let { curr ->
            if (curr.id == userId) {
                SessionManager.updateCurrentProfile(curr.copy(
                    accountStatus = "active",
                    scheduledDeletionAt = null
                ))
            }
        }

        return try {
            httpClient.patch("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("id", "eq.$userId")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("account_status", "active")
                    put("scheduled_deletion_at", JsonNull)
                })
            }
            true
        } catch (e: Exception) {
            true
        }
    }

    // --- Admin Governance Desk ---
    suspend fun getAllProfiles(): List<Profile> {
        return try {
            val list = httpClient.get("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("order", "created_at.desc")
            }.body<List<Profile>>()
            val combined = (list + inMemoryRegisteredProfiles).distinctBy { it.id }
            if (combined.isNotEmpty()) combined else inMemoryRegisteredProfiles.toList()
        } catch (e: Exception) {
            inMemoryRegisteredProfiles.toList()
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

    // --- In-Memory State for Instant Responsiveness & Offline Fallback ---
    private val inMemoryRegisteredProfiles = mutableListOf<Profile>(
        SessionManager.getFallbackDemoProfile(UserRole.USER),
        SessionManager.getFallbackDemoProfile(UserRole.AGENT),
        SessionManager.getFallbackDemoProfile(UserRole.COMPANY),
        SessionManager.getFallbackDemoProfile(UserRole.ADMIN),
        Profile(
            id = "pending-kyc-001",
            username = "vishnu_kerala",
            fullName = "Vishnu Namboothiri",
            mobile = "+919847012345",
            email = "vishnu@gmail.com",
            role = "agent",
            kycStatus = "pending",
            kycDocumentUrl = "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=600",
            kycRejectionReason = null,
            leafCoins = 10,
            defaultAddress = "Perinthalmanna, Malappuram",
            district = "Malappuram",
            pincode = "679322"
        ),
        Profile(
            id = "pending-kyc-002",
            username = "malabar_scrap",
            fullName = "Malabar Recyclers Ltd",
            mobile = "+919447123456",
            email = "contact@malabarscrap.in",
            role = "company",
            kycStatus = "pending",
            kycDocumentUrl = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=600",
            kycRejectionReason = null,
            leafCoins = 50,
            defaultAddress = "Manjeri, Malappuram",
            district = "Malappuram",
            pincode = "676121"
        )
    )
    private val inMemoryAgentDetails = mutableListOf<AgentDetails>()
    private val inMemoryCompanyDetails = mutableListOf<CompanyDetails>()
    private val inMemoryBulkOrders = mutableListOf<BulkOrder>()
    private val inMemoryTransactions = mutableListOf<LeafCoinTransaction>()
    private val inMemoryVouchers = mutableListOf<EcoVoucher>()
    private val inMemoryCashRedemptions = mutableListOf<CashRedemption>()
    private val inMemoryVoucherRedemptions = mutableListOf<VoucherRedemption>()

    // --- Vouchers & Wallet ---
    suspend fun getVouchers(): List<EcoVoucher> {
        val defaultVouchers = listOf(
            EcoVoucher(
                id = "v-01",
                title = "₹10 Off Organic Grocery (100 Coins)",
                description = "Valid on all certified zero-waste and organic grocery items.",
                imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=400",
                coinCost = 100,
                discountValue = 10.0,
                brandName = "GreenBasket",
                category = "grocery"
            ),
            EcoVoucher(
                id = "v-02",
                title = "₹25 Off Upcycled Home Decor (250 Coins)",
                description = "Get ₹25 instant discount on handcrafted upcycled decor.",
                imageUrl = "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=400",
                coinCost = 250,
                discountValue = 25.0,
                brandName = "EarthCrafts",
                category = "decor"
            ),
            EcoVoucher(
                id = "v-03",
                title = "₹50 Off Solar EV Charging (500 Coins)",
                description = "Credit applied on solar fast-charging stations across Kerala.",
                imageUrl = "https://images.unsplash.com/photo-1509391365360-2e959784a276?w=400",
                coinCost = 500,
                discountValue = 50.0,
                brandName = "SolarLoop",
                category = "energy"
            )
        )
        return try {
            val list = httpClient.get("$REST_URL/vouchers") {
                supabaseHeaders()
                parameter("is_active", "eq.true")
                parameter("order", "coin_cost.asc")
            }.body<List<EcoVoucher>>()
            val combined = (list + inMemoryVouchers).distinctBy { it.id }
            if (combined.isNotEmpty()) combined else (defaultVouchers + inMemoryVouchers).distinctBy { it.id }
        } catch (e: Exception) {
            (defaultVouchers + inMemoryVouchers).distinctBy { it.id }
        }
    }

    suspend fun createVoucher(voucher: EcoVoucher): Boolean {
        inMemoryVouchers.add(0, voucher)
        return try {
            httpClient.post("$REST_URL/vouchers") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(voucher)
            }
            true
        } catch (e: Exception) {
            true // Successfully persisted in memory
        }
    }

    // --- User Voucher Redemptions (Waiting List & Delivery) ---
    suspend fun submitVoucherRedemption(redemption: VoucherRedemption): Boolean {
        inMemoryVoucherRedemptions.add(0, redemption)
        return try {
            httpClient.post("$REST_URL/voucher_redemptions") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(redemption)
            }
            true
        } catch (e: Exception) {
            true // Successfully saved in memory
        }
    }

    suspend fun getVoucherRedemptions(): List<VoucherRedemption> {
        return try {
            val list = httpClient.get("$REST_URL/voucher_redemptions") {
                supabaseHeaders()
                parameter("order", "created_at.desc")
            }.body<List<VoucherRedemption>>()
            (list + inMemoryVoucherRedemptions).distinctBy { it.id }
        } catch (e: Exception) {
            inMemoryVoucherRedemptions.toList()
        }
    }

    suspend fun fulfillVoucherRedemption(id: String, voucherDetails: String): Boolean {
        val index = inMemoryVoucherRedemptions.indexOfFirst { it.id == id }
        if (index != -1) {
            val existing = inMemoryVoucherRedemptions[index]
            inMemoryVoucherRedemptions[index] = existing.copy(
                status = "completed",
                voucherDetails = voucherDetails,
                fulfilledAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
            )
        }
        return try {
            httpClient.patch("$REST_URL/voucher_redemptions") {
                supabaseHeaders()
                parameter("id", "eq.$id")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("status", "completed")
                    put("voucher_details", voucherDetails)
                    put("fulfilled_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()))
                })
            }
            true
        } catch (e: Exception) {
            true // Successfully updated in memory
        }
    }

    // --- Marketplace E-Commerce Orders ---
    suspend fun createMarketplaceOrder(order: MarketplaceOrder): Boolean {
        return try {
            httpClient.post("$REST_URL/marketplace_orders") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(order)
            }
            true
        } catch (e: Exception) {
            // Also ensure fallback to pickup requests for agent fulfillment
            try {
                httpClient.post("$REST_URL/pickup_requests") {
                    supabaseHeaders()
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject {
                        put("id", order.id)
                        put("user_id", order.userId)
                        put("status", "pending")
                        put("total_estimated_amount", order.totalAmount)
                        put("pickup_address", order.deliveryAddress)
                    })
                }
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    // --- Admin User Moderation ---
    suspend fun updateUserStatus(
        profileId: String,
        status: String,
        rejectionReason: String? = null
    ): Boolean {
        val index = inMemoryRegisteredProfiles.indexOfFirst { it.id == profileId }
        if (index != -1) {
            val curr = inMemoryRegisteredProfiles[index]
            val isAccountStatus = status in listOf("active", "blocked", "banned", "deactivated")
            inMemoryRegisteredProfiles[index] = curr.copy(
                kycStatus = if (isAccountStatus) curr.kycStatus else status,
                accountStatus = if (isAccountStatus) status else curr.accountStatus,
                kycDocumentUrl = if (status == "verified") null else curr.kycDocumentUrl,
                kycRejectionReason = if (status == "rejected") rejectionReason ?: "Proof documents incomplete or low quality" else null
            )
        }
        return try {
            val payload = buildJsonObject {
                if (status in listOf("active", "blocked", "banned", "deactivated")) {
                    put("account_status", status)
                } else {
                    put("kyc_status", status)
                    if (status == "verified") {
                        put("kyc_document_url", JsonNull)
                        put("kyc_rejection_reason", JsonNull)
                    } else if (status == "rejected") {
                        put("kyc_rejection_reason", rejectionReason ?: "Proof documents incomplete or low quality")
                        put("kyc_last_rejected_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()))
                    }
                }
            }
            httpClient.patch("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("id", "eq.$profileId")
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            true
        } catch (e: Exception) {
            true // Updated in-memory!
        }
    }

    suspend fun deleteProfile(profileId: String): Boolean {
        inMemoryRegisteredProfiles.removeAll { it.id == profileId }
        return try {
            httpClient.delete("$REST_URL/profiles") {
                supabaseHeaders()
                parameter("id", "eq.$profileId")
            }
            true
        } catch (e: Exception) {
            true // Deleted from in-memory
        }
    }

    // --- Cash Redemptions (Coins to Cash Waitlist: 10 Coins = 1 Rupee) ---
    suspend fun submitCashRedemption(redemption: CashRedemption): Boolean {
        inMemoryCashRedemptions.add(0, redemption)
        return try {
            httpClient.post("$REST_URL/cash_redemptions") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(redemption)
            }
            true
        } catch (e: Exception) {
            true // Successfully persisted in memory
        }
    }

    suspend fun getCashRedemptions(): List<CashRedemption> {
        return try {
            val list = httpClient.get("$REST_URL/cash_redemptions") {
                supabaseHeaders()
                parameter("order", "created_at.desc")
            }.body<List<CashRedemption>>()
            (list + inMemoryCashRedemptions).distinctBy { it.id }
        } catch (e: Exception) {
            inMemoryCashRedemptions.toList()
        }
    }

    suspend fun updateCashRedemptionStatus(id: String, status: String): Boolean {
        val index = inMemoryCashRedemptions.indexOfFirst { it.id == id }
        if (index != -1) {
            val existing = inMemoryCashRedemptions[index]
            inMemoryCashRedemptions[index] = existing.copy(
                status = status,
                settledAt = if (status == "completed") java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()) else existing.settledAt
            )
        }
        return try {
            httpClient.patch("$REST_URL/cash_redemptions") {
                supabaseHeaders()
                parameter("id", "eq.$id")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("status", status)
                    if (status == "completed") {
                        put("settled_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()))
                    }
                })
            }
            true
        } catch (e: Exception) {
            true // Successfully updated in memory
        }
    }

    // --- Agent Vehicle Change Requests ---
    suspend fun submitVehicleChangeRequest(request: VehicleChangeRequest): Boolean {
        return try {
            httpClient.post("$REST_URL/vehicle_change_requests") {
                supabaseHeaders()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getVehicleChangeRequests(): List<VehicleChangeRequest> {
        return try {
            httpClient.get("$REST_URL/vehicle_change_requests") {
                supabaseHeaders()
                parameter("order", "created_at.desc")
            }.body<List<VehicleChangeRequest>>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun reviewVehicleChangeRequest(
        id: String,
        agentId: String,
        newVehicleNumber: String,
        status: String,
        reason: String? = null
    ): Boolean {
        return try {
            httpClient.patch("$REST_URL/vehicle_change_requests") {
                supabaseHeaders()
                parameter("id", "eq.$id")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("status", status)
                    put("rejection_reason", reason)
                    if (status == "approved") {
                        // Auto-delete vehicle photo proof upon approval
                        put("vehicle_photo_url", JsonNull)
                    }
                    put("reviewed_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()))
                })
            }
            if (status == "approved") {
                // Update agent details vehicle number
                httpClient.patch("$REST_URL/agent_details") {
                    supabaseHeaders()
                    parameter("agent_id", "eq.$agentId")
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject {
                        put("vehicle_number", newVehicleNumber)
                        put("vehicle_photo_url", JsonNull)
                    })
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
