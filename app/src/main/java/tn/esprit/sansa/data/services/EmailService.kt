package tn.esprit.sansa.data.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

object EmailService {
    
    // Configuration EmailJS
    private const val SERVICE_ID = "service_uqztwlo"
    private const val TEMPLATE_ID = "template_jj7x41n"
    private const val PUBLIC_KEY = "isI0k5Uy66fNmz398"
    private const val EMAILJS_API_URL = "https://api.emailjs.com/api/v1.0/email/send"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Génère un mot de passe temporaire sécurisé
     * Format: 12 caractères avec majuscules, minuscules, chiffres et caractères spéciaux
     */
    fun generateSecurePassword(length: Int = 12): String {
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val special = "!@#$%^&*"
        val allChars = uppercase + lowercase + digits + special
        
        val random = SecureRandom()
        val password = StringBuilder()
        
        // Garantir au moins un caractère de chaque type
        password.append(uppercase[random.nextInt(uppercase.length)])
        password.append(lowercase[random.nextInt(lowercase.length)])
        password.append(digits[random.nextInt(digits.length)])
        password.append(special[random.nextInt(special.length)])
        
        // Remplir le reste aléatoirement
        repeat(length - 4) {
            password.append(allChars[random.nextInt(allChars.length)])
        }
        
        // Mélanger les caractères
        return password.toString().toList().shuffled(random).joinToString("")
    }
    
    /**
     * Envoie un email d'invitation via EmailJS
     * @param technicianName Nom complet du technicien
     * @param technicianEmail Email du technicien
     * @param tempPassword Mot de passe temporaire généré
     * @return true si l'email a été envoyé avec succès, false sinon
     */
    suspend fun sendInvitationEmail(
        technicianName: String,
        technicianEmail: String,
        tempPassword: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("EmailService", "=== DÉBUT ENVOI EMAIL ===")
            android.util.Log.d("EmailService", "Destinataire: $technicianEmail")
            android.util.Log.d("EmailService", "Nom: $technicianName")
            
            // Construire le payload JSON pour EmailJS
            val payload = JSONObject().apply {
                put("service_id", SERVICE_ID)
                put("template_id", TEMPLATE_ID)
                put("user_id", PUBLIC_KEY)
                put("template_params", JSONObject().apply {
                    put("to_email", technicianEmail)
                    put("technician_name", technicianName)
                    put("technician_email", technicianEmail)
                    put("temp_password", tempPassword)
                    put("app_name", "NoorCity")
                })
            }
            
            android.util.Log.d("EmailService", "Payload JSON créé")
            android.util.Log.d("EmailService", "URL: $EMAILJS_API_URL")
            
            // Créer la requête HTTP
            val requestBody = payload.toString()
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(EMAILJS_API_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            android.util.Log.d("EmailService", "Requête HTTP créée, envoi en cours...")
            
            // Exécuter la requête
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: "Pas de réponse"
                android.util.Log.d("EmailService", "Code de réponse: ${response.code}")
                android.util.Log.d("EmailService", "Corps de réponse: $responseBody")
                
                if (response.isSuccessful) {
                    android.util.Log.d("EmailService", "✅ Email envoyé avec succès!")
                    Result.success("Email envoyé avec succès à $technicianEmail")
                } else {
                    android.util.Log.e("EmailService", "❌ Échec: ${response.code} - $responseBody")
                    Result.failure(Exception("Échec de l'envoi: ${response.code} - $responseBody"))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("EmailService", "❌ ERREUR EXCEPTION: ${e.message}", e)
            Result.failure(Exception("Erreur réseau: ${e.localizedMessage ?: "Connexion impossible"}"))
        }
    }
}
