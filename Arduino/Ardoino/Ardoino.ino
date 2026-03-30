#include <Wire.h>

#define MPU_ADDR      0x68
#define PWR_MGMT_1    0x6B
#define ACCEL_CONFIG  0x1C
#define ACCEL_YOUT_H  0x3D

float targetDistance = 0;

#define ALPHA 0.2
float AcselY_Filter = 0;
double velocityY      = 0.0;
double distanceY      = 0.0;
float biasY = 0.0;
unsigned long t_now = 0;
unsigned long t_then = 0;

unsigned long moveStartTime = 0;

// Hardware Pins
#define AIN1_R 22
#define AIN2_R 23
#define PWMA_R 44
#define BIN1_R 24
#define BIN2_R 25
#define PWMB_R 45

#define AIN1_L 26
#define AIN2_L 27
#define PWMA_L 46
#define BIN1_L 28
#define BIN2_L 29
#define PWMB_L 2

void rightForward(int speed) {
  digitalWrite(AIN1_R, HIGH); digitalWrite(AIN2_R, LOW);
  analogWrite(PWMA_R, speed);
  digitalWrite(BIN1_R, HIGH); digitalWrite(BIN2_R, LOW);
  analogWrite(PWMB_R, speed);
}

void rightBackward(int speed) {
  digitalWrite(AIN1_R, LOW); digitalWrite(AIN2_R, HIGH);
  analogWrite(PWMA_R, speed);
  digitalWrite(BIN1_R, LOW); digitalWrite(BIN2_R, HIGH);
  analogWrite(PWMB_R, speed);
}

void leftForward(int speed) {
  digitalWrite(AIN1_L, HIGH); digitalWrite(AIN2_L, LOW);
  analogWrite(PWMA_L, speed);
  digitalWrite(BIN1_L, HIGH); digitalWrite(BIN2_L, LOW);
  analogWrite(PWMB_L, speed);
}

void leftBackward(int speed) {
  digitalWrite(AIN1_L, LOW); digitalWrite(AIN2_L, HIGH);
  analogWrite(PWMA_L, speed);
  digitalWrite(BIN1_L, LOW); digitalWrite(BIN2_L, HIGH);
  analogWrite(PWMB_L, speed);
}

void stopLMotors() {
  digitalWrite(AIN1_L, LOW); digitalWrite(AIN2_L, LOW);
  analogWrite(PWMA_L, 0);
  digitalWrite(BIN1_L, LOW); digitalWrite(BIN2_L, LOW);
  analogWrite(PWMB_L, 0);
}

void stopRMotors() {
  digitalWrite(AIN1_R, LOW); digitalWrite(AIN2_R, LOW);
  analogWrite(PWMA_R, 0);
  digitalWrite(BIN1_R, LOW); digitalWrite(BIN2_R, LOW);
  analogWrite(PWMB_R, 0);
}

void moveForward(int speed) {
  rightForward(speed);
  leftForward(speed);
}

void moveBackward(int speed) {
  rightBackward(speed);
  leftBackward(speed);
}

void turnRight(int speed) {
  leftForward(speed);
  rightBackward(speed);
}

void turnLeft(int speed) {
  leftBackward(speed);
  rightForward(speed);
}

void stopRobot() {
  stopLMotors();
  stopRMotors();
}

bool mpuInit() {
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(PWR_MGMT_1);
  Wire.write(0x00);
  if (Wire.endTransmission(true) != 0) return false;
  delay(100);
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(ACCEL_CONFIG);
  Wire.write(0x08); // ±8g
  return (Wire.endTransmission(true) == 0);
}

void i2cRecover() {
   Wire.end();
   pinMode(20, INPUT_PULLUP); // SDA на Mega = pin 20
   pinMode(21, OUTPUT);       // SCL на Mega = pin 21
   for (int i = 0; i < 20; i++) {
     digitalWrite(21, LOW);
     delayMicroseconds(10);
     digitalWrite(21, HIGH);
     delayMicroseconds(10);
   }
   Wire.begin();
   Wire.setClock(400000L);
   Wire.setWireTimeout(3000, true);
 }

float readAccelY() {
  Wire.beginTransmission(MPU_ADDR);
   Wire.write(ACCEL_YOUT_H);
   if (Wire.endTransmission(false) != 0) {
     i2cRecover();
     return 0.0;
   }
   Wire.requestFrom(MPU_ADDR, 2, true);
   if (Wire.available() < 2) {
     i2cRecover();
     return 0.0;
   }
   int16_t raw = (Wire.read() << 8) | Wire.read();
   float accel_cm_s2 = ((float)raw / 8192.0) * 980.66;
   return accel_cm_s2;
}

  /*This is for debugging from Arduinos terminal
  String str = "";

  if (Serial.available()) {
    str = Serial.readStringUntil('\n');
  } else if (Serial1.available()) {
    return "";
  } else {
    return "";
  }

  str.trim();
  if (str.length() == 0) return "";

  if (str.equalsIgnoreCase("L")) return "left";
  if (str.equalsIgnoreCase("R")) return "right";

  String numStr = str;
  if (str.charAt(0) == 'M' || str.charAt(0) == 'm') {
    numStr = str.substring(1);
    numStr.trim();
  }

  float val = numStr.toFloat();
  if (val > 0) return String(val, 2);

  return "";*/

String readIncomingCommand() {
  String str = "";

  if (Serial1.available()) {
    str = Serial1.readStringUntil('\n');
  } else {
    return "";
  }

  str.trim();
  if (str.length() == 0) return "";

  if (str.equalsIgnoreCase("L")) return "left";
  if (str.equalsIgnoreCase("R")) return "right";

  String numStr = str;
  if (str.charAt(0) == 'M' || str.charAt(0) == 'm') {
    numStr = str.substring(1);
    numStr.trim();
  }

  float val = numStr.toFloat();
  if (val > 0) return String(val, 2);

  return "";
}

void calibrate() {
  Serial.print("Calibrating");
  biasY = 0.0;
  for (int i = 0; i < 1000; i++) {
    biasY += readAccelY();
    if (i % 200 == 0) Serial.print(".");
  }
  biasY /= 1000.0;
  Serial.print(" bias=");
  Serial.print(biasY, 2);
  Serial.println(" OK");
}

void setup() {
  Serial.begin(115200);
  Serial1.begin(115200);
  Wire.begin();
  Wire.setClock(400000L);
  Wire.setWireTimeout(3000, true);

  if (!mpuInit()) Serial.println("MPU6050 init FAILED!");
  else            Serial.println("MPU6050 OK");

  pinMode(AIN1_R, OUTPUT); pinMode(AIN2_R, OUTPUT); pinMode(PWMA_R, OUTPUT);
  pinMode(BIN1_R, OUTPUT); pinMode(BIN2_R, OUTPUT); pinMode(PWMB_R, OUTPUT);
  pinMode(AIN1_L, OUTPUT); pinMode(AIN2_L, OUTPUT); pinMode(PWMA_L, OUTPUT);
  pinMode(BIN1_L, OUTPUT); pinMode(BIN2_L, OUTPUT); pinMode(PWMB_L, OUTPUT);

  stopRobot();

  calibrate();

  t_then = micros();
}

double calkdist(){
  float accelY = readAccelY() - biasY;
  AcselY_Filter = AcselY_Filter + ALPHA * (accelY - AcselY_Filter);

  if(AcselY_Filter<0.0) {
    AcselY_Filter=0.0;
  }

  t_now = micros();
  float dt = (float)(t_now - t_then ) / (float)1000000.0;

  t_then = t_now;

  velocityY += (double)AcselY_Filter * dt;
  distanceY += velocityY * dt;
  return distanceY;
}

void zero_var(){
  AcselY_Filter = 0;
  velocityY = 0.0;
  distanceY= 0.0;
  t_now = 0;
  t_then = 0;
}


static unsigned long lastPrint = 0;


void loop() {
  String cmd = readIncomingCommand();

  delay(10);

  if (cmd == "left") {
    Serial.println("Turning LEFT...");
    stopRobot();
    turnLeft(150);
    delay(1100);
    stopRobot();
    Serial.println("Turn done.");

  } else if (cmd == "right") {
    Serial.println("Turning RIGHT...");
    stopRobot();
    turnRight(150);
    delay(1100);
    stopRobot();
    Serial.println("Turn done.");

  } else if (cmd.length() > 0) {
    float val = cmd.toFloat();
    if (val > 0) {
      targetDistance = val;
      Serial.print("Target set: ");
      Serial.print(val);
      Serial.println(" cm — moving forward...");
      calibrate();
      zero_var();
      moveForward(160);
      t_then = micros();
      lastPrint=millis();
    }
  }

  if (targetDistance > 0) {
    distanceY = calkdist();
    if (distanceY >= targetDistance) {
      stopRobot();

      Serial.print(">> Destination reached! Total: ");
      Serial.print(distanceY, 1);
      Serial.println(" cm");

      Serial1.println("R");
      calibrate();
      zero_var();
      targetDistance = -1.0;
    }
  }
}