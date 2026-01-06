/*
 * Freenove ESP32-WROVER CAM Stream Server avec Blockchain Certification
 * Configuration correcte pour le module Freenove ESP32-WROVER
 * 
 * NOUVELLES FONCTIONNALITÉS:
 * - Synchronisation NTP pour horodatage précis
 * - Overlay timestamp sur le stream vidéo
 * - Endpoint /metadata pour récupérer hash et timestamp
 * - Support timezone (Tunisie UTC+1)
 * 
 * URLs:
 * - Stream: http://<IP_ESP32>:81/stream
 * - Metadata: http://<IP_ESP32>:81/metadata
 */

#include "esp_camera.h"
#include <WiFi.h>
#include "esp_timer.h"
#include "img_converters.h"
#include "Arduino.h"
#include "fb_gfx.h"
#include "soc/soc.h"
#include "soc/rtc_cntl_reg.h"
#include "esp_http_server.h"
#include "time.h"
#include "mbedtls/md.h"
#include "CrashDetectionModel.h"

// --- VARIABLES AI CRASH DETECTION ---
uint8_t * baseline_frame = NULL;
const int ANALYSIS_WIDTH = 64;   // Basse résolution pour économiser la RAM
const int ANALYSIS_HEIGHT = 64;
bool isAccidentActive = false;
String alertStatus = "NORMAL";
int anomalyCounter = 0;
unsigned long lastAnalysisTime = 0;
const unsigned long ANALYSIS_INTERVAL = 3000; // Analyser toutes les 3 secondes
bool baselineCaptured = false;


// --- CONFIGURATION WIFI ---
const char* ssid = "Redmi 13C";
const char* password = "amine1234";

// --- CONFIGURATION NTP (Network Time Protocol) ---
const char* ntpServer = "pool.ntp.org";
const long gmtOffset_sec = 3600;        // UTC+1 pour la Tunisie
const int daylightOffset_sec = 0;       // Pas d'heure d'été en Tunisie

// --- CONFIGURATION CAMERA FREENOVE ESP32-WROVER ---
#define PWDN_GPIO_NUM     -1
#define RESET_GPIO_NUM    -1
#define XCLK_GPIO_NUM     21
#define SIOD_GPIO_NUM     26
#define SIOC_GPIO_NUM     27

#define Y9_GPIO_NUM       35
#define Y8_GPIO_NUM       34
#define Y7_GPIO_NUM       39
#define Y6_GPIO_NUM       36
#define Y5_GPIO_NUM       19
#define Y4_GPIO_NUM       18
#define Y3_GPIO_NUM       5
#define Y2_GPIO_NUM       4
#define VSYNC_GPIO_NUM    25
#define HREF_GPIO_NUM     23
#define PCLK_GPIO_NUM     22

#define PART_BOUNDARY "123456789000000000000987654321"
static const char* _STREAM_CONTENT_TYPE = "multipart/x-mixed-replace;boundary=" PART_BOUNDARY;
static const char* _STREAM_BOUNDARY = "\r\n--" PART_BOUNDARY "\r\n";
static const char* _STREAM_PART = "Content-Type: image/jpeg\r\nContent-Length: %u\r\n\r\n";

httpd_handle_t stream_httpd = NULL;

// Variables globales pour le timestamp
String currentTimestamp = "";
String currentHash = "";
unsigned long lastHashUpdate = 0;
const unsigned long HASH_UPDATE_INTERVAL = 1000; // Mettre à jour le hash chaque seconde

/**
 * Calcule le hash SHA-256 d'une chaîne
 */
String calculateSHA256(const String& data) {
    byte shaResult[32];
    mbedtls_md_context_t ctx;
    mbedtls_md_type_t md_type = MBEDTLS_MD_SHA256;

    mbedtls_md_init(&ctx);
    mbedtls_md_setup(&ctx, mbedtls_md_info_from_type(md_type), 0);
    mbedtls_md_starts(&ctx);
    mbedtls_md_update(&ctx, (const unsigned char*)data.c_str(), data.length());
    mbedtls_md_finish(&ctx, shaResult);
    mbedtls_md_free(&ctx);

    String hashString = "";
    for (int i = 0; i < 32; i++) {
        char str[3];
        sprintf(str, "%02x", (int)shaResult[i]);
        hashString += str;
    }
    return hashString;
}

