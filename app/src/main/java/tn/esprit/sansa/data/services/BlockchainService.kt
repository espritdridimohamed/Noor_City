package tn.esprit.sansa.data.services

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import tn.esprit.sansa.data.models.BlockchainBlock
import tn.esprit.sansa.data.models.VideoCertificate
import tn.esprit.sansa.data.models.VerificationResult
import tn.esprit.sansa.data.models.VerificationStatus
import tn.esprit.sansa.utils.CryptoUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Service de gestion de la blockchain pour les certificats vidéo
 * Implémente une blockchain simplifiée stockée dans Firebase
 */
class BlockchainService {
    private val db = FirebaseFirestore.getInstance()
    private val certificatesCollection = db.collection("video_certificates")
    private val blocksCollection = db.collection("blockchain_blocks")

    companion object {
        private const val TAG = "BlockchainService"
        private const val GENESIS_BLOCK_NUMBER = 0L
    }

    /**
     * Initialise la blockchain avec le bloc genesis si nécessaire
     */
    suspend fun initializeBlockchain() {
        try {
            val genesisExists = blocksCollection
                .document(GENESIS_BLOCK_NUMBER.toString())
                .get()
                .await()
                .exists()

            if (!genesisExists) {
                val genesisBlock = BlockchainBlock.createGenesisBlock()
                blocksCollection
                    .document(GENESIS_BLOCK_NUMBER.toString())
                    .set(genesisBlock.toMap())
                    .await()
                Log.d(TAG, "Genesis block created")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing blockchain", e)
        }
    }

    /**
     * Crée un nouveau certificat vidéo et l'ajoute à la blockchain
     */
    suspend fun createCertificate(
        cameraId: String,
        cameraLocation: String,
        videoHash: String,
        metadata: Map<String, Any> = emptyMap()
    ): Result<VideoCertificate> {
        return try {
            val timestamp = System.currentTimeMillis()
            val certificateId = CryptoUtils.generateCertificateId(cameraId, timestamp)
            
            // Récupérer le dernier bloc
            val lastBlock = getLastBlock()
            val newBlockNumber = (lastBlock?.blockNumber ?: 0) + 1
            
            // Créer le certificat
            val certificate = VideoCertificate(
                id = certificateId,
                cameraId = cameraId,
                cameraLocation = cameraLocation,
                timestamp = timestamp,
                timestampFormatted = CryptoUtils.formatTimestamp(timestamp),
                videoHash = videoHash,
                blockNumber = newBlockNumber,
                previousBlockHash = lastBlock?.hash ?: "0",
                metadata = metadata,
                createdAt = timestamp,
                verified = true
            )

            // Créer le nouveau bloc
            // Générer un nonce unique pour ce bloc
            val nonce = CryptoUtils.generateNonce()

            // Créer le nouveau bloc
            val blockHash = CryptoUtils.calculateBlockHash(
                blockNumber = newBlockNumber,
                timestamp = timestamp,
                data = videoHash,
                previousHash = certificate.previousBlockHash,
                nonce = nonce
            )

            val newBlock = BlockchainBlock(
                blockNumber = newBlockNumber,
                timestamp = timestamp,
                data = certificate,
                previousHash = certificate.previousBlockHash,
                hash = blockHash,
                nonce = nonce
            )

            // Sauvegarder dans Firebase
            certificatesCollection.document(certificateId).set(certificate.toMap()).await()
            blocksCollection.document(newBlockNumber.toString()).set(newBlock.toMap()).await()

            Log.d(TAG, "Certificate created: $certificateId in block $newBlockNumber")
            Result.success(certificate)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating certificate", e)
            Result.failure(e)
        }
    }

    /**
     * Récupère le dernier bloc de la chaîne
     */
    private suspend fun getLastBlock(): BlockchainBlock? {
        return try {
            val snapshot = blocksCollection
                .orderBy("blockNumber", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val data = snapshot.documents[0].data
                if (data != null) {
                    BlockchainBlock.fromMap(data)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last block", e)
            null
        }
    }

    /**
     * Vérifie l'intégrité d'un certificat
     */
    suspend fun verifyCertificate(certificateId: String): VerificationResult {
        return try {
            // Récupérer le certificat
            val certDoc = certificatesCollection.document(certificateId).get().await()
            if (!certDoc.exists()) {
                return VerificationResult(
                    status = VerificationStatus.NOT_FOUND,
                    message = "Certificat non trouvé"
                )
            }

            val certData = certDoc.data
            if (certData == null) {
                return VerificationResult(
                    status = VerificationStatus.NOT_FOUND,
                    message = "Données du certificat invalides"
                )
            }

            val certificate = VideoCertificate.fromMap(certData)

            // Récupérer le bloc correspondant
            val blockDoc = blocksCollection.document(certificate.blockNumber.toString()).get().await()
            if (!blockDoc.exists()) {
                return VerificationResult(
                    status = VerificationStatus.CHAIN_BROKEN,
                    certificate = certificate,
                    message = "Bloc de la chaîne manquant",
                    chainIntegrity = false
                )
            }

            val blockData = blockDoc.data
            if (blockData == null) {
                return VerificationResult(
                    status = VerificationStatus.CHAIN_BROKEN,
                    certificate = certificate,
                    message = "Données du bloc invalides",
                    chainIntegrity = false
                )
            }

            val block = BlockchainBlock.fromMap(blockData)

            // Vérifier le hash du bloc
            val calculatedHash = CryptoUtils.calculateBlockHash(
                blockNumber = block.blockNumber,
                timestamp = block.timestamp,
                data = certificate.videoHash,
                previousHash = block.previousHash,
                nonce = block.nonce
            )

            if (!CryptoUtils.verifyHash(block.hash, calculatedHash)) {
                return VerificationResult(
                    status = VerificationStatus.TAMPERED,
                    certificate = certificate,
                    message = "Hash du bloc ne correspond pas",
                    chainIntegrity = false
                )
            }

            // Vérifier le lien avec le bloc précédent
            if (block.blockNumber > 0) {
                val prevBlockDoc = blocksCollection.document((block.blockNumber - 1).toString()).get().await()
                if (prevBlockDoc.exists()) {
                    val prevBlockData = prevBlockDoc.data
                    if (prevBlockData != null) {
                        val prevBlock = BlockchainBlock.fromMap(prevBlockData)
                        if (!CryptoUtils.verifyHash(block.previousHash, prevBlock.hash)) {
                            return VerificationResult(
                                status = VerificationStatus.CHAIN_BROKEN,
                                certificate = certificate,
                                message = "Chaîne de blocs brisée",
                                chainIntegrity = false
                            )
                        }
                    }
                }
            }

            // Tout est valide
            VerificationResult(
                status = VerificationStatus.AUTHENTIC,
                certificate = certificate,
                message = "Certificat authentique et vérifié",
                chainIntegrity = true,
                details = mapOf(
                    "blockNumber" to block.blockNumber,
                    "blockHash" to CryptoUtils.shortHash(block.hash),
                    "verifiedAt" to CryptoUtils.formatTimestamp(System.currentTimeMillis())
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying certificate", e)
            VerificationResult(
                status = VerificationStatus.NOT_FOUND,
                message = "Erreur lors de la vérification: ${e.message}"
            )
        }
    }

    /**
     * Récupère tous les certificats d'une caméra
     */
    fun getCertificatesByCamera(cameraId: String): Flow<List<VideoCertificate>> = callbackFlow {
        val listener = certificatesCollection
            .whereEqualTo("cameraId", cameraId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to certificates", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val certificates = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            VideoCertificate.fromMap(data)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing certificate", e)
                        null
                    }
                } ?: emptyList()

                trySend(certificates)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Récupère le nombre de certificats pour une caméra
     */
    suspend fun getCertificateCount(cameraId: String): Int {
        return try {
            val snapshot = certificatesCollection
                .whereEqualTo("cameraId", cameraId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting certificate count", e)
            0
        }
    }

    /**
     * Récupère le dernier certificat pour une caméra spécifique
     */
    suspend fun getLastCertificate(cameraId: String): VideoCertificate? {
        return try {
            val snapshot = certificatesCollection
                .whereEqualTo("cameraId", cameraId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val data = snapshot.documents[0].data
                data?.let { VideoCertificate.fromMap(it) }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last certificate", e)
            null
        }
    }




    /**
     * Vérifie l'intégrité de toute la blockchain
     */
    suspend fun verifyBlockchainIntegrity(): Boolean {
        return try {
            val snapshot = blocksCollection
                .orderBy("blockNumber", Query.Direction.ASCENDING)
                .get()
                .await()

            val blocks = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        BlockchainBlock.fromMap(data)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing block", e)
                    null
                }
            }

            for (i in 1 until blocks.size) {
                val currentBlock = blocks[i]
                val previousBlock = blocks[i - 1]

                // Vérifier le lien
                if (!CryptoUtils.verifyHash(currentBlock.previousHash, previousBlock.hash)) {
                    Log.e(TAG, "Chain broken at block ${currentBlock.blockNumber}")
                    return false
                }
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying blockchain integrity", e)
            false
        }
    }
}
