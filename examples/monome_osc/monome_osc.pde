import jklabs.monomic.*;

import processing.serial.*;

Monome m = new MonomeOSC(this);

m.setDebug(true);

m.testPattern(true);