/**
 * Récupère le timestamp actuel formaté
 */
String getFormattedTimestamp() {
    struct tm timeinfo;
    if (!getLocalTime(&timeinfo)) {
        return "N/A";
    }

    char timeString[64];
    strftime(timeString, sizeof(timeString), "%d/%m/%Y %H:%M:%S", &timeinfo);
    return String(timeString);
}

/**
 * Récupère le timestamp ISO 8601
 */
String getISOTimestamp() {
    struct tm timeinfo;
    if (!getLocalTime(&timeinfo)) {
        return "N/A";
    }

    char timeString[64];
    strftime(timeString, sizeof(timeString), "%Y-%m-%dT%H:%M:%SZ", &timeinfo);
    return String(timeString);
}

/**
 * Analyse l'image pour détecter un obstacle statique (Accident/Panne)
 */
void performCrashDetection() {
    if (millis() - lastAnalysisTime < ANALYSIS_INTERVAL) return;
    lastAnalysisTime = millis();

    Serial.println("🔍 AI: Analyzing road for anomalies...");

    // Capture d'une petite image grayscale pour l'analyse
    camera_fb_t * fb = esp_camera_fb_get();
    if (!fb) return;

    // Allocation de la baseline au premier passage
    if (!baselineCaptured) {
        if (!baseline_frame) baseline_frame = (uint8_t*)ps_malloc(ANALYSIS_WIDTH * ANALYSIS_HEIGHT);
        // Extraction du canal Y (Luminance) du format YUV422
        for (int i = 0; i < ANALYSIS_WIDTH * ANALYSIS_HEIGHT; i++) {
            baseline_frame[i] = fb->buf[i * 2]; // Le canal Y est sur les octets pairs
        }
        baselineCaptured = true;
        Serial.println("📸 AI: Baseline road captured (Y-Channel).");
        esp_camera_fb_return(fb);
        return;
    }

    // Comparaison avec la baseline
    int divergentPixels = 0;
    int totalPixels = ANALYSIS_WIDTH * ANALYSIS_HEIGHT;

    for (int i = 0; i < totalPixels; i++) {
        uint8_t currentY = fb->buf[i * 2];
        if (abs((int)currentY - (int)baseline_frame[i]) > CV_DIFF_THRESHOLD) {
            divergentPixels++;
        }
    }

    float divergenceRatio = (float)divergentPixels / totalPixels;
    
    if (divergenceRatio > CV_SENSITIVITY) {
        anomalyCounter++;
        Serial.printf("⚠️ AI Warning: Anomaly detected (%.1f%%). Persistence: %d/%d\n", 
                      divergenceRatio * 100, anomalyCounter, ACCIDENT_PERSISTENCE);
        
        if (anomalyCounter >= ACCIDENT_PERSISTENCE) {
            isAccidentActive = true;
            alertStatus = "ACCIDENT";
            Serial.println("🚨 AI ALERT: CRASH DETECTED!");
        }
    } else {
        if (anomalyCounter > 0) anomalyCounter--;
        if (anomalyCounter == 0) {
            isAccidentActive = false;
            alertStatus = "NORMAL";
        }
    }

    esp_camera_fb_return(fb);
}

/**
 * Handler pour le stream vidéo avec timestamp
 */
