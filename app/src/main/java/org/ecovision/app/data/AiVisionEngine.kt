package org.ecovision.app.data

import kotlinx.coroutines.delay

object AiVisionEngine {

    suspend fun analyzeWasteImage(imageUri: String?): AiDetectionResult {
        // Simulate real-time YOLOv8 / Vision AI model inference
        delay(1200)

        // Mock classifications based on typical recyclable items
        val candidates = listOf(
            AiDetectionResult(
                categoryId = "pet_plastic",
                categoryName = "PET Plastic Bottles",
                confidence = 96.4,
                isRecyclable = true,
                estimatedBasePrice = 14.0,
                diyRecommendations = listOf(
                    "Self-Watering Indoor Herb Planters",
                    "Desk Stationery & Pen Organizer",
                    "Hanging Backyard Bird Feeder",
                    "Vertical Garden Drip Irrigation Cups"
                ),
                imageUrl = imageUri ?: "https://images.unsplash.com/photo-1562077772-3ab1218634f2?w=600"
            ),
            AiDetectionResult(
                categoryId = "cardboard",
                categoryName = "Corrugated Packaging Cardboard",
                confidence = 94.8,
                isRecyclable = true,
                estimatedBasePrice = 10.0,
                diyRecommendations = listOf(
                    "Multi-Tier Desk Drawer Organizer",
                    "Decorative Jute-Wrapped Storage Bin",
                    "Modular Cat Play Tunnel",
                    "Under-Bed Shoe Compartment Divider"
                ),
                imageUrl = imageUri ?: "https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=600"
            ),
            AiDetectionResult(
                categoryId = "aluminum",
                categoryName = "Aluminum Cans & Scrap",
                confidence = 98.2,
                isRecyclable = true,
                estimatedBasePrice = 95.0,
                diyRecommendations = listOf(
                    "Upcycled Punched-Metal Candle Lantern",
                    "Garden Plant Identification Markers",
                    "Industrial Modern Desk Pen Caddy"
                ),
                imageUrl = imageUri ?: "https://images.unsplash.com/photo-1558885544-2defc62e252b?w=600"
            ),
            AiDetectionResult(
                categoryId = "e_waste",
                categoryName = "E-Waste Circuit Boards & Peripherals",
                confidence = 91.5,
                isRecyclable = true,
                estimatedBasePrice = 65.0,
                diyRecommendations = listOf(
                    "Cyberpunk Desk Clock with Quartz Movement",
                    "Recycled RAM Stick Keychain Pendants",
                    "Framed Circuit Board Wall Art"
                ),
                imageUrl = imageUri ?: "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=600"
            )
        )

        return candidates.random()
    }
}
