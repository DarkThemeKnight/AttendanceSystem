#include <WiFiManager.h>
#include <WiFi.h>
#include <ESPAsyncWebServer.h>
#include <AsyncTCP.h>
#include <HTTPClient.h>

// Server details
const char* server_ip = "192.168.43.49";
const int server_port = 8080;
const char* api_endpoint = "/api/v1/update";

// Access Point credentials
const char* ap_ssid = "Teal studio ssid";
const char* ap_password = "WoxncssOPs";

// Global objects
WiFiManager wm;
AsyncWebServer server(80);
HTTPClient httpClient;

void setupWifiConfigurer() {
  Serial.println("Setting up Wifi Configurer");
  bool res = wm.autoConnect("attendance_manager");
  if (!res) {
    Serial.println("Failed to connect");
    ESP.restart();
  } else {
    Serial.println("Connected...yeey :)");
  }
}

void handleFileUpload(AsyncWebServerRequest *request, String filename, size_t index, uint8_t *data, size_t len, bool final) {
  static String fileContent; // Holds the complete file content
  if (!index) {
    Serial.printf("Upload Start: %s\n", filename.c_str());
    fileContent = ""; // Clear the file content at the start of a new file upload
  }
  // Append the new chunk of data to fileContent
  for (size_t i = 0; i < len; i++) {
    fileContent += (char)data[i];
  }
  if (final) {
    Serial.printf("Upload End: %s, %u B\n", filename.c_str(), index + len);    
    // Forward the received file to the remote server
    if (WiFi.status() == WL_CONNECTED) {
      String serverPath = String("http://") + server_ip + ":" + String(server_port) + api_endpoint;
      httpClient.begin(serverPath);
      httpClient.addHeader("Content-Type", "application/octet-stream");
      int httpResponseCode = httpClient.POST((uint8_t*)fileContent.c_str(), fileContent.length());
      if (httpResponseCode > 0) {
        String response = httpClient.getString();
        Serial.printf("HTTP Response code: %d\n", httpResponseCode);
        Serial.println("Response: " + response);
      } else {
        Serial.printf("Error on HTTP request: %s\n", httpClient.errorToString(httpResponseCode).c_str());
      }
      httpClient.end();
    }
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(1, INPUT);

  // Start Wi-Fi as both client and access point
  WiFi.softAP(ap_ssid, ap_password);
  Serial.print("AP IP address: ");
  Serial.println(WiFi.softAPIP());

  // Configure and start the web server
  server.on("/", HTTP_GET, [](AsyncWebServerRequest *request) {
    request->send(200, "text/html", "<h1>Hello from ESP32</h1><form method='POST' action='/upload' enctype='multipart/form-data'><input type='file' name='file'><input type='submit' value='Upload'></form>");
  });

  server.onFileUpload(handleFileUpload);

  server.begin();
  Serial.println("Async Web Server started");

  setupWifiConfigurer();
}

void loop() {
  int val = digitalRead(1);
  if (val == 1) {
    setupWifiConfigurer();
  }
}