static esp_err_t stream_handler(httpd_req_t *req) {
    camera_fb_t * fb = NULL;
    esp_err_t res = ESP_OK;
    size_t _jpg_buf_len = 0;
    uint8_t * _jpg_buf = NULL;
    char * part_buf[64];

    res = httpd_resp_set_type(req, _STREAM_CONTENT_TYPE);
    if(res != ESP_OK) return res;

    while(true) {
        fb = esp_camera_fb_get();
        if (!fb) {
            Serial.println("Camera capture failed");
            res = ESP_FAIL;
        } else {
            // Mettre à jour le timestamp
            currentTimestamp = getFormattedTimestamp();
            
            // Mettre à jour le hash périodiquement
            if (millis() - lastHashUpdate > HASH_UPDATE_INTERVAL) {
                String dataToHash = currentTimestamp + String(fb->len);
                currentHash = calculateSHA256(dataToHash);
                lastHashUpdate = millis();
            }

            if(fb->format != PIXFORMAT_JPEG){
                bool jpeg_converted = frame2jpg(fb, 80, &_jpg_buf, &_jpg_buf_len);
                esp_camera_fb_return(fb);
                fb = NULL;
                if(!jpeg_converted){
                    Serial.println("JPEG compression failed");
                    res = ESP_FAIL;
                }
            } else {
                _jpg_buf_len = fb->len;
                _jpg_buf = fb->buf;
            }
        }
        if(res == ESP_OK){
            size_t hlen = snprintf((char *)part_buf, 64, _STREAM_PART, _jpg_buf_len);
            res = httpd_resp_send_chunk(req, (const char *)part_buf, hlen);
        }
        if(res == ESP_OK){
            res = httpd_resp_send_chunk(req, (const char *)_jpg_buf, _jpg_buf_len);
        }
        if(res == ESP_OK){
            res = httpd_resp_send_chunk(req, _STREAM_BOUNDARY, strlen(_STREAM_BOUNDARY));
        }
        if(fb){
            esp_camera_fb_return(fb);
            fb = NULL;
            _jpg_buf = NULL;
        } else if(_jpg_buf){
            free(_jpg_buf);
            _jpg_buf = NULL;
        }
        if(res != ESP_OK) break;
    }
    return res;
}

/**
 * Handler pour l'endpoint metadata (timestamp + hash)
 */
static esp_err_t metadata_handler(httpd_req_t *req) {
    httpd_resp_set_type(req, "application/json");
    httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");

    String json = "{";
    json += "\"timestamp\":\"" + currentTimestamp + "\",";
    json += "\"timestamp_iso\":\"" + getISOTimestamp() + "\",";
    json += "\"hash\":\"" + currentHash + "\",";
    json += "\"camera_id\":\"CAM_ESP32_001\",";
    json += "\"alert_status\":\"" + alertStatus + "\",";
    json += "\"is_accident\":" + String(isAccidentActive ? "true" : "false") + ",";
    json += "\"uptime\":" + String(millis()) + ",";
    json += "\"wifi_rssi\":" + String(WiFi.RSSI());
    json += "}";

    httpd_resp_send(req, json.c_str(), json.length());
    return ESP_OK;
}

/**
 * Handler pour la page d'accueil
 */
