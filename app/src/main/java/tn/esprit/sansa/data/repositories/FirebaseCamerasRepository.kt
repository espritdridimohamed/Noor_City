package tn.esprit.sansa.data.repositories

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tn.esprit.sansa.ui.screens.models.Camera
import tn.esprit.sansa.ui.screens.models.CameraStatus
import tn.esprit.sansa.ui.screens.models.CameraType

class FirebaseCamerasRepository {
    private val database = FirebaseDatabase.getInstance("https://smartcity-35df0-default-rtdb.firebaseio.com/").getReference("cameras")

    fun getCameras(): Flow<List<Camera>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cameras = snapshot.children.mapNotNull { child ->
                    val id = child.key ?: return@mapNotNull null
                    val location = child.child("location").getValue(String::class.java) ?: "Inconnu"
                    val statusStr = child.child("status").getValue(String::class.java) ?: "ONLINE"
                    val typeStr = child.child("type").getValue(String::class.java) ?: "DOME"
                    val resolution = child.child("resolution").getValue(String::class.java) ?: "1080p"
                    val associatedStreetlight = child.child("associatedStreetlight").getValue(String::class.java) ?: ""
                    val nightVision = child.child("nightVision").getValue(Boolean::class.java) ?: true
                    val recordingEnabled = child.child("recordingEnabled").getValue(Boolean::class.java) ?: true
                    val motionDetection = child.child("motionDetection").getValue(Boolean::class.java) ?: true
                    val zone = child.child("zone").getValue(String::class.java) ?: ""
                    val installDate = child.child("installDate").getValue(String::class.java) ?: ""
                    val lastMaintenance = child.child("lastMaintenance").getValue(String::class.java) ?: ""
                    val streamUrl = child.child("streamUrl").getValue(String::class.java) ?: ""

                    Camera(
                        id = id,
                        location = location,
                        status = try { CameraStatus.valueOf(statusStr) } catch (e: Exception) { CameraStatus.ONLINE },
                        type = try { CameraType.valueOf(typeStr) } catch (e: Exception) { CameraType.DOME },
                        resolution = resolution,
                        associatedStreetlight = associatedStreetlight,
                        nightVision = nightVision,
                        recordingEnabled = recordingEnabled,
                        motionDetection = motionDetection,
                        zone = zone,
                        installDate = installDate,
                        lastMaintenance = lastMaintenance,
                        streamUrl = streamUrl
                    )
                }
                trySend(cameras)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    fun addCamera(camera: Camera, onComplete: (Boolean) -> Unit) {
        val cameraMap = mapOf(
            "location" to camera.location,
            "status" to camera.status.name,
            "type" to camera.type.name,
            "resolution" to camera.resolution,
            "associatedStreetlight" to camera.associatedStreetlight,
            "nightVision" to camera.nightVision,
            "recordingEnabled" to camera.recordingEnabled,
            "motionDetection" to camera.motionDetection,
            "zone" to camera.zone,
            "installDate" to camera.installDate,
            "lastMaintenance" to camera.lastMaintenance,
            "streamUrl" to camera.streamUrl
        )
        database.child(camera.id).setValue(cameraMap)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun deleteCamera(id: String) {
        database.child(id).removeValue()
    }
}
