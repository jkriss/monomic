package jklabs.monomic;

import oscP5.OscIn;
import oscP5.OscMessage;
import oscP5.OscP5;

public class MonomeOSC extends Monome {

	// oscP5 instance for the osc communication
	private OscP5 oscP5;
	private String oscP5event;
	private String boxName = "box";

	// osc constants
	private static final String LED = "led";
	private static final String ROW = "led_row";
	private static final String COL = "led_col";
	private static final String SHUTDOWN = "shutdown";
	private static final String BUTTON = "press";
	private static final String TEST = "test";
	private static final String ADC = "adc_val";
	private static final String ADC_ENABLE = "adc_enable";

	// osc addresses for this instance
	private String led, row, col, shutdown, button, test, adc, adc_enable;

	public MonomeOSC(Object parent) {
		this(parent, "box");
//		this(parent, null);
	}

	public MonomeOSC(Object parent, String boxName) {
		this(parent, boxName, "127.0.0.1");
	}

	public MonomeOSC(Object parent, String boxName, String host) {
		this(parent, boxName, host, 8080, 8000);
	}

	public MonomeOSC(Object parent, String boxName, String host, int sendPort, int receivePort) {
		super(parent);
		initOsc(host, sendPort, receivePort);

		if (boxName == null) {
			String[] monomes = MonomeSerial.getMonomes();
			if (monomes.length > 0)
				boxName = monomes[0];
		}

		setBoxName(boxName);
		super.init();
	}

	private void setBoxName(String boxName) {
		this.boxName = boxName;

		// set osc addresses
		led = prependName(LED);
		row = prependName(ROW);
		col = prependName(COL);
		shutdown = prependName(SHUTDOWN);
		button = prependName(BUTTON);
		test = prependName(TEST);
		adc = prependName(ADC);
		adc_enable = prependName(ADC_ENABLE);
	}

	private String prependName(String command) {
		return "/" + boxName + "/" + command;
	}

	////////////////////////////////////////////////// monome functions

	public void testPattern(boolean b) {
		super.testPattern(b);
		OscMessage oscMsg = makeMessage(test);
		oscMsg.add(b ? 1 : 0);
		send(oscMsg);
	}

	public void setValue(int x, int y, int value) {
		super.setValue(x, y, value);
		OscMessage oscMsg = makeMessage(led);
		oscMsg.add(x);
		oscMsg.add(y);
		oscMsg.add(value);
		send(oscMsg);
	}

	public void setRow(int i, byte bitVals) {
		super.setRow(i, bitVals);
		setRowOrColumn(row, i, bitVals);
	}

	public void setCol(int i, byte bitVals) {
		super.setCol(i, bitVals);
		setRowOrColumn(col, i, bitVals);
	}

	public void setLowPower(boolean b) {
		super.setLowPower(b);
		OscMessage oscMsg = makeMessage(shutdown);
		oscMsg.add(b ? 1 : 0);
		send(oscMsg);
	}

	public void setLedIntensity(float f) {
		OscMessage oscMsg = makeMessage(led);
		oscMsg.add(f);
		send(oscMsg);
	}
	
	protected void setADC(int i, boolean b) {
		OscMessage oscMsg = makeMessage(adc_enable);
		oscMsg.add(i);
		oscMsg.add(b?1:0);
		send(oscMsg);
	}

	////////////////////////////////////////////////// helper methods

	private OscMessage makeMessage(String command) {
		return oscP5.newMsg(command);
	}

	private void setRowOrColumn(String command, int i, int bitVals) {
		OscMessage oscMsg = makeMessage(command);
		oscMsg.add(i);
		oscMsg.add(bitVals);
		send(oscMsg);
	}

	////////////////////////////////////////////////// osc communication

	private void send(OscMessage m) {
		if (debug == FINE) System.out.println("$$ sending " + m.getMsgName() + " " + m.getArgs());
		oscP5.sendMsg(m);
	}

	private void initOsc(String host, int sendPort, int receivePort) {
		oscP5event = "oscEvent";
		oscP5 = new OscP5(this, host, sendPort, receivePort, oscP5event);
	}

	public void oscEvent(OscIn oscIn) {
		if (debug == FINE) 
			System.out.println("received a message ... forwarding to unpackMessage(OscIn)");
		unpackMessage(oscIn);
	}

	void unpackMessage(OscIn oscIn) {
		if (boxName == null) {
			String a = oscIn.getAddrPattern();
			if (a.indexOf("m40h") != -1) {
				String newBox = a.substring(1, a.indexOf('/', 1));
				System.out.println("discovered new monome 40h: " + newBox);
				setBoxName(newBox);
			}
		}
		if (oscIn.checkAddrPattern(button)) {
			if (oscIn.checkTypetag("iii")) {
				int x = oscIn.getInt(0);
				int y = oscIn.getInt(1);
				int value = oscIn.getInt(2);
				handleInputEvent(x, y, value);
			}
		} else if (oscIn.checkAddrPattern(adc)) {
			if (oscIn.checkTypetag("if")) {
				int port = oscIn.getInt(0);
				float value = oscIn.getFloat(1);
				handleAdcInput(port, value);
			}
		} else {
			if (debug == FINE) {
				System.out.println("you have received an osc message "
						+ oscIn.getAddrPattern() + "   " + oscIn.getTypetag());
				Object[] o = oscIn.getData();
				for (int i = 0; i < o.length; i++) {
					System.out.println(i + "  " + o[i]);
				}
			}
		}
	}

	////////////////////////////////////////////////// cleanup

	protected void finalize() throws Throwable {
		oscP5.disconnectFromTEMP();
		oscP5 = null;
		super.finalize();
	}

}
