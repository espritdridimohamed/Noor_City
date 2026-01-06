package tn.esprit.sansa.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

/**
 * TFLiteObjectDetectionHelper — Centralise l'inférence IA temps réel pour Noor Vision.
 */
class TFLiteObjectDetectionHelper(
    val context: Context,
    val modelName: String = "mobilenet_v1.tflite",
    val scoreThreshold: Float = 0.5f,
    val maxResults: Int = 5
) {
    private var objectDetector: ObjectDetector? = null

    init {
        setupObjectDetector()
    }

    fun setupObjectDetector() {
        val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
            .setScoreThreshold(scoreThreshold)
            .setMaxResults(maxResults)

        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
            Log.d("TFLiteHelper", "✅ Modèle TFLite chargé avec succès : $modelName")
        } catch (e: Exception) {
            Log.e("TFLiteHelper", "❌ Erreur de chargement du modèle TFLite", e)
        }
    }

    /**
     * Analyse une image (Bitmap) et retourne une liste de détections.
     */
    fun detect(bitmap: Bitmap, rotation: Int = 0): List<Detection> {
        if (objectDetector == null) return emptyList()

        val imageProcessor = ImageProcessor.Builder()
            .add(Rot90Op(-rotation / 90))
            .build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))
        
        val detections = try {
            objectDetector?.detect(tensorImage) ?: emptyList()
        } catch (e: Exception) {
            Log.e("TFLiteHelper", "Inference error", e)
            emptyList()
        }

        if (detections.isNotEmpty()) {
            detections.forEach { 
                val label = it.categories.firstOrNull()?.label ?: "unknown"
                val score = it.categories.firstOrNull()?.score ?: 0f
                Log.d("TFLiteHelper", "🎯 Détecté: $label ($score)")
            }
        }
        
        return detections
    }

    fun close() {
        objectDetector?.close()
        objectDetector = null
    }
}
