# EcoVision Platform Architecture & Workflow

This document describes the complete end-to-end platform architecture and transaction lifecycle for **EcoVision**.

---

## 🗺️ System Flowchart

```mermaid
flowchart TD
    Start["🚀 Open EcoVision App"] --> AuthChoice{"New or Existing?"}

    %% Sign Up Branch
    AuthChoice -->|Sign Up| SignUp["Collect: Name, Username, Mobile, Password, Role"]
    SignUp --> RoleCheck{"Role == Agent?"}
    RoleCheck -->|Yes| AddVehicle["Enter Vehicle Number & Zone"]
    RoleCheck -->|No| RefCheck["Optional Referral Code ➔ Bonus Leaf Coins 🍃"]
    AddVehicle --> SaveUser["Save to Database ➔ Pending Admin Verify"]
    RefCheck --> SaveUser
    SaveUser --> AdminVerify

    %% Sign In Branch
    AuthChoice -->|Sign In| Identifier["Enter Username OR Mobile Number"]
    Identifier --> DetectInput{"Is 10-Digit Mobile?"}
    DetectInput -->|Yes| SendOTP["Send SMS OTP"]
    SendOTP --> VerifyOTP["Verify 6-Digit OTP"]
    VerifyOTP --> DashboardRouter{"Route by Role"}
    DetectInput -->|No| EnterPass["Enter Password"]
    EnterPass --> VerifyPass["Verify Credentials"]
    VerifyPass --> DashboardRouter

    %% Admin Subgraph
    subgraph ADMIN["🛡️ ADMIN DASHBOARD"]
        AdminVerify["Verify Users, Companies & Agents + Vehicle No."]
        AdminConfig["Set Live Market Rates, Min Pickup Limit, Agent Areas & Allowed Days"]
        AdminMonitor["Monitor Total Waste, Godown Stock & Agent Reports"]
        AdminVerify --> AdminConfig --> AdminMonitor
    end

    %% User Subgraph
    subgraph USER["🏠 USER DASHBOARD — Household / Seller"]
        ScanWaste["📷 Scan Waste via AI Camera"]
        AIResult["AI Identifies Material, Confidence & DIY/Upcycle Ideas"]
        LowConf{"Confidence < 70%?"}
        ManualEdit["User Confirms / Edits Category"]
        UserPath{"Choose Action"}
        CalcEst["Enter Est. Weight ➔ Show Est. Amount + Scale Disclaimer"]
        MinCheck{"Amount >= Min Limit?"}
        VirtualBin["Save to Virtual Bin & Accumulate"]
        ReqPickup["📍 Request Pickup + View Area Agent & Vehicle No."]
        ListUpcycle["List Upcycled Product in Marketplace"]
        Bargain["💬 Other Users Bargain Price & Buy"]
        RateAgent["⭐ Rate & Public Review Agent within 12 Hrs + Report Option"]
        LeafWallet["🍃 Leaf Coins Wallet: Earn by Weight & Referrals ➔ Redeem Cash/Voucher"]

        ScanWaste --> AIResult --> LowConf
        LowConf -->|Yes| ManualEdit --> UserPath
        LowConf -->|No| UserPath
        UserPath -->|Sell Raw Waste| CalcEst --> MinCheck
        MinCheck -->|No| VirtualBin --> MinCheck
        MinCheck -->|Yes| ReqPickup
        UserPath -->|Make DIY Product| ListUpcycle --> Bargain
    end

    %% Agent Subgraph
    subgraph AGENT["🚚 AGENT DASHBOARD — Pickup & Godown"]
        FilterJobs["View Requests in Assigned Area & Allowed Days Only"]
        AcceptJob["Accept Pickup Request — Atomic Lock"]
        SpotVerify["At Doorstep: User Matches Vehicle No. ➔ Agent Weighs on Scale"]
        UpdatePrice["Update Actual Weight & Final Price ➔ Syncs to User Dashboard"]
        PayUser["Pay Cash to User ➔ User Earns Leaf Coins 🍃"]
        GodownEntry["Move Waste to Godown & Click 'Accept to Godown Stock'"]

        FilterJobs --> AcceptJob --> SpotVerify --> UpdatePrice --> PayUser --> GodownEntry
    end

    %% Company Subgraph
    subgraph COMPANY["🏭 COMPANY DASHBOARD — Waste Buyers"]
        ViewStock["View ONLY Godown-Accepted Verified Stock"]
        BulkSelect["Select Multiple Users' Collected Waste by Category"]
        BulkBuy["🛒 Single Bulk Purchase Order & Settlement"]

        ViewStock --> BulkSelect --> BulkBuy
    end

    %% Inter-Dashboard Routing
    DashboardRouter -->|Admin| AdminVerify
    DashboardRouter -->|User| ScanWaste
    DashboardRouter -->|Agent| FilterJobs
    DashboardRouter -->|Company| ViewStock

    %% Cross-Role Workflows
    ReqPickup --> FilterJobs
    PayUser --> RateAgent
    PayUser --> LeafWallet
    GodownEntry -->|Verified Stock Visible| ViewStock
```

---

## 📋 Role Responsibilities

### 1. Platform Admin
- **Governance:** Platform-wide or role-specific Maintenance Mode control with custom downtime notices.
- **Verification Desk:** Review and verify Aadhaar, Business GST, and cross-match Agent Vehicle Photos with registered Vehicle Numbers.
- **Dynamic Pricing Engine:** Set baseline rates and dynamic weight-tier pricing slabs (e.g., 1–5 kg, 5–20 kg, 20+ kg) per recyclable category.

### 2. Households (Users)
- **AI Scanning:** Camera scan using YOLOv8 simulation to classify waste and provide DIY crafts suggestions.
- **Dual Selling Paths:**
  - **P2P Upcycle Store:** List handmade crafts with interactive price bargaining.
  - **Virtual Bin:** Accumulate raw recyclables until minimum threshold (e.g. ₹100 or 3 kg) is reached to request doorstep collection.
- **Doorstep Verification:** Verify agent vehicle number and live photo before handover.
- **Gamified Rewards:** Earn Leaf Coins proportional to kilograms recycled, redeemable for cash or eco-vouchers.

### 3. Pickup Agents
- **Zone & Schedule Filtering:** Agents see only pickups in their assigned geographic pincodes on approved days.
- **Spot Scale Weighing:** Enter certified physical scale weights at the doorstep to compute final cash payout.
- **Godown Inventory Gate:** Collected waste is moved to the warehouse and must be explicitly accepted into godown stock.

### 4. Buyer Companies (Recyclers)
- **Verified Procurement:** Browse godown-verified stock only (unverified or transit waste is hidden).
- **Category Lot Pooling:** Combine multiple users' collected waste into a single bulk purchase order.
- **Automated Manifests & ESG Impact:** Generates transaction manifests (`EV-MANIFEST-XXXX`) and calculates carbon/water savings.
