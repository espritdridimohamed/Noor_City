/**
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
