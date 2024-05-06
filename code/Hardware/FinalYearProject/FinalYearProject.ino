#include "esp_camera.h"
#include <Keypad.h>
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>
#include <ctype.h>
#include <WiFiManager.h>
#include <WiFi.h>
//wifi
#define WIFI_CONFIG_TRIGGER_PIN  15
int timeout = 200; //duration the portal is open for 

//keypad
const byte ROWS = 5; // 5 rows
const byte COLS = 4; // 4 columns
const char * url = "http://localhost:8080/api/v1/hardware/initialize";
const char * deviceId = "DefaultDeviceId";

char* specialKeys[] ={
    "F1",  "F2", "#", "*",
    "1",  "2", "3", "UP",
    "4",  "5", "6", "DOWN",
    "7",  "8", "9", "ESC",
    "LEFT",  "0", "RIGHT", "ENTER"  
};

char specialKeysID[] = {
    'A',  'B', '#', '*',
    '1',  '2', '3', 'C',
    '4',  '5', '6', 'D',
    '7',  '8', '9', 'E',
    'F',  '0', 'G', 'H'
};                    

char keys[ROWS][COLS] = {
    {specialKeysID[0],  specialKeysID[1], specialKeysID[2], specialKeysID[3]},
    {specialKeysID[4],  specialKeysID[5], specialKeysID[6], specialKeysID[7]},
    {specialKeysID[8],  specialKeysID[9], specialKeysID[10], specialKeysID[11]},
    {specialKeysID[12],  specialKeysID[13], specialKeysID[14], specialKeysID[15]},
    {specialKeysID[16],  specialKeysID[17], specialKeysID[18], specialKeysID[19]}
};

void clearRow(LiquidCrystal_I2C &lcd, int row);
void sendRequest(String code);

byte rowPins[ROWS] = {35, 32, 33, 25, 26};
byte colPins[COLS] = {27, 14, 12, 13};

//lcd
Keypad keypad = Keypad(makeKeymap(keys), rowPins, colPins, ROWS, COLS);
LiquidCrystal_I2C lcd(0x27, 16, 2);
String password = ""; // To store the password


void startCameraServer();
void setupLedFlash(int pin);

void setup() {
    //Wifi setup Config
    WiFi.mode(WIFI_STA); //Stationary mode
    Serial.begin(115200);
    Serial.setDebugOutput(true);
    pinMode(WIFI_CONFIG_TRIGGER_PIN, INPUT_PULLUP);
    lcd.init();                      // Initialize the LCD
    lcd.backlight();                 // Turn on the backlight
    lcd.setCursor(0, 0);
    lcd.print("Attendance Code");   
    lcd.setCursor(0, 1);

    WiFiManager wm;
    bool res = wm.autoConnect("Face recognition");
    if(!res){
      Serial.println("Failed to connect");
    }  
    while (WiFi.status() != WL_CONNECTED) {
      delay(500);
      Serial.print(".");
    }
    Serial.println("");
    Serial.println("WiFi connected");
    Serial.print("Camera Ready! Use 'http://");
    Serial.print(WiFi.localIP());
    Serial.println("' to connect");
}
void loop() {
    if(digitalRead(WIFI_CONFIG_TRIGGER_PIN) == 0){
      Serial.println("Button pressed");
      WiFiManager wifiManager;
      wifiManager.setConfigPortalTimeout(timeout);
      if(!wifiManager.startConfigPortal("Face recognition")){
          lcd.clear();
          lcd.print("Failed to");
          lcd.setCursor(0, 1);
          lcd.print("Connect to Wifi");
          delay(3000);
          lcd.clear();
          ESP.restart();
          delay(5000);
      }
      lcd.clear();
      lcd.print("Connected");
      lcd.setCursor(0, 1);
      lcd.print("Successfully");
      delay(3000);
      lcd.clear();
      lcd.print("Attendance Code");   
      lcd.setCursor(0, 1);
    } 
    char key = keypad.getKey();
    const char * keyName =  getKey(key);
    if (key) {
        if (strcmp(keyName, "ENTER") == 0) {
            // Call function to send request with the code
            sendRequest(password);
            password = ""; // Clear the password
            clearRow(lcd, 1); // Clear the password display
        } else if (strcmp(keyName, "ESC") == 0) {
            password = ""; // Clear the password
            clearRow(lcd, 1); // Clear the password display
        } else if (isdigit(key)) {
            password += key; // Append the digit to the password
            lcd.setCursor(0, 1);
            lcd.print("                "); // Clear the previous password display
            lcd.setCursor(0, 1);
            lcd.print(password); // Display the updated password
        }
    }
}

void clearRow(LiquidCrystal_I2C &lcd, int row) {
    lcd.setCursor(0, row);
    for (int i = 0; i < 16; i++) {
        lcd.print(" "); // Print spaces to overwrite existing content
    }
}

void sendRequest(String code) {
    // Send request with the code (Implement your logic here)
    Serial.print("Sending request with code: ");
    Serial.println(code);
}
const char* getKey(char k) {
    for (int i = 0; i < 20; i++) {
        if (specialKeysID[i] == k) return specialKeys[i];
    }
    return ""; // or whatever default value you want to return
}