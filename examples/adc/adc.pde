import processing.serial.*;

import jklabs.monomic.*;

Monome m;

void setup() {
  m = new MonomeSerial(this);
  m.enableADC(0);
}

void draw() {

}

void monomeAdc(int port, float value) {
  println("monome adc " + port + ": " + value); 
}
