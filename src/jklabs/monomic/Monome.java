package jklabs.monomic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Monome {

	private static int x_dim = 8;
	private static int y_dim = x_dim;
	private static int[][] ALL_ON;
	private static int[][] ALL_OFF;
	private static Integer[] INTS;

	public static int NONE = 99;
	public static int FINE = 1;
	public static int WARNING = 2;
	protected int debug = NONE;
	
	private byte[] buttonVals = new byte[y_dim];
	private byte[] ledVals = new byte[y_dim];

	// create ALL_ON and ALL_OFF matrices, INT array
	static {
		ALL_ON = new int[x_dim][y_dim];
		ALL_OFF = new int[x_dim][y_dim];
		INTS = new Integer[x_dim];
		for (int i = 0; i < x_dim; i++) {
			for (int j = 0; j < y_dim; j++) {
				ALL_ON[i][j] = 1;
				ALL_OFF[i][j] = 0;
			}
			INTS[i] = new Integer(i);
		}
	}

	protected Object listener;

	// for efficient method calling using reflection
	private Integer[] buttonLoc = { null, null };
	private Object[] adcValues = {null, null};

	private Method buttonPressedMethod;
	private Method buttonReleasedMethod;
	private Method adcInputMethod;

	boolean[][] ledValues = new boolean[y_dim][x_dim];
	boolean[][] buttonValues = new boolean[y_dim][x_dim];
	
	boolean tempRow[] = new boolean[y_dim];

	protected Monome(Object listener) {
		this.listener = listener;
		getMethods(listener);
	}
	
	protected void init() {
		testPattern(false);
		lightsOff();
		setLedIntensity(10f);
		for (int i=0; i<4; i++) disableADC(i);
		setLowPower(false);
	}

	protected void getMethods(Object parent) {
		Class[] args = new Class[] { int.class, int.class };
		Class[] adcArgs = new Class[] {int.class, float.class};
		try {
			buttonPressedMethod = parent.getClass().getDeclaredMethod(
					"monomePressed", args);
		} catch (NoSuchMethodException e) {
			// not a big deal if they aren't implemented
		}
		try {
			buttonReleasedMethod = parent.getClass().getDeclaredMethod(
					"monomeReleased", args);
		} catch (NoSuchMethodException e) {
			// not a big deal if they aren't implemented
		}
		try {
			adcInputMethod = parent.getClass().getDeclaredMethod(
					"monomeAdc", adcArgs);
		} catch (NoSuchMethodException e) {
			// not a big deal if they aren't implemented
		}
	}

	////////////////////////////////////////////////// handy stuff

	public void setDebug(boolean b) {
		if (b)
			setDebug(FINE);
		else
			setDebug(NONE);
	}

	public void setDebug(int debug) {
		this.debug = debug;
	}

	public void testInput() {
		handleInputEvent(0, 0, 1);
		handleInputEvent(3, 4, 0);
	}

	////////////////////////////////////////////////// button state
	
	public boolean isPressed(int x, int y) {
		return buttonValues[x][y];
	}
	
	public boolean isLit(int x, int y) {
		return ledValues[x][y];
	}
	
	public byte[] getLedValues() {
		return pack(ledValues, ledVals);
	}
	
	public byte[] getButtonValues() {
		return pack(buttonValues, buttonVals);
	}
		
	public byte getRowValues(int i) {
		for (int y=0; y<y_dim; y++)
			tempRow[y] = ledValues[y][i];
		return pack(tempRow);	
	}
	
	public byte getColValues(int i) {
		return pack(ledValues[i]);	
	}
	
	
	////////////////////////////////////////////////// monome functions
	
	public void pressButton(int x, int y) {
		handleInputEvent(x, y, 1);
	}

	public void releaseButton(int x, int y) {
		handleInputEvent(x, y, 0);
	}

	public void testPattern(boolean b) {
		if (debug == FINE)
			System.out.println("setting led test pattern " + (b ? "on" : "off"));
	}

	public void lightsOn() {
		setValues(ALL_ON);
	}

	public void lightsOff() {
		setValues(ALL_OFF);
	}

	public void lightOn(int x, int y) {
		setValue(x, y, 1);
	}

	public void lightOff(int x, int y) {
		setValue(x, y, 0);
	}

	public void setValue(int x, int y, boolean value) {
		setValue(x, y, value?1:0);
	}
	
	public void setValue(int x, int y, int value) {
		if (debug == FINE)
			System.out.println("setting light " + x + "," + y + " to " + value);
		setInternalLedValue(x, y, value);
	}

	public void invertRow(int i) {
	    setRow(i, (byte)(0xff-getRowValues(i))); 
	}

	public void setRow(int i, int[] vals) {
		setRow(i, pack(vals));
	}

	public void setRow(int i, boolean[] vals) {
		setRow(i, pack(vals));
	}

	public void setRow(int i, byte bitVals) {
		if (debug == FINE) System.out.println("setting row " + i + " to " + bitString(bitVals));
		
		for (int j=0; j<8; j++)
			setInternalLedValue(j, i, (bitVals >> j )&0x01);
	}

	public void invertCol(int i) {
	    setCol(i, (byte)(0xff-getColValues(i))); 
	}
	
	public void setCol(int i, int[] vals) {
		setCol(i, pack(vals));
	}

	public void setCol(int i, boolean[] vals) {
		setCol(i, pack(vals));
	}

	public void setCol(int i, byte bitVals) {
		if (debug == FINE) System.out.println("setting col " + i + " to " + bitString(bitVals));

		for (int j=0; j<8; j++)
			setInternalLedValue(i, j, (bitVals >> j )&0x01);
	}

	public void setLowPower(boolean b) {
		if (debug == FINE)
			System.out.println("setting low power to " + b);
	}

	public void setLedIntensity(float f) {
		if (debug == FINE)
			System.out.println("setting led intensity to " + f);
	}

	public void invert() {
		for (int x=0; x<x_dim; x++)
			invertCol(x);
	}
	
	public void setValues(int[][] vals) {
		for (int i = 0; i < vals.length; i++)
			setRow(i, vals[i]);
	}

	public void setValues(boolean[][] vals) {
		for (int i = 0; i < vals.length; i++)
			setRow(i, vals[i]);
	}

	public void setValues(byte[] vals) {
		for (int i = 0; i < vals.length; i++)
			setRow(i, vals[i]);
	}

	public void enableADC(int i) {
		if (debug == FINE) System.out.println("enabling adc " + i);
		setADC(i, true);
	}
	
	public void disableADC(int i) {
		if (debug == FINE) System.out.println("disabling adc " + i);
		setADC(i, false);
	}
	
	protected void setADC(int i, boolean b) {
		
	}

	protected synchronized void handleAdcInput(int port, float value) {
		if (debug == FINE) System.out.println("adc input: port " + port + ": " + value);
		if (adcInputMethod == null) return;
		
		adcValues[0] = INTS[port];
		adcValues[1] = new Float(value);
		
		try {
			adcInputMethod.invoke(listener, adcValues);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	protected synchronized void handleInputEvent(int x, int y, int value) {
		if (x<0 || y<0) return;
		setInternalButtonValue(x, y, value);
		Method m = (value == 1) ? buttonPressedMethod : buttonReleasedMethod;
		if (m == null) // || x>x_dim-1 || y>y_dim-1 || x<0 || y<0)
			return;
		buttonLoc[0] = INTS[x];
		buttonLoc[1] = INTS[y];
		try {
			m.invoke(listener, buttonLoc);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}		
	}

	////////////////////////////////////////////////// helper methods

	private void setInternalLedValue(int x, int y, int value) {
//		System.out.println("setting internal state of " + x + "," + y + " to " + value);
		ledValues[x][y] = (value == 1);
	}

	private void setInternalButtonValue(int x, int y, int value) {
		buttonValues[x][y] = (value == 1);
	}
	
	private byte pack(int[] values) {
		byte b = 0;
		for (int i = 0; i < values.length && i < 8; i++)
			b += values[values.length - 1 - i] << values.length-1-i;
		return b;
	}
	
	private byte pack(boolean[] values) {
		byte b = 0;
		for (int i = 0; i < values.length && i < 8; i++)
			b += (values[values.length - 1 - i]?1:0) << values.length-1-i;
		return b;
	}
	
//	private byte[] pack(boolean[][] values) {
//		return pack(values, new byte[8]);
//	}
	
	private byte[] pack(boolean[][] values, byte[] dest) {
		for (int i=0; i<values.length; i++)
			dest[i] = pack(values[i]);
		return dest;
	}

	private StringBuffer s = new StringBuffer();

	public String bitString(byte b) {
		s.setLength(0);
		for (int i = 0; i < 8; i++) {
			s.insert(0, (char) b & 0x1);
			b >>= 1;
		}
		return s.toString();
	}
	
	public String getMatrixString() {
		String s = "";
		for (int y=0; y<y_dim; y++) {
			for (int x=0; x<x_dim; x++)
				s += (ledValues[x][y] ? 1 : 0) + " ";
			s +="\n";
		}
		return s;
	}
}
