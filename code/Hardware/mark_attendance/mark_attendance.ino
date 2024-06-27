#include <WiFiManager.h>
#include <ESPAsyncWebServer.h>
#include <HTTPClient.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// Define pin constants
const int SERVER_ON_LED = 15;
const int SERVER_INACTIVE_LED = 26;
const int SERVER_RESET_BUTTON = 4;
const int SERVER_RESET_LED = 13;

// Define WiFi and server objects
WiFiManager wm;
AsyncWebServer server(80);
HTTPClient httpClient;

// Static IP configuration
IPAddress local_IP(192, 168, 1, 184); // Set your desired static IP address
IPAddress gateway(192, 168, 1, 1);    // Set your network gateway
IPAddress subnet(255, 255, 255, 0);   // Set your network subnet mask
IPAddress primaryDNS(8, 8, 8, 8);     // Optional: Set your primary DNS
IPAddress secondaryDNS(8, 8, 4, 4);   // Optional: Set your secondary DNS

// Initialize LCD
LiquidCrystal_I2C lcd(0x27, 16, 2); // Change the address 0x27 to match your LCD module

// Function to turn on server indication LEDs
void serverOnSetup() {
  digitalWrite(SERVER_ON_LED, HIGH);
  digitalWrite(SERVER_INACTIVE_LED, LOW);
}

// Function to turn off server indication LEDs
void serverOffSetup() {
  digitalWrite(SERVER_ON_LED, LOW);
  digitalWrite(SERVER_INACTIVE_LED, HIGH);
}

// Function to handle WiFi configuration
void setupWifiConfigurer() {
  serverOffSetup();
  if (!wm.autoConnect("attendance_manager")) {
    Serial.println("Failed to connect and hit timeout");
    serverOffSetup();
    ESP.restart();
  } else {
    Serial.print("Local IP: ");
    Serial.println(WiFi.localIP());
    serverOnSetup();
  }
}

// Function to handle button press for WiFi configuration
void handleResetButton() {
  int isResetButtonPressed = digitalRead(SERVER_RESET_BUTTON);
  if (isResetButtonPressed == HIGH) {
    digitalWrite(SERVER_INACTIVE_LED, LOW);
    digitalWrite(SERVER_RESET_LED, HIGH);
    setupWifiConfigurer();
    digitalWrite(SERVER_RESET_LED, LOW);
  }
}

void setup() {
  Serial.begin(115200);

  // Initialize pins
  pinMode(SERVER_ON_LED, OUTPUT);
  pinMode(SERVER_INACTIVE_LED, OUTPUT);
  pinMode(SERVER_RESET_LED, OUTPUT);
  pinMode(SERVER_RESET_BUTTON, INPUT);

  // Set initial LED states
  digitalWrite(SERVER_ON_LED, LOW);
  digitalWrite(SERVER_INACTIVE_LED, LOW);
  digitalWrite(SERVER_RESET_LED, LOW);

  // Initialize LCD
  lcd.init();
  lcd.backlight();

  // Start WiFi Access Point
  WiFi.softAP("Teal studio", "WoxncssOPs");
  Serial.print("AP IP address: ");
  Serial.println(WiFi.softAPIP());

  // Set static IP address
  if (!WiFi.config(local_IP, gateway, subnet, primaryDNS, secondaryDNS)) {
    Serial.println("STA Failed to configure");
  }

  // Auto-connect to WiFi
  setupWifiConfigurer();

  // Define status endpoint
  server.on("/status", HTTP_GET, [](AsyncWebServerRequest *request) {
    request->send(200, "application/json", "{\"status\":\"Server is running\"}");
  });

  // Define upload endpoint
  server.on("/upload", HTTP_POST, [](AsyncWebServerRequest *request) {
    if (request->hasParam("imageUpload", true)) {
      AsyncWebParameter* p = request->getParam("imageUpload", true);
      const String& filename = p->value();
      Serial.printf("Upload Start: %s\n", filename.c_str());

      if (WiFi.status() == WL_CONNECTED) {
        String serverPath = "http://192.168.43.49:8080/api/v1/update";
        httpClient.begin(serverPath);
        httpClient.addHeader("Content-Type", "application/octet-stream");
        int httpResponseCode = httpClient.POST((uint8_t*)filename.c_str(), filename.length());
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
    request->send(200, "text/plain", "Upload complete");
  });

  // Start the server
  server.begin();
}

void loop() {
  handleResetButton();
  delay(1000);

  // Display IP address on LCD
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("IP Address:");
  lcd.setCursor(0, 1);
  lcd.print(WiFi.localIP().toString());
}
