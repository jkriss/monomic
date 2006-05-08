import jklabs.monomic.Monome;
import jklabs.monomic.MonomeSerial;
import processing.core.PApplet;

public class MonomeTest extends PApplet {
Monome m;

public void setup() {
  
  //m = new MonomeOSC(this);
  m = new MonomeSerial(this);
  
  m.setDebug(m.FINE);
  
  //m.lightsOn();
  //m.lightsOff();
  m.lightOn(0,0);
  m.lightOff(0,1);
  
  int[] vals = new int[]{0,0,1,1,0,0,1,1};
  //int[] vals = new int[]{1,1,1,1,1,1,1,1};
  int[][] matrix = new int[][]{
                    {0,1,0,1,0,1,0,1},
                    {1,0,1,0,1,0,1,0},
                    {0,1,0,1,0,1,0,1},
                    {1,0,1,0,1,0,1,0},
                    {0,1,0,1,0,1,0,1},
                    {1,0,1,0,1,0,1,0},
                    {0,1,0,1,0,1,0,1},
                    {1,0,1,0,1,0,1,0},
                  };
  m.setRow(1,vals);
  m.setCol(2,vals);
  //m.setValues(matrix);
  
  m.testInput();
  
  m.setLedIntensity(0.5f);
  
  m.setLowPower(true);
  
}


public void monomePressed(int x, int y) {
  println("button pressed! (" + x + "," + y + ")");
}

public void monomeReleased(int x, int y) {
  println("button released! (" + x + "," + y + ")");  
}
}