import processing.serial.*;
import jklabs.monomic.*;

Monome m;

void setup() {
  m = new MonomeSerial(this);
}

void monomePressed(int x, int y) {
  m.setValue(x, y, !m.isLit(x,y));
}

void draw() {
  
}

