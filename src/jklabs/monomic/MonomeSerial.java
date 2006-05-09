package jklabs.monomic;

import java.util.Vector;

import processing.core.PApplet;
import processing.serial.Serial;

public class MonomeSerial extends Monome {

	protected static final long SLEEP_TIME = 5;

	// serial constants
	private static final byte BUTTON_ON = 0x01;
	private static final byte BUTTON_OFF = 0x00;
	private static final int BUTTON_PREFIX = 0;
	private static final int ADC_INPUT_PREFIX = 1;
	private static final byte ADC_ENABLE = 0x50;
	private static final byte LED_ON = 0x21;
	private static final byte LED_OFF = 0x20;
	private static final byte LED_INTENSITY = 0x30;
	private static final byte LED_TEST = 0x40;
	private static final byte SHUTDOWN = 0x60;
	private static final byte ROW_PREFIX = 0x7;
	private static final byte COL_PREFIX = 0x8;

	private Serial serialPort;
	private Thread serialThread;
	private boolean listening;

	public MonomeSerial(Object listener) {
		this(listener, null);
	}

	public MonomeSerial(Object listener, String devName) {
		super(listener);
		init(devName);
	}

	private void init(String devName) {
		getMethods(listener);
		if (listener instanceof PApplet) {
			if (devName == null && getMonomes().length > 0)
				devName = getMonomes()[0];
			if (devName != null) {
				PApplet p = (PApplet) listener;
				serialPort = new Serial(p, devName, 19200);
				serialThread = createSerialThread(serialPort);
				p.registerDispose(this);
				super.init();
				startListening();
			} else
				System.err.println("sorry, no monomes found on the serial bus; ignoring all commands");
		}
	}

	private void startListening() {
		listening = true;
		serialThread.start();
		if (debug == FINE)
			System.out.println("started serial listening thread");
	}

	private void stopListening() {
		listening = false;
		if (debug == FINE)
			System.out.println("stopped serial listening thread");
	}

	private Thread createSerialThread(final Serial port) {
		return new Thread(new Runnable() {
			private byte[] buffer = new byte[2];

			public void run() {
				while (listening) {
					while (port.available() > 0) {
						port.readBytes(buffer);
						handleSerialInput(buffer[0], buffer[1]);
					}
					try {
						Thread.sleep(SLEEP_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}, "monome serial listener thread");
	}

	public static String[] getMonomes() {
		String[] all = Serial.list();
		Vector v = new Vector();
		for (int i = 0; i < all.length; i++)
			if (all[i].indexOf("40h") != -1)
				v.addElement(all[i]);
		String[] monomes = new String[v.size()];
		for (int i = 0; i < monomes.length; i++)
			monomes[i] = (String) v.elementAt(i);

		return monomes;
	}

	////////////////////////////////////////////////// handy stuff

	public void testInput() {
		handleSerialInput(BUTTON_ON, xyByte(1, 1));
		handleSerialInput(BUTTON_OFF, xyByte(1, 1));
	}

	////////////////////////////////////////////////// monome functions

	public void testPattern(boolean b) {
		super.testPattern(b);
		sendSerial(LED_TEST, (byte) (b ? 1 : 0));
	}

	public void setValue(int x, int y, int value) {
		super.setValue(x, y, value);
		sendSerial((value == 1) ? LED_ON : LED_OFF, xyByte(x, y));
	}

	public void setRow(int i, byte bitVals) {
		super.setRow(i, bitVals);
		sendSerial((byte) ((ROW_PREFIX << 4) + i), bitVals);
	}

	public void setCol(int i, byte bitVals) {
		super.setCol(i, bitVals);
		sendSerial((byte) ((COL_PREFIX << 4) + i), bitVals);
	}

	public void setLowPower(boolean b) {
		super.setLowPower(b);
		sendSerial(SHUTDOWN, (byte) (b ? 0 : 1));
	}

	public void setLedIntensity(float f) {
		super.setLedIntensity(f);
		sendSerial(LED_INTENSITY, (byte) f);
	}

	protected void setADC(int i, boolean b) {
		sendSerial(ADC_ENABLE, (byte)((i<<4)+(b?1:0)));
	}

	////////////////////////////////////////////////// serial helper methods

	private int bottom4(byte b) {
		return b & 0x0F;
	}

	private int top4(byte b) {
		return b >> 4;
	}

	private byte xyByte(int x, int y) {
		return (byte) ((x << 4) + y);
	}

	////////////////////////////////////////////////// serial communication

	private byte[] data = new byte[2];

	private void sendSerial(byte b1, byte b2) {
		if (debug == FINE)
			System.out.println("$$ sending data: " + bitString(b1) + " | "
					+ bitString(b2));
		data[0] = b1;
		data[1] = b2;
		if (serialPort != null) {
			serialPort.write(data);
		}
	}

	public void handleSerialInput(byte data0, byte data1) {

		if (debug == FINE)
			System.out.println("$$ received data: " + bitString(data0) + " | "
					+ bitString(data1));
		int address = top4(data0);

		switch (address) {
		case BUTTON_PREFIX:
			handleInputEvent(top4(data1), bottom4(data1), bottom4(data0));
			break;
		case ADC_INPUT_PREFIX:
			handleAdcInput((data0>>2)&0xf3, (float)((data0<<6)+data1));
		default:
		}
	}

	////////////////////////////////////////////////// cleanup

	protected void finalize() throws Throwable {
		stopListening();
		serialPort.stop();
		super.finalize();
	}
	
	public void dispose() throws Throwable {
		finalize();
	}

}
