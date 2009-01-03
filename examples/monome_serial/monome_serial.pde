import jklabs.monomic.*;

import processing.serial.*;

Monome m = new MonomeSerial(this);

m.setDebug(true);

m.testPattern(true);