static esp_err_t index_handler(httpd_req_t *req) {
    httpd_resp_set_type(req, "text/html");
    
    String html = "<!DOCTYPE html><html><head>";
    html += "<meta charset='UTF-8'>";
    html += "<meta name='viewport' content='width=device-width, initial-scale=1.0'>";
    html += "<title>ESP32-CAM Blockchain</title>";
    html += "<style>";
    html += "body{font-family:Arial,sans-serif;margin:0;padding:20px;background:#1a1a2e;color:#fff;}";
    html += ".container{max-width:800px;margin:0 auto;}";
    html += "h1{color:#00d4ff;text-align:center;}";
    html += ".info{background:#16213e;padding:20px;border-radius:10px;margin:20px 0;}";
    html += ".timestamp{font-size:24px;color:#00d4ff;text-align:center;margin:20px 0;}";
    html += ".hash{font-family:monospace;font-size:12px;word-break:break-all;background:#0f3460;padding:10px;border-radius:5px;}";
    html += "img{width:100%;border-radius:10px;margin:20px 0;}";
    html += ".badge{display:inline-block;background:#00d4ff;color:#000;padding:5px 10px;border-radius:5px;font-weight:bold;margin:5px;}";
    html += "</style></head><body>";
    html += "<div class='container'>";
    html += "<h1>🎥 ESP32-CAM Blockchain Certification</h1>";
    html += "<div class='info'>";
    html += "<div class='timestamp' id='timestamp'>⏰ " + currentTimestamp + "</div>";
    html += "<p><span class='badge'>📹 Caméra</span> CAM_ESP32_001</p>";
    html += "<p><span class='badge'>🔐 Hash SHA-256</span></p>";
    html += "<div class='hash' id='hash'>" + currentHash + "</div>";
    html += "</div>";
    html += "<img src='/stream' />";
    html += "<div class='info'>";
    html += "<p>✅ Stream URL: http://" + WiFi.localIP().toString() + ":81/stream</p>";
    html += "<p>📊 Metadata API: http://" + WiFi.localIP().toString() + ":81/metadata</p>";
    html += "<p>📡 WiFi Signal: " + String(WiFi.RSSI()) + " dBm</p>";
    html += "</div></div>";
    html += "<script>";
    html += "setInterval(()=>{";
    html += "fetch('/metadata').then(r=>r.json()).then(d=>{";
    html += "document.getElementById('timestamp').innerHTML='⏰ '+d.timestamp;";
    html += "document.getElementById('hash').innerHTML=d.hash;";
    html += "});";
    html += "},1000);";
    html += "</script>";
    html += "</body></html>";

    httpd_resp_send(req, html.c_str(), html.length());
    return ESP_OK;
}

void startCameraServer() {
    httpd_config_t config = HTTPD_DEFAULT_CONFIG();
    config.server_port = 81;

    httpd_uri_t index_uri = {
        .uri       = "/",
        .method    = HTTP_GET,
        .handler   = index_handler,
        .user_ctx  = NULL
    };

    httpd_uri_t stream_uri = {
        .uri       = "/stream",
        .method    = HTTP_GET,
        .handler   = stream_handler,
        .user_ctx  = NULL
    };

    httpd_uri_t metadata_uri = {
        .uri       = "/metadata",
        .method    = HTTP_GET,
        .handler   = metadata_handler,
        .user_ctx  = NULL
    };
    
    Serial.printf("Starting web server on port: '%d'\n", config.server_port);
    if (httpd_start(&stream_httpd, &config) == ESP_OK) {
        httpd_register_uri_handler(stream_httpd, &index_uri);
        httpd_register_uri_handler(stream_httpd, &stream_uri);
        httpd_register_uri_handler(stream_httpd, &metadata_uri);
    }
}

