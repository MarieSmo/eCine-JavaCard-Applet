package ecinepackage;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;

public class eCine extends Applet {
	public static final byte CLA_ECINE = (byte) 0x25;

	/* Instructions */
	public static final byte INS_BUY_TICKET = (byte) 0x00;
	public static final byte INS_GET_BALANCE = (byte) 0x01;
	public static final byte INS_REFUND_BALANCE = (byte) 0x02;
	public static final byte INS_UNLOCK_CARD = (byte) 0x03;
	public static final byte INS_ARCHIVE_TICKET = (byte) 0x04;
	public static final byte INS_VERIFY_PIN = (byte) 0x05;

	/* Error return value */
	public static final byte SW1_OK = (byte) 0x0A;
	public static final byte SW2_PURCHASE_OK = (byte) 0x01;
	public static final byte SW1_ERROR = (byte) 0x0E;
	public static final byte SW2_INSUFFICIENT_BALANCE = (byte) 0x01;
	public static final byte SW2_TICKET_MAX_AMOUNT_REACHED = (byte) 0x02;
	public static final byte SW2_VERIFICATION_FAILED = (byte) 0x03;
	public static final byte SW2_CARD_LOCKED = (byte) 0x04;

	
	private static byte balance;
	private static byte rewards;
	private static OwnerPIN userPIN;
	private static OwnerPIN adminPUK;

	private static Screening[] screenings;
	private static Screening immediateScreening;

	private eCine() {
		balance = (byte) 50;
		screenings = new Screening[]{null, null, null, null, null};

		byte[] pin = { 1, 2, 3, 4 };
		userPIN = new OwnerPIN((byte) 3, (byte) 4);
		userPIN.update(pin, (short) 0, (byte) 4);
	}

	public static void install(byte bArray[], short bOffset, byte bLength)
			throws ISOException {
		new eCine().register();
	}

	public void process(APDU apdu) throws ISOException {
		byte[] buffer = apdu.getBuffer();

		if (this.selectingApplet())
			return;

		if (buffer[ISO7816.OFFSET_CLA] != CLA_ECINE) {
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}

		switch (buffer[ISO7816.OFFSET_INS]) {
		case INS_BUY_TICKET:
			buyTicket(apdu);
			break;
		case INS_GET_BALANCE:
			buffer[0] = balance;
			apdu.setOutgoingAndSend((short) 0, (short) 1);
			break;
		case INS_REFUND_BALANCE:

			break;
		case INS_UNLOCK_CARD:
			apdu.setIncomingAndReceive();
			// compteur = buffer[ISO7816.OFFSET_CDATA];
			break;
		case INS_ARCHIVE_TICKET:
			apdu.setIncomingAndReceive();
			// compteur = buffer[ISO7816.OFFSET_CDATA];
			break;
		case INS_VERIFY_PIN:
			verify(apdu);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

	private void addScreening(Screening s) {
		for (short i = 0; i < 5; ++i) {
			if (screenings[i] == null) {
				screenings[i] = s;
				return;
			}
		}
		ISOException.throwIt(SW2_TICKET_MAX_AMOUNT_REACHED);
	}

	private void verify(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		// retrieve the PIN data for validation.
		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		// check pin
		if (userPIN.check(buffer, ISO7816.OFFSET_CDATA, byteRead) == false) {
			if (userPIN.getTriesRemaining() <= 0) {
				ISOException.throwIt(SW2_CARD_LOCKED);
			} else {
				ISOException.throwIt(SW2_VERIFICATION_FAILED);
			}
		}
	}

	private void buyTicket(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		// Lc byte denotes the number of bytes in the
		// data field of the command APDU
		byte numBytes = buffer[ISO7816.OFFSET_LC];

		// indicate that this APDU has incoming data
		// and receive data starting at the offset
		// ISO7816.OFFSET_CDATA following the 5 header
		// bytes.
		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		// it is an error if the number of data bytes
		// read does not match the number in Lc byte
		if (byteRead != 9)
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

		Screening newTicket = Screening.fromByteArray(buffer,
				ISO7816.OFFSET_CDATA);
		addScreening(newTicket);

		balance = (byte) (balance - newTicket.getPrice());
	}

}
