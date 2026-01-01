package tn.esprit.sansa.data.repositories

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tn.esprit.sansa.ui.screens.models.LightingProgram

class FirebaseLightingRepository {
    private val database = FirebaseDatabase.getInstance().getReference("lighting_programs")

    fun getPrograms(): Flow<List<LightingProgram>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val programs = snapshot.children.mapNotNull { it.getValue(LightingProgram::class.java) }
                trySend(programs)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    fun addProgram(program: LightingProgram, onComplete: (Boolean) -> Unit) {
        val id = program.id.ifBlank { database.push().key ?: return }
        database.child(id).setValue(program.copy(id = id))
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteProgram(id: String, onComplete: (Boolean) -> Unit) {
        database.child(id).removeValue()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}
