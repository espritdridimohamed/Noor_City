package tn.esprit.sansa.data.repositories

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tn.esprit.sansa.ui.screens.models.Reclamation

class FirebaseReclamationsRepository {
    private val database = FirebaseDatabase.getInstance()
    private val reclamationsRef = database.getReference("reclamations")

    fun getReclamations(): Flow<List<Reclamation>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reclamations = snapshot.children.mapNotNull { it.getValue(Reclamation::class.java) }
                trySend(reclamations)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        reclamationsRef.addValueEventListener(listener)
        awaitClose { reclamationsRef.removeEventListener(listener) }
    }

    fun addReclamation(reclamation: Reclamation, onComplete: (Boolean) -> Unit) {
        val id = reclamationsRef.push().key ?: return
        val newReclamation = reclamation.copy(id = id, date = System.currentTimeMillis())
        reclamationsRef.child(id).setValue(newReclamation)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun deleteReclamation(id: String, onComplete: (Boolean) -> Unit) {
        reclamationsRef.child(id).removeValue()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
}
