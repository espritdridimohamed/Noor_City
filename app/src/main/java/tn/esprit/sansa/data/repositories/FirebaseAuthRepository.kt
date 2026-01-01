package tn.esprit.sansa.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import tn.esprit.sansa.ui.screens.models.*

class FirebaseAuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")
    private val functions = FirebaseFunctions.getInstance()

    fun getCurrentUser() = auth.currentUser

    suspend fun getUserAccount(uid: String): UserAccount? {
        return try {
            val snapshot = database.child(uid).get().await()
            snapshot.getValue(UserAccount::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTechnicians(): List<UserAccount> {
        return try {
            val snapshot = FirebaseDatabase.getInstance().getReference("technicians").get().await()
            snapshot.children.mapNotNull { it.getValue(UserAccount::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getInvitations(): List<UserAccount> {
        return try {
            val snapshot = FirebaseDatabase.getInstance().getReference("invitations").get().await()
            snapshot.children.mapNotNull { it.getValue(UserAccount::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val account = getUserAccount(user.uid)
                if (account != null) {
                    AuthResult.Success(account)
                } else {
                    AuthResult.Failure("Compte utilisateur introuvable.")
                }
            } else {
                AuthResult.Failure("Échec de la connexion.")
            }
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Erreur de connexion.")
        }
    }

    suspend fun signUpCitizen(name: String, email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val newAccount = UserAccount(
                    uid = user.uid,
                    name = name,
                    email = email,
                    role = UserRole.CITIZEN
                )
                database.child(user.uid).setValue(newAccount).await()
                AuthResult.Success(newAccount)
            } else {
                AuthResult.Failure("Échec de la création du compte.")
            }
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Erreur lors de l'inscription.")
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun inviteTechnician(name: String, email: String, specialty: String): AuthResult {
        return try {
            val tempPassword = tn.esprit.sansa.data.services.EmailService.generateSecurePassword()
            
            val authResult = auth.createUserWithEmailAndPassword(email, tempPassword).await()
            val firebaseUser = authResult.user ?: return AuthResult.Failure("Échec de la création du compte Firebase")
            
            val newTech = UserAccount(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                role = UserRole.TECHNICIAN,
                specialty = specialty,
                isVerified = false,
                isFirstLogin = true,
                invitedAt = System.currentTimeMillis()
            )
            
            // Enregistrer dans /users et /invitations (PAS technicians encore)
            database.child(firebaseUser.uid).setValue(newTech).await()
            FirebaseDatabase.getInstance().getReference("invitations")
                .child(firebaseUser.uid).setValue(newTech).await()
            
            val emailResult = tn.esprit.sansa.data.services.EmailService.sendInvitationEmail(
                technicianName = name,
                technicianEmail = email,
                tempPassword = tempPassword
            )
            
            if (emailResult.isFailure) {
                return AuthResult.Failure("Compte créé mais email non envoyé: ${emailResult.exceptionOrNull()?.message}")
            }
            
            AuthResult.Success(newTech)
            AuthResult.Success(newTech)
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            AuthResult.Failure("Cet email est déjà utilisé par un compte existant.")
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Erreur lors de la création du technicien.")
        }
    }

    suspend fun completeOnboarding(uid: String, phoneNumber: String, zone: String): AuthResult {
        return try {
            val account = getUserAccount(uid) ?: return AuthResult.Failure("Compte introuvable")
            
            val updatedAccount = account.copy(
                phoneNumber = phoneNumber,
                workingZone = zone,
                isFirstLogin = false,
                activatedAt = System.currentTimeMillis(),
                isVerified = true
            )
            
            // 1. Mettre à jour /users
            database.child(uid).setValue(updatedAccount).await()
            
            // 2. Ajouter à /technicians
            FirebaseDatabase.getInstance().getReference("technicians")
                .child(uid).setValue(updatedAccount).await()
                
            // 3. Supprimer de /invitations
            FirebaseDatabase.getInstance().getReference("invitations")
                .child(uid).removeValue().await()
                
            AuthResult.Success(updatedAccount)
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Erreur lors de l'activation du profil.")
        }
    }

    suspend fun deleteAccount(): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            val uid = user.uid

            // 1. Supprimer de /users
            database.child(uid).removeValue().await()
            
            // 2. Supprimer de /technicians
            FirebaseDatabase.getInstance().getReference("technicians").child(uid).removeValue().await()
            
            // 3. Supprimer de /invitations (au cas où)
            FirebaseDatabase.getInstance().getReference("invitations").child(uid).removeValue().await()

            // 4. Supprimer le compte Auth
            user.delete().await()
            
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun reauthenticate(password: String): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updatePassword(newPassword: String): AuthResult {
        return try {
            val user = auth.currentUser ?: return AuthResult.Failure("Utilisateur non connecté")
            user.updatePassword(newPassword).await()
            AuthResult.Success(getUserAccount(user.uid)!!)
        } catch (e: Exception) {
             AuthResult.Failure(e.localizedMessage ?: "Erreur de mise à jour du mot de passe")
        }
    }

    suspend fun updateUserProfile(updates: Map<String, Any>): AuthResult {
        return try {
            val user = auth.currentUser ?: return AuthResult.Failure("Utilisateur non connecté")
            val uid = user.uid

            if (updates.containsKey("email")) {
                val newEmail = updates["email"] as String
                user.updateEmail(newEmail).await()
            }

            database.child(uid).updateChildren(updates).await()
            
            // Sync with technicians table if needed
            val role = (updates["role"] as? String) ?: getUserAccount(uid)?.role?.name
            if (role == UserRole.TECHNICIAN.name) {
                 FirebaseDatabase.getInstance().getReference("technicians").child(uid).updateChildren(updates).await()
            }
            
            val updated = getUserAccount(uid) ?: return AuthResult.Failure("Erreur de récupération")
            AuthResult.Success(updated)
        } catch (e: Exception) {
            AuthResult.Failure(e.localizedMessage ?: "Erreur de mise à jour")
        }
    }
}

sealed class AuthResult {
    data class Success(val user: UserAccount) : AuthResult()
    data class Failure(val message: String) : AuthResult()
}
