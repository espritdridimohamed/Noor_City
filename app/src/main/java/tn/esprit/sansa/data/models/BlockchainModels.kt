package tn.esprit.sansa.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color

/**
 * Modèle de certificat vidéo pour la blockchain
 * Représente une preuve cryptographique d'un enregistrement vidéo
 */
data class VideoCertificate(
    val id: String = "",
    val cameraId: String = "",
    val cameraLocation: String = "",
    val timestamp: Long = 0L,
    val timestampFormatted: String = "",
    val videoHash: String = "",
    val blockNumber: Long = 0L,
    val previousBlockHash: String = "",
    val metadata: Map<String, Any> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val verified: Boolean = false
) {
    /**
     * Convertit le certificat en Map pour Firebase
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "cameraId" to cameraId,
            "cameraLocation" to cameraLocation,
            "timestamp" to timestamp,
            "timestampFormatted" to timestampFormatted,
            "videoHash" to videoHash,
            "blockNumber" to blockNumber,
            "previousBlockHash" to previousBlockHash,
            "metadata" to metadata,
            "createdAt" to createdAt,
            "verified" to verified
        )
    }

    companion object {
        /**
         * Crée un certificat depuis une Map Firebase
         */
        fun fromMap(map: Map<String, Any>): VideoCertificate {
            return VideoCertificate(
                id = map["id"] as? String ?: "",
                cameraId = map["cameraId"] as? String ?: "",
                cameraLocation = map["cameraLocation"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
                timestampFormatted = map["timestampFormatted"] as? String ?: "",
                videoHash = map["videoHash"] as? String ?: "",
                blockNumber = (map["blockNumber"] as? Number)?.toLong() ?: 0L,
                previousBlockHash = map["previousBlockHash"] as? String ?: "",
                metadata = map["metadata"] as? Map<String, Any> ?: emptyMap(),
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
                verified = map["verified"] as? Boolean ?: false
            )
        }
    }
}

/**
 * Bloc de la blockchain
 * Contient un certificat et est lié au bloc précédent
 */
data class BlockchainBlock(
    val blockNumber: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val data: VideoCertificate? = null,
    val previousHash: String = "",
    val hash: String = "",
    val nonce: Long = 0L
) {
    /**
     * Convertit le bloc en Map pour Firebase
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "blockNumber" to blockNumber,
            "timestamp" to timestamp,
            "data" to (data?.toMap() ?: emptyMap<String, Any>()),
            "previousHash" to previousHash,
            "hash" to hash,
            "nonce" to nonce
        )
    }

    companion object {
        /**
         * Crée un bloc depuis une Map Firebase
         */
        fun fromMap(map: Map<String, Any>): BlockchainBlock {
            val dataMap = map["data"] as? Map<String, Any>
            return BlockchainBlock(
                blockNumber = (map["blockNumber"] as? Number)?.toLong() ?: 0L,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
                data = dataMap?.let { VideoCertificate.fromMap(it) },
                previousHash = map["previousHash"] as? String ?: "",
                hash = map["hash"] as? String ?: "",
                nonce = (map["nonce"] as? Number)?.toLong() ?: 0L
            )
        }

        /**
         * Crée le bloc genesis (premier bloc de la chaîne)
         */
        fun createGenesisBlock(): BlockchainBlock {
            return BlockchainBlock(
                blockNumber = 0,
                timestamp = System.currentTimeMillis(),
                data = null,
                previousHash = "0",
                hash = "0000000000000000000000000000000000000000000000000000000000000000",
                nonce = 0
            )
        }
    }
}

/**
 * Résultat de vérification d'intégrité
 */
enum class VerificationStatus {
    AUTHENTIC,      // Vidéo authentique, hash correspond
    TAMPERED,       // Vidéo modifiée, hash ne correspond pas
    NOT_FOUND,      // Certificat non trouvé
    CHAIN_BROKEN,   // Chaîne de blocs corrompue
    PENDING         // Vérification en cours
}

/**
 * Résultat détaillé de vérification
 */
data class VerificationResult(
    val status: VerificationStatus = VerificationStatus.PENDING,
    val certificate: VideoCertificate? = null,
    val message: String = "",
    val verifiedAt: Long = System.currentTimeMillis(),
    val chainIntegrity: Boolean = true,
    val details: Map<String, Any> = emptyMap()
) {
    val isValid: Boolean
        get() = status == VerificationStatus.AUTHENTIC && chainIntegrity

    val statusColor: Color
        get() = when (status) {
            VerificationStatus.AUTHENTIC -> Color(0xFF10B981)
            VerificationStatus.TAMPERED -> Color(0xFFEF4444)
            VerificationStatus.NOT_FOUND -> Color(0xFFF59E0B)
            VerificationStatus.CHAIN_BROKEN -> Color(0xFFDC2626)
            VerificationStatus.PENDING -> Color(0xFF6B7280)
        }

    val statusIcon: androidx.compose.ui.graphics.vector.ImageVector
        get() = when (status) {
            VerificationStatus.AUTHENTIC -> Icons.Default.VerifiedUser
            VerificationStatus.TAMPERED -> Icons.Default.Warning
            VerificationStatus.NOT_FOUND -> Icons.Default.SearchOff
            VerificationStatus.CHAIN_BROKEN -> Icons.Default.BrokenImage
            VerificationStatus.PENDING -> Icons.Default.HourglassEmpty
        }
}
