/*
 * ESP32 - Temperature & Humidity Node (Sansa Project)
 * Transmet les données d'un capteur DHT11/DHT22 vers Firebase en temps réel.
 * 
 * Bibliothèques requises (Arduino IDE) :
 * 1. DHT sensor library by Adafruit
 * 2. Firebase ESP32 Client by Mobizt
 */

#include <WiFi.h>
#include <FirebaseESP32.h>
#include "DHT.h"

// --- CONFIGURATION SENSOR ---
#define DHTPIN 14          // Broche DATA du DHT (GPIO 14)
#define DHTTYPE DHT11     // Changez en DHT22 si vous utilisez un DHT22
DHT dht(DHTPIN, DHTTYPE);

// --- CONFIGURATION WIFI ---
const char* WIFI_SSID = "VOTRE_SSID";
const char* WIFI_PASSWORD = "VOTRE_PASSWORD";

// --- CONFIGURATION FIREBASE ---
const char* FIREBASE_HOST = "smartcity-35df0-default-rtdb.firebaseio.com";
const char* FIREBASE_AUTH = "VOTRE_DATABASE_SECRET_OU_TOKEN"; 

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

String SENSOR_ID; // Sera généré automatiquement à partir de l'adresse MAC
unsigned long sendDataPrevMillis = 0;

void setup() {
  Serial.begin(115200);

  // Utilisation du pull-up interne
  pinMode(DHTPIN, INPUT_PULLUP); 
  dht.begin();

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  Serial.println("\nConnected!");

  // Génération de l'ID Unique à partir de la MAC (ex: TEMP_A1B2C3)
  String mac = WiFi.macAddress();
  mac.replace(":", "");
  SENSOR_ID = "TEMP_" + mac.substring(6); // Utilise les 6 derniers caractères pour plus de clarté
  
  Serial.println("------------------------------------");
  Serial.print("VOTRE ID CAPTEUR UNIQUE : ");
  Serial.println(SENSOR_ID);
  Serial.println("------------------------------------");

  config.host = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
}

void loop() {
  if (millis() - sendDataPrevMillis > 10000 || sendDataPrevMillis == 0) {
    sendDataPrevMillis = millis();

    float t = dht.readTemperature();
    float h = dht.readHumidity();

    if (isnan(t) || isnan(h)) {
      Serial.println("Failed to read from DHT sensor!");
      return;
    }

    Serial.printf("ID: %s | Temp: %.1f°C\n", SENSOR_ID.c_str(), t);

    // On envoie UNIQUEMENT les données dynamiques
    // Le nom (streetlightName) est géré dans l'App Sansa
    String path = "/sensors/" + SENSOR_ID;
    
    FirebaseJson updateData;
    updateData.add("value", String(t, 1));
    updateData.add("status", "ACTIVE");
    updateData.add("battery", 98); // Simulation niveau batterie

    // On utilise .updateNode pour ne pas effacer le nom ou le type mis dans l'App
    if (Firebase.updateNode(fbdo, path, updateData)) {
      Serial.println("Envoi réussi vers Firebase.");
    } else {
      Serial.println("Erreur Firebase: " + fbdo.errorReason());
    }
  }
}
