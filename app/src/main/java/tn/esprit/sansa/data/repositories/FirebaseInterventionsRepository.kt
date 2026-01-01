package tn.esprit.sansa.data.repositories

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tn.esprit.sansa.ui.screens.models.Intervention

class FirebaseInterventionsRepository {
    private val database = FirebaseDatabase.getInstance()
    private val interventionsRef = database.getReference("interventions")

    fun getInterventions(): Flow<List<Intervention>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val interventions = snapshot.children.mapNotNull { it.getValue(Intervention::class.java) }
                trySend(interventions)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        interventionsRef.addValueEventListener(listener)
        awaitClose { interventionsRef.removeEventListener(listener) }
    }

    fun addIntervention(intervention: Intervention, onComplete: (Boolean) -> Unit) {
        val id = interventionsRef.push().key ?: return
        val newIntervention = intervention.copy(id = id, date = System.currentTimeMillis())
        interventionsRef.child(id).setValue(newIntervention)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun deleteIntervention(id: String, onComplete: (Boolean) -> Unit) {
        interventionsRef.child(id).removeValue()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
}
