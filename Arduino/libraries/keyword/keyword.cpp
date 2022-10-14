#include "Arduino.h"
#include "keyword.h"
extern Servo servo;
extern bool togSonar, togLED, togBuzz;
extern bool sDir[3];
/* intr1 */
void toggleSonar(){
	delayMicroseconds(2000); /* Chattering */
	if(digitalRead(intr1)!=LOW) return;
	togSonar = !togSonar;
	if(!togSonar){
		vibration(false);
		alarm(false);
		turnServo(1);
	}
	Serial2.println(5);
}
/* intr2 */
void takePicture(){
	delayMicroseconds(2000);
	if(digitalRead(intr2)!=LOW) return;
	digitalWrite(ledR,255);
	digitalWrite(ledG,0);
	digitalWrite(ledB,0);
	Serial2.println(1);
}
/* intr3 */
void takeOCR(){
	delayMicroseconds(2000);
	if(digitalRead(intr3)!=LOW) return;
	digitalWrite(ledR,255);
	digitalWrite(ledG,228);
	digitalWrite(ledB,0);
	Serial2.println(2);
}
/* intr4 */
void findWay(){
	delayMicroseconds(2000);
	if(digitalRead(intr4)!=LOW) return;
	digitalWrite(ledR,0);
	digitalWrite(ledG,255);
	digitalWrite(ledB,0);
	Serial2.println(3);
}
/* intr5 */
void toggleLED(){
	delayMicroseconds(2000);
	if(digitalRead(intr5)!=LOW) return;
	if(togLED == true){
		digitalWrite(ledR, 0);
		digitalWrite(ledG, 0);
		digitalWrite(ledB, 0);
	}
	togLED = !togLED;
	Serial2.println(4);
}
/* vibration module */
void vibration(bool flag){
	if(flag){
		analogWrite(vibr, 64);
		Serial.println("vibra on");
	}
	else{
		analogWrite(vibr,0);
	}
}
/* active buzzer */
void alarm(bool flag){
	if(flag){
		digitalWrite(buzz, HIGH);
	}
	else{
		digitalWrite(buzz, LOW);
	}
}
/* Servo
	dir == 0   : ?
	dir == 90  : ?
	dir == 180 : ?
 */
void turnServo(int dir){
	dir *= 90;
	servo.write(dir);
}
void switchServo(){
	// 0 : left
	// 1 : mid
	// 2 : right
	if(!sDir[0] and !sDir[1] and !sDir[2]){ 	// 000
		turnServo(1);
	}
	else if(!sDir[0] and !sDir[1] and sDir[2]){ // 001
		turnServo(0);
	}
	else if(!sDir[0] and sDir[1] and !sDir[2]){ // 010
		turnServo(2);
	}
	else if(!sDir[0]){ 							// 011
		turnServo(0);
	}
	else if(!sDir[1] and !sDir[2]){				// 100
		turnServo(2);
	}
	else if(!sDir[1]){ 							// 101
		turnServo(1);
	}
	else if(!sDir[2]){ 							// 110
		turnServo(2);
	}
	else{ 										// 111
		turnServo(1);
	}
}
void findStick(){
	togBuzz = !togBuzz;
	alarm(togBuzz);
}
/* Bluetooth */
void callBLE(char ch){
	if(ch == 'a'){
      takePicture();
    }
    else if(ch == 'b'){
      takeOCR();
    }
    else if(ch == 'c'){
      findWay();
    }
    else if(ch == 'd'){
      toggleLED();
    }
	else if(ch == 'e'){
		toggleSonar();
	}
	else if(ch == 'f'){
		findStick();
	}
}