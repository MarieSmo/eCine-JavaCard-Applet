package ecinepackage;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;

public class eCine extends Applet {
/* Constantes */
public static final byte CLA_MONAPPLET = (byte) 0xB0;
 
public static final byte INS_INCREMENTER_COMPTEUR = 0x00;
public static final byte INS_DECREMENTER_COMPTEUR = 0x01;
public static final byte INS_INTERROGER_COMPTEUR = 0x02;
public static final byte INS_INITIALISER_COMPTEUR = 0x03;
	
	private eCine() {
	}

	public static void install(byte bArray[], short bOffset, byte bLength)
			throws ISOException {
		new eCine().register();
	}

	public void process(APDU apdu) throws ISOException {
		// TODO Auto-generated method stub

	}

}
