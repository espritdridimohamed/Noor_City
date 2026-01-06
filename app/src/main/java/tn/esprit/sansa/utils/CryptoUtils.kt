package tn.esprit.sansa.utils

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilitaires cryptographiques pour la blockchain
 * Gère le hashing SHA-256 et la vérification d'intégrité
 */
object CryptoUtils {

    /**
     * Calcule le hash SHA-256 d'une chaîne de caractères
     * @param input Texte à hasher
     * @return Hash hexadécimal
     */
    fun calculateSHA256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Calcule le hash SHA-256 d'un tableau de bytes
     * @param bytes Données à hasher
     * @return Hash hexadécimal
     */
    fun calculateSHA256(bytes: ByteArray): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Calcule le hash d'un bloc blockchain
     * Combine blockNumber + timestamp + data + previousHash + nonce
     */
    fun calculateBlockHash(
        blockNumber: Long,
        timestamp: Long,
        data: String,
        previousHash: String,
        nonce: Long = 0
    ): String {
        val input = "$blockNumber$timestamp$data$previousHash$nonce"
        return calculateSHA256(input)
    }

    /**
     * Calcule le hash d'un certificat vidéo
     * Combine tous les champs importants
     */
    fun calculateCertificateHash(
        cameraId: String,
        timestamp: Long,
        location: String,
        metadata: String = ""
    ): String {
        val input = "$cameraId$timestamp$location$metadata"
        return calculateSHA256(input)
    }

    /**
     * Vérifie si un hash correspond à un pattern (pour proof of work)
     * @param hash Hash à vérifier
     * @param difficulty Nombre de zéros requis au début
     */
    fun isValidHash(hash: String, difficulty: Int = 4): Boolean {
        val prefix = "0".repeat(difficulty)
        return hash.startsWith(prefix)
    }

    /**
     * Génère un timestamp formaté pour affichage
     */
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Génère un timestamp ISO 8601
     */
    fun formatTimestampISO(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }

    /**
     * Génère un ID unique pour un certificat
     */
    fun generateCertificateId(cameraId: String, timestamp: Long): String {
        val input = "$cameraId-$timestamp-${UUID.randomUUID()}"
        return calculateSHA256(input).substring(0, 16)
    }

    /**
     * Vérifie l'intégrité d'un hash
     * @param original Hash original
     * @param toVerify Hash à vérifier
     */
    fun verifyHash(original: String, toVerify: String): Boolean {
        return original.equals(toVerify, ignoreCase = true)
    }

    /**
     * Génère un nonce pour le proof of work
     * (Version simplifiée, pas de mining réel)
     */
    fun generateNonce(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Crée une signature numérique simple
     * Combine hash + timestamp pour créer une signature unique
     */
    fun createDigitalSignature(hash: String, timestamp: Long): String {
        return calculateSHA256("$hash-$timestamp-SANSA-BLOCKCHAIN")
    }

    /**
     * Vérifie une signature numérique
     */
    fun verifyDigitalSignature(
        hash: String,
        timestamp: Long,
        signature: String
    ): Boolean {
        val expectedSignature = createDigitalSignature(hash, timestamp)
        return verifyHash(expectedSignature, signature)
    }

    /**
     * Encode des données en Base64
     */
    fun encodeBase64(data: String): String {
        return try {
            android.util.Base64.encodeToString(
                data.toByteArray(Charsets.UTF_8),
                android.util.Base64.NO_WRAP
            )
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Décode des données Base64
     */
    fun decodeBase64(encoded: String): String {
        return try {
            String(
                android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP),
                Charsets.UTF_8
            )
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Génère un résumé court d'un hash (pour affichage)
     */
    fun shortHash(hash: String, length: Int = 8): String {
        return if (hash.length > length) {
            "${hash.substring(0, length)}...${hash.substring(hash.length - length)}"
        } else {
            hash
        }
    }

    /**
     * Valide le format d'un hash SHA-256
     */
    fun isValidSHA256Format(hash: String): Boolean {
        return hash.matches(Regex("^[a-fA-F0-9]{64}$"))
    }
}
