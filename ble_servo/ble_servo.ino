/*
    Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleWrite.cpp
    Ported to Arduino ESP32 by Evandro Copercini
*/

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <ESP32Servo.h>


#define SERVICE_UUID        "14eff093-8234-4b9c-89ee-6929ad5222e1"
#define CHARACTERISTIC_UUID "ed011481-be35-4609-b0d3-1f57019b2af2"

uint16_t servoPos = 1500;


template<typename T>
bool parseValue(BLECharacteristic* pCharacteristic, T &value) {
    if (pCharacteristic->getLength() >= sizeof(T)) {
        memcpy(&value, pCharacteristic->getData(), sizeof(T));
        return true;
    }
    return false;
}

class ServoCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) {
    uint16_t pos;
    if (parseValue(pCharacteristic, pos)) {
        Serial.println("Characteristics reads: ");
        Serial.println(pos);
        Serial.print("servoPos reads: ");
        Serial.println(servoPos);

    } else {
        Serial.println("Error: Not enough data for uint16_t.");
    }
  }
};

void setup() {
  Serial.begin(9600);

  // Serial.println("1- Download and install an BLE scanner app in your phone");
  // Serial.println("2- Scan for BLE devices in the app");
  // Serial.println("3- Connect to MyESP32");
  // Serial.println("4- Go to CUSTOM CHARACTERISTIC in CUSTOM SERVICE and write something");
  // Serial.println("5- See the magic =)");

  BLEDevice::init("MayWindTunnel");
  BLEServer *pServer = BLEDevice::createServer();

  BLEService *pService = pServer->createService(SERVICE_UUID);

  BLECharacteristic *pCharacteristic =
    pService->createCharacteristic(CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);

  pCharacteristic->setCallbacks(new ServoCallbacks());

  pCharacteristic->setValue(servoPos);
  pService->start();

  BLEAdvertising *pAdvertising = pServer->getAdvertising();
  pAdvertising->start();
  Serial.println("Started advertising");
}

void loop() {
  // put your main code here, to run repeatedly:
  delay(200);
}
