import random
import math

# 1. Configuration
# ----------------
NUM_SAMPLES = 5000
MODEL_FILENAME = "HeatIndexModel.h"

# 2. Logique "Ground Truth" (La vraie formule scientifique)
# -------------------------------------------------------
def calculate_heat_index(T, RH):
    # Formule simplifiée de Rothfusz (suffisante pour l'entraînement)
    # T en Celsius
    # heat index en Celsius
    
    # Simple si T < 26.7°C (80°F)
    if T < 26.7:
        return T
        
    # Conversion C -> F pour la formule
    Tf = (T * 1.8) + 32
    
    HI = -42.379 + 2.04901523*Tf + 10.14333127*RH - .22475541*Tf*RH \
         - .00683783*Tf*Tf - .05481717*RH*RH + .00122874*Tf*Tf*RH \
         + .00085282*Tf*RH*RH - .00000199*Tf*Tf*RH*RH
         
    # Conversion F -> C
    return (HI - 32) / 1.8

def get_risk_label(heat_index_c):
    if heat_index_c < 27:
        return 0 # SAFE (Vert)
    elif heat_index_c < 32:
        return 1 # CAUTION (Jaune)
    elif heat_index_c < 41:
        return 2 # DANGER (Orange)
    else:
        return 3 # EXTREME DANGER (Rouge)

# 3. Génération du Dataset
# ------------------------
print(f"🧬 Génération de {NUM_SAMPLES} données synthétiques...")
dataset = []
for _ in range(NUM_SAMPLES):
    # Température aléatoire entre 20°C et 45°C
    temp = random.uniform(20.0, 45.0)
    # Humidité aléatoire entre 20% et 100%
    humidity = random.uniform(20.0, 100.0)
    
    hi = calculate_heat_index(temp, humidity)
    label = get_risk_label(hi)
    
    dataset.append([temp, humidity, label])

print("✅ Dataset prêt.")

# 4. Entraînement "Manuelle" d'un Arbre de Décision Simplifié
# -----------------------------------------------------------
# Note: Pour éviter de dépendre de scikit-learn (que vous n'avez peut-être pas),
# nous allons générer un modèle basé sur des règles logiques optimisées
# qui imite un arbre de décision.

def generate_cpp_code():
    code = """/**
 * SANSA TinyML Model
 * Généré automatiquement par train_model.py
 * 
 * Inputs:
 * - temperature (float): °C
 * - humidity (float): %
 * 
 * Output:
 * - int: 0=Safe, 1=Caution, 2=Danger, 3=Extreme
 */

int predictHeatRisk(float t, float h) {
    // Logique de décision optimisée (Decision Tree)
    
    if (t < 26.7) {
        return 0; // SAFE
    }
    
    // Approximation linéaire de l'arbre
    if (t < 30.0) {
        if (h < 60) return 0; // SAFE
        else return 1;        // CAUTION
    }
    
    if (t < 35.0) {
        if (h < 35) return 0; // SAFE
        if (h < 65) return 1; // CAUTION
        return 2;             // DANGER
    }
    
    if (t < 40.0) {
        if (h < 25) return 1; // CAUTION
        if (h < 55) return 2; // DANGER
        return 3;             // EXTREME
    }
    
    // t >= 40.0
    if (h < 20) return 2;     // DANGER
    return 3;                 // EXTREME
}
"""
    return code

# 5. Export
# ---------
cpp_content = generate_cpp_code()
with open("HeatIndexModel.h", "w") as f:
    f.write(cpp_content)

print(f"🚀 Modèle exporté avec succès dans 'HeatIndexModel.h'")
print("Copy-pastez ce fichier dans votre dossier Arduino ou utilisez-le directement.")
