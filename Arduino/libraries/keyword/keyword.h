#ifndef MyLibrary_H
#define MyLibrary_H

#include <SoftwareSerial.h>
#include <Servo.h>
#include <NewPing.h>

#define vibr 4
#define intr1 3 
#define intr2 18
#define intr3 19
#define intr4 20
#define intr5 21
#define ledR 30
#define ledG 32
#define ledB 34
#define trig1 40
#define echo1 41
#define trig2 42
#define echo2 43
#define trig3 44
#define echo3 45
#define buzz 46
#define serv 48
#define MAX_DISTANCE 200

void toggleSonar();
void takePicture();
void takeOCR();
void findWay();
void toggleLED();
void vibration(bool);
void alarm(bool);
void turnServo(int);
void switchServo();
void callBLE(char);
void findStick();

#endif