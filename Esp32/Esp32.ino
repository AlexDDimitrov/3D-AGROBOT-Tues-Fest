#include <WiFi.h>

const char* SSID     = "    ";
const char* PASSWORD = "    ";
const int   PORT     = 8080;

WiFiServer server(PORT);
WiFiClient client;

#define RXD2 16
#define TXD2 17

void setup() {
  Serial.begin(115200);
  Serial2.begin(115200, SERIAL_8N1, RXD2, TXD2);

  WiFi.begin(SSID, PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nConnected to WiFi");
  Serial.print("IP: "); Serial.println(WiFi.localIP());
  server.begin();
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) return;

  if (!client || !client.connected()) {
    WiFiClient newClient = server.available();
    if (newClient) {
      client = newClient;
      Serial.println("Pi Connected");
    }
  }

  // FROM PI -> TO ARDUINO
  if (client && client.available()) {
    String cmd = client.readStringUntil('\n');
    cmd.trim();
    if (cmd.length() > 0) {
      Serial.print("Pi Command: "); 
      Serial.println(cmd);
      Serial2.println(cmd); // Sends to Arduino
    }
  }

  // FROM ARDUINO -> TO PI
  if (Serial2.available()) {
    String ardData = Serial2.readStringUntil('\n');
    ardData.trim();
    if (ardData.length() > 0) {
      Serial.print("Arduino says: "); 
      Serial.println(ardData);
      if (client && client.connected()) {
        client.println(ardData); // Sends to Pi
      }
    }
  }
}
