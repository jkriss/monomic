import processing.core.PApplet;
import processing.serial.Serial;


public class SerialTest extends PApplet {

	public void setup() {
		Object listener = this;
		Serial serialPort;
		if (listener instanceof PApplet)
		      serialPort = new Serial((PApplet)listener);
	}
	
}