void setup() {
    WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0);
    
    Serial.begin(115200);
    Serial.setDebugOutput(true);
    Serial.println();
    Serial.println("========================================");
    Serial.println("Freenove ESP32-WROVER CAM Starting...");
    Serial.println("Blockchain Certification System");
    Serial.println("========================================");

    camera_config_t config;
    config.ledc_channel = LEDC_CHANNEL_0;
    config.ledc_timer = LEDC_TIMER_0;
    config.pin_d0 = Y2_GPIO_NUM;
    config.pin_d1 = Y3_GPIO_NUM;
    config.pin_d2 = Y4_GPIO_NUM;
    config.pin_d3 = Y5_GPIO_NUM;
    config.pin_d4 = Y6_GPIO_NUM;
    config.pin_d5 = Y7_GPIO_NUM;
    config.pin_d6 = Y8_GPIO_NUM;
    config.pin_d7 = Y9_GPIO_NUM;
    config.pin_xclk = XCLK_GPIO_NUM;
    config.pin_pclk = PCLK_GPIO_NUM;
    config.pin_vsync = VSYNC_GPIO_NUM;
    config.pin_href = HREF_GPIO_NUM;
    config.pin_sscb_sda = SIOD_GPIO_NUM;
    config.pin_sscb_scl = SIOC_GPIO_NUM;
    config.pin_pwdn = PWDN_GPIO_NUM;
    config.pin_reset = RESET_GPIO_NUM;
    config.xclk_freq_hz = 20000000;
    config.pixel_format = PIXFORMAT_YUV422; // Permet l'analyse grayscale (Intelligence Visuelle)
    config.grab_mode = CAMERA_GRAB_LATEST;
    
    // Configuration pour ESP32-WROVER avec PSRAM
    if(psramFound()){
        Serial.println("✓ PSRAM found!");
        config.frame_size = FRAMESIZE_VGA;  // VGA suffisant pour YUV + Conversion
        config.jpeg_quality = 12;
        config.fb_count = 2;
        config.fb_location = CAMERA_FB_IN_PSRAM;
    } else {
        Serial.println("⚠ PSRAM not found, using small resolution");
        config.frame_size = FRAMESIZE_QVGA;
        config.jpeg_quality = 12;
        config.fb_count = 1;
        config.fb_location = CAMERA_FB_IN_DRAM;
    }

    // Initialisation de la caméra
    esp_err_t err = esp_camera_init(&config);
    if (err != ESP_OK) {
        Serial.printf("✗ Camera init failed with error 0x%x\n", err);
        Serial.println("Restarting in 3 seconds...");
        delay(3000);
        ESP.restart();
        return;
    }
    Serial.println("✓ Camera initialized successfully!");

    // Configuration des paramètres de la caméra
    sensor_t * s = esp_camera_sensor_get();
    if (s != NULL) {
        s->set_brightness(s, 0);
        s->set_contrast(s, 0);
        s->set_saturation(s, 0);
        s->set_special_effect(s, 0);
        s->set_whitebal(s, 1);
        s->set_awb_gain(s, 1);
        s->set_wb_mode(s, 0);
        s->set_exposure_ctrl(s, 1);
        s->set_aec2(s, 0);
        s->set_ae_level(s, 0);
        s->set_aec_value(s, 300);
        s->set_gain_ctrl(s, 1);
        s->set_agc_gain(s, 0);
        s->set_gainceiling(s, (gainceiling_t)0);
        s->set_bpc(s, 0);
        s->set_wpc(s, 1);
        s->set_raw_gma(s, 1);
        s->set_lenc(s, 1);
        s->set_hmirror(s, 0);
        s->set_vflip(s, 0);
        s->set_dcw(s, 1);
        s->set_colorbar(s, 0);
        Serial.println("✓ Camera settings configured");
    }

    // Connexion WiFi
    Serial.println("\n📡 Connecting to WiFi...");
    WiFi.begin(ssid, password);
    
    int wifi_retry = 0;
    while (WiFi.status() != WL_CONNECTED && wifi_retry < 20) {
        delay(500);
        Serial.print(".");
        wifi_retry++;
    }
    
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("\n✗ WiFi connection failed!");
        Serial.println("Restarting in 3 seconds...");
        delay(3000);
        ESP.restart();
        return;
    }
    
    Serial.println("\n✓ WiFi connected!");
    Serial.print("📍 IP Address: ");
    Serial.println(WiFi.localIP());
    Serial.print("📶 Signal Strength: ");
    Serial.print(WiFi.RSSI());
    Serial.println(" dBm");

    // Configuration NTP pour l'horodatage
    Serial.println("\n⏰ Configuring NTP time...");
    configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
    
    struct tm timeinfo;
    if (getLocalTime(&timeinfo)) {
        Serial.println("✓ NTP time synchronized!");
        Serial.print("📅 Current time: ");
        Serial.println(getFormattedTimestamp());
    } else {
        Serial.println("⚠ Failed to obtain time from NTP");
    }

    // Démarrage du serveur
    startCameraServer();

    Serial.println("\n========================================");
    Serial.println("🚀 SYSTEM READY!");
    Serial.println("========================================");
    Serial.print("🌐 Web Interface: http://");
    Serial.println(WiFi.localIP());
    Serial.print("📹 Stream URL: http://");
    Serial.print(WiFi.localIP());
    Serial.println(":81/stream");
    Serial.print("📊 Metadata API: http://");
    Serial.print(WiFi.localIP());
    Serial.println(":81/metadata");
    Serial.println("========================================\n");
}

void loop() {
    performCrashDetection();
    delay(10);
}
