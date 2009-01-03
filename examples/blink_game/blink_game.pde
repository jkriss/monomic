import processing.serial.*;

import jklabs.monomic.*;

Monome m;

byte all = (byte)0xff;
byte[] matrix = {0x0f,0x0f,0x0f,0x0f,0x0f,0x0f,0x0f,0x0f};

void setup() {
  m = new MonomeSerial(this);
  framerate(2);
  m.setValues(matrix);
}

void draw() {
  m.invert();
}

void monomePressed(int x, int y) {
  m.invertCol(x);
}
