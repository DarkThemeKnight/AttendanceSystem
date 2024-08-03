#include <WiFiManager.h>
#include <ESPAsyncWebServer.h>
#include <HTTPClient.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// Define pin constants
const int SERVER_ON_LED = 15;
const int SERVER_INACTIVE_LED = 26;
// const int SERVER_RESET_BUTTON = 4;
const int SERVER_RESET_LED = 13;

const char * password = "141066123";
// Define WiFi and server objects
WiFiManager wm;
AsyncWebServer server(80);
HTTPClient httpClient;

// Static IP configuration
IPAddress local_IP(192, 168, 43, 184); // Set your desired static IP address
IPAddress gateway(192, 168, 43, 200);    // Set your network gateway
IPAddress subnet(255, 255, 255, 0);   // Set your network subnet mask
IPAddress primaryDNS(8, 8, 8, 8);     // Optional: Set your primary DNS
IPAddress secondaryDNS(8, 8, 4, 4);   // Optional: Set your secondary DNS

// Initialize LCD
LiquidCrystal_I2C lcd(0x27, 16, 2); // Change the address 0x27 to match your LCD module
// Function to turn on server indication LEDs
void serverOnSetup() {
  digitalWrite(SERVER_ON_LED, HIGH);
  digitalWrite(SERVER_INACTIVE_LED, LOW);
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Server Running");
}
// Function to turn off server indication LEDs
void serverOffSetup() {
  digitalWrite(SERVER_ON_LED, LOW);
  digitalWrite(SERVER_INACTIVE_LED, HIGH);
  lcd.print("Rebooting");
}

// Function to handle WiFi configuration
void setupWifiConfigurer() {
  serverOffSetup();
  if (!wm.autoConnect("attendance_manager")) {
      // Display IP address on LCD
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Timeout!!!");
      // Display IP address on LCD
    lcd.setCursor(0, 1);
    lcd.print("Restarting");
    serverOffSetup();
    delay(1000);
    ESP.restart();
  } else {
    serverOnSetup();
  }
}

// Function to handle button press for WiFi configuration
// void handleResetButton() {
//   int isResetButtonPressed = digitalRead(SERVER_RESET_BUTTON);
//   if (isResetButtonPressed == HIGH) {
//       // Display IP address on LCD
//     lcd.clear();
//     lcd.setCursor(0, 0);
//     lcd.print("Rebooting.......");
//     digitalWrite(SERVER_INACTIVE_LED, LOW);
//     digitalWrite(SERVER_RESET_LED, HIGH);
//     delay(1000);
//     setupWifiConfigurer();
//     digitalWrite(SERVER_RESET_LED, LOW);
//   }
// }

void setup() {
  Serial.begin(115200);
  // Initialize pins
  pinMode(SERVER_ON_LED, OUTPUT);
  pinMode(SERVER_INACTIVE_LED, OUTPUT);
  pinMode(SERVER_RESET_LED, OUTPUT);
  // pinMode(SERVER_RESET_BUTTON, INPUT);  
  // Set initial LED states
  digitalWrite(SERVER_ON_LED, LOW);
  digitalWrite(SERVER_INACTIVE_LED, LOW);
  digitalWrite(SERVER_RESET_LED, LOW);
  // Initialize LCD
  lcd.init();
  lcd.backlight();
  // Start WiFi Access Point
  WiFi.softAP("Teal studio", "jojo");
  // Set static IP address
  WiFi.config(local_IP, gateway, subnet, primaryDNS, secondaryDNS);
  // Auto-connect to WiFi
  setupWifiConfigurer();  
  // Define status endpoint
  server.on("/status", HTTP_GET, [](AsyncWebServerRequest *request) {
    request->send(200, "application/json", "{\"status\":\"Server is running\"}");
  });
  
  server.on("/upload", HTTP_POST, [](AsyncWebServerRequest *request) {
    Serial.println("Received request to upload");

    if (request->hasParam("attendanceCode") && request->hasParam("file", true, true)) {
        String attendanceCode = request->getParam("attendanceCode")->value();
        AsyncWebParameter* p = request->getParam("file", true, true);
        size_t fileSize = p->size();
        const uint8_t* data = (uint8_t*)p->value().c_str();

        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String payload = "--" + boundary + "\r\n";
        payload += "Content-Disposition: form-data; name=\"attendanceCode\"\r\n\r\n";
        payload += attendanceCode + "\r\n";
        payload += "--" + boundary + "\r\n";
        payload += "Content-Disposition: form-data; name=\"image\"; filename=\"upload.jpg\"\r\n";
        payload += "Content-Type: application/octet-stream\r\n\r\n";

        int payloadLength = payload.length();
        String endBoundary = "\r\n--" + boundary + "--\r\n";
        int endBoundaryLength = endBoundary.length();
        int contentLength = payloadLength + fileSize + endBoundaryLength;

        uint8_t* postData = new uint8_t[contentLength];
        memcpy(postData, payload.c_str(), payloadLength);
        memcpy(postData + payloadLength, data, fileSize);
        memcpy(postData + payloadLength + fileSize, endBoundary.c_str(), endBoundaryLength);

        httpClient.begin("http://192.168.43.49:8080/api/v1/students/update");
        httpClient.addHeader("Content-Type", "multipart/form-data; boundary=" + boundary);

        Serial.printf("Sending POST request with content length %d\n", contentLength);

        int httpResponseCode = httpClient.POST(postData, contentLength);

        if (httpResponseCode == 200) {
            String response = httpClient.getString();
            Serial.printf("HTTP Response code: %d\n", httpResponseCode);
            Serial.print("Response: ");
            Serial.println(response);
            request->send(200, "text/plain", "Upload successful");
        } else {
            Serial.print("Error on HTTP request: ");
            Serial.println(httpClient.errorToString(httpResponseCode).c_str());
            request->send(httpResponseCode, "text/plain", "Upload failed");
        }

        httpClient.end();
        delete[] postData;
    } else {
        request->send(400, "text/plain", "Invalid parameters");
    }
});

 

  server.begin();
  Serial.println("Setup completed...");
}

void loop() {
  // handleResetButton();
}