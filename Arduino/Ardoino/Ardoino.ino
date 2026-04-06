#include <Wire.h>
#include <Servo.h>

#define MPU_ADDR      0x68
#define PWR_MGMT_1    0x6B
#define ACCEL_CONFIG  0x1C
#define ACCEL_YOUT_H  0x3D
#define GYRO_ZOUT_H   0x47

float targetDistance = 0;

#define ALPHA 0.2
float AcselY_Filter = 0;
double velocityY      = 0.0;
double distanceY      = 0.0;
float biasY = 0.0;

float gyroZ_bias = 0.0;
float currentAngle = 0.0;

unsigned long t_now = 0;
unsigned long t_then = 0;

unsigned long moveStartTime = 0;

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

Servo servo;
#define SERVO_PIN 7
int servoMoveCount = 0;

void rightForward(int speed) {
  digitalWrite(AIN1_R, HIGH); 
  digitalWrite(AIN2_R, LOW);
  analogWrite(PWMA_R, speed);
  digitalWrite(BIN1_R, HIGH); 
  digitalWrite(BIN2_R, LOW);
  analogWrite(PWMB_R, speed);
}

void rightBackward(int speed) {
  digitalWrite(AIN1_R, LOW); 
  digitalWrite(AIN2_R, HIGH);
  analogWrite(PWMA_R, speed);
  digitalWrite(BIN1_R, LOW); 
  digitalWrite(BIN2_R, HIGH);
  analogWrite(PWMB_R, speed);
}

void leftForward(int speed) {
  digitalWrite(AIN1_L, HIGH); 
  digitalWrite(AIN2_L, LOW);
  analogWrite(PWMA_L, speed);
  digitalWrite(BIN1_L, HIGH); 
  digitalWrite(BIN2_L, LOW);
  analogWrite(PWMB_L, speed);
}

void leftBackward(int speed) {
  digitalWrite(AIN1_L, LOW);
  digitalWrite(AIN2_L, HIGH);
  analogWrite(PWMA_L, speed);
  digitalWrite(BIN1_L, LOW); 
  digitalWrite(BIN2_L, HIGH);
  analogWrite(PWMB_L, speed);
}

void stopLMotors() {
  digitalWrite(AIN1_L, LOW); 
  digitalWrite(AIN2_L, LOW);
  analogWrite(PWMA_L, 0);
  digitalWrite(BIN1_L, LOW); 
  digitalWrite(BIN2_L, LOW);
  analogWrite(PWMB_L, 0);
}

void stopRMotors() {
  digitalWrite(AIN1_R, LOW); 
  digitalWrite(AIN2_R, LOW);
  analogWrite(PWMA_R, 0);
  digitalWrite(BIN1_R, LOW); 
  digitalWrite(BIN2_R, LOW);
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
   pinMode(20, INPUT_PULLUP); 
   pinMode(21, OUTPUT);       
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

float readGyroZ() {
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(GYRO_ZOUT_H);
  if (Wire.endTransmission(false) != 0) {
    i2cRecover();
    return 0.0;
  }
  Wire.requestFrom(MPU_ADDR, 2, true);
  if (Wire.available() < 2) return 0.0;
  int16_t raw = (Wire.read() << 8) | Wire.read();
  return (float)raw / 131.0;
}
/*DEBUGGING FROM SERIAL MONITOR:
String readIncomingCommand() {
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

  return "";
}*/

//FROM ESP32:
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
  Serial.print("Calibrating...");
  biasY = 0.0;
  gyroZ_bias = 0.0;
  for (int i = 0; i < 1000; i++) {
    biasY += readAccelY();
    gyroZ_bias += readGyroZ();
    if (i % 200 == 0) Serial.print(".");
  }
  biasY /= 1000.0;
  gyroZ_bias /= 1000.0;
}

void SetMotorDriversToOutput(){
  pinMode(AIN1_R, OUTPUT);
  pinMode(AIN2_R, OUTPUT);
  pinMode(PWMA_R, OUTPUT);
  pinMode(BIN1_R, OUTPUT);
  pinMode(BIN2_R, OUTPUT);
  pinMode(PWMB_R, OUTPUT);
  pinMode(AIN1_L, OUTPUT);
  pinMode(AIN2_L, OUTPUT);
  pinMode(PWMA_L, OUTPUT);
  pinMode(BIN1_L, OUTPUT);
  pinMode(BIN2_L, OUTPUT);
  pinMode(PWMB_L, OUTPUT);
}

void setup() {
  servo.attach(SERVO_PIN);
  servo.write(0);
  Serial.begin(115200);
  Serial1.begin(115200);
  Wire.begin();
  Wire.setClock(400000L);
  Wire.setWireTimeout(3000, true);

  if (!mpuInit()) Serial.println("MPU6050 init FAILED!");
  else            Serial.println("MPU6050 OK");

  SetMotorDriversToOutput();

  stopRobot();
  calibrate();
  t_then = micros();
}

double calkdist(){
  float accelY = readAccelY() - biasY;
  AcselY_Filter = AcselY_Filter + ALPHA * (accelY - AcselY_Filter);
  if(AcselY_Filter<0.0) AcselY_Filter=0.0;
    
  t_now = micros();
  float dt = (float)(t_now - t_then ) / 1000000.0;
  t_then = t_now;  
  
  velocityY += (double)AcselY_Filter * dt;
  distanceY += velocityY * dt;
  return distanceY;
}

void loop() {
  String cmd = readIncomingCommand();
  delay(10);

  t_now = micros();
  float dt = (float)(t_now - t_then) / 1000000.0;
  t_then = t_now;

  float gz = readGyroZ() - gyroZ_bias;
  if (abs(gz) > 0.1) {
     currentAngle += gz * dt;
  }

  static unsigned long lastPrint = 0;
  if (millis() - lastPrint > 100) {
    lastPrint = millis();
  }

  if (cmd == "left") {
    servoMoveCount++;
    if (servoMoveCount >= 2) {
      servoMoveCount = 0;
      servo.write(180);
    }

    Serial.println("Turning LEFT");
    currentAngle = 0.0;
    t_then = micros();
    turnLeft(150);

    while (currentAngle < 90.0) {
      //Debuging:
      //Serial.println(currentAngle);
      t_now = micros();
      float turn_dt = (float)(t_now - t_then) / 1000000.0;
      t_then = t_now;

      float turn_gz = readGyroZ() - gyroZ_bias;
      if (abs(turn_gz) > 0.1) {
         currentAngle += turn_gz * turn_dt;
      }
    }
    stopRobot();
    Serial.println("Turn done.");

  } else if (cmd == "right") {
    servoMoveCount++;
    if (servoMoveCount >= 2) {
      servoMoveCount = 0;
      servo.write(0);
    }

    Serial.println("Turning RIGHT");
    currentAngle = 0.0;
    t_then = micros();
    turnRight(150);

    while (currentAngle > -90.0) {
      //Debuging:
      //Serial.println(currentAngle);
      t_now = micros();
      float turn_dt = (float)(t_now - t_then) / 1000000.0;
      t_then = t_now;

      float turn_gz = readGyroZ() - gyroZ_bias;
      if (abs(turn_gz) > 0.1) {
         currentAngle += turn_gz * turn_dt;
      }
    }
    stopRobot();
    Serial.println("Turn done.");

  } else if (cmd.length() > 0) {
    float val = cmd.toFloat();
    if (val > 0) {
      float msPerCm = 25.0;
      unsigned long duration = (unsigned long)(val * msPerCm);
      //debuging:
      //Serial.print("Moving ");
      //Serial.print(val);
      //Serial.print(" cm for ");
      //Serial.print(duration);
      //Serial.println(" ms");

      moveForward(160);
      delay(duration);
      stopRobot();

      Serial.println("Destination reached!");
      Serial1.println("R");
    }
  }
}
