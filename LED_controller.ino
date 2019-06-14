#include <SoftwareSerial.h>
SoftwareSerial BTserial(10, 11); // not for Serial port

//RGB LED Pins
int RED = 3;
int GREEN = 6;
int BLUE = 5;

int led = 13;//arduino led

String data = "";
boolean RGB_Completed = false;
 
void setup() {
  Serial.begin(9600); 
  BTserial.begin(9600);//bluetooth baud rate：9600
  data.reserve(30);
 
  pinMode(RED, OUTPUT); 
  pinMode(GREEN, OUTPUT); 
  pinMode(BLUE, OUTPUT); 
  pinMode(led, OUTPUT); 
}
 
void loop() {
//  BTserial.write("OKKKKK\r\n");// send data
  while (BTserial.available()) {
    char ReadChar = (char)BTserial.read();
//  while (Serial.available()) {
//    char ReadChar = (char)Serial.read();
    // Right parentheses ) indicates complet of the string
    if (ReadChar == '\n' || ReadChar == '\r') {
      RGB_Completed = true;
    } else {
      data += ReadChar;
    }
  }

  if (RGB_Completed) {
    //Print out debug info at Serial output window
    Serial.print("RGB:");
    Serial.print(data);
    Serial.println();
    
    if (data == "ON") {
      digitalWrite(13, HIGH);
      Light_RGB_LED("255.255.255");
    } 
    else if (data == "OFF") {
      digitalWrite(13, LOW);
      digitalWrite(RED, LOW);
      digitalWrite(GREEN, LOW);
      digitalWrite(BLUE, LOW);
    } 
    else {
      Light_RGB_LED(data);
    }
    
    data = "";
    RGB_Completed = false;
  }
}

void Light_RGB_LED(String RGB) {
  int SP1 = RGB.indexOf('.');
  int SP2 = RGB.indexOf('.', SP1 + 1);
  int SP3 = RGB.indexOf('.', SP2 + 1);
  String R = RGB.substring(0, SP1);
  String G = RGB.substring(SP1 + 1, SP2);
  String B = RGB.substring(SP2 + 1, SP3);

  int red = R.toInt();
  int green = G.toInt();
  int blue = B.toInt();

  //Print out debug info at Serial output window
  Serial.print("R=");
  Serial.println(constrain(red, 0, 255));
  Serial.print("G=");
  Serial.println(constrain(green, 0, 255));
  Serial.print("B=");
  Serial.println( constrain(blue, 0, 255));
  //Light up the LED with color code
  analogWrite(RED, red);
  analogWrite(GREEN, green);
  analogWrite(BLUE, blue);
}
