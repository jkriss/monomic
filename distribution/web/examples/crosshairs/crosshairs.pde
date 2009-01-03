import processing.serial.*;

import jklabs.monomic.*;

Monome m;

byte on = (byte)0xff;
byte off = 0;

void setup() {
  m = new MonomeSerial(this);
}

void draw() {

}

void monomePressed(int x, int y) {
   m.setCol(x, on);
   m.setRow(y, on);
}

void monomeReleased(int x, int y) {
   m.setCol(x, off);
   m.setRow(y, off);
}

