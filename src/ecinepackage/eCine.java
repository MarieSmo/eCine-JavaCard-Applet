package ecinepackage;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;

public class eCine extends Applet {
	public static final byte CLA_ECINE = (byte) 0x25;
	 
	/* Instructions */
	public static final byte INS_BUY_TICKET = 0x00;
	public static final byte INS_GET_BALANCE = 0x01;
	public static final byte INS_REFUND_BALANCE = 0x02;
	public static final byte INS_UNLOCK_CARD = 0x03;
	public static final byte INS_ARCHIVE_TICKET = 0x04;
	
	/* Error return value */
	public static final byte SW1_OK = 0x0A;
	public static final byte SW2_PURCHASE_OK = 0x01;
	public static final byte SW1_ERROR = 0x0E;
	public static final byte SW2_INSUFFICIENT_BALANCE = 0x01;
	public static final byte SW2_TICKET_MAX_AMOUNT_REACHED = 0x02;
	
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
