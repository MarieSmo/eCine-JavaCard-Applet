package ecinepackage;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;
import javacard.framework.Util;

public class eCine extends Applet {
	public static final byte CLA_ECINE = (byte) 0x25;

	/* Instructions */
	public static final byte INS_BUY_TICKET = (byte) 0x01;
	public static final byte INS_GET_BALANCE = (byte) 0x02;
	public static final byte INS_REFUND_BALANCE = (byte) 0x03;
	public static final byte INS_UNLOCK_CARD = (byte) 0x04;
	public static final byte INS_ARCHIVE_TICKETS = (byte) 0x05;
	public static final byte INS_VERIFY_PIN = (byte) 0x06;
	public static final byte INS_GET_LOGS = (byte) 0x07;
	public static final byte INS_VERIFY_PUK = (byte) 0x08;
	public static final byte INS_GET_ARCHIVED_TICKETS = (byte) 0x09;

	/* Error return value */
	public static final byte SW1_OK = (byte) 0x0A;
	public static final byte SW2_PURCHASE_OK = (byte) 0x01;
	public static final byte SW1_ERROR = (byte) 0x0E;
	public static final byte SW2_INSUFFICIENT_BALANCE = (byte) 0x01;
	public static final byte SW2_TICKET_MAX_AMOUNT_REACHED = (byte) 0x02;
	public static final byte SW2_VERIFICATION_FAILED = (byte) 0x03;
	public static final byte SW2_CARD_LOCKED = (byte) 0x04;
	public static final byte SW2_INVALID_REFUND_AMOUNT = (byte) 0x05;
	public static final byte SW2_EXCEED_MAXIMUM_BALANCE = (byte) 0x06;
	public static final byte SW2_MAX_TRANSACTION_AMOUNT_REACHED = (byte) 0x07;
	public static final byte SW2_CARD_DEAD = (byte) 0x08;
	public static final byte SW2_DATE_CONFLICT = (byte) 0x09;

	// Operational Variables
	private static byte balance;
	private static byte rewards;
	public static byte transactions;
	private static OwnerPIN userPIN;
	private static OwnerPIN adminPUK;
	/* Screening variables */
	private static Screening[] screenings;
	private static Screening immediateScreening;
	
	// Loggers
	private static Logger logger;
	private static PastScreenings pastScreenings;

	/* Operational Consts */
	public static final byte MAX_REFUND_AMOUNT = (byte) 50;
	public static final byte MAX_BALANCE_AMOUNT = (byte) 100;
	public static final byte MAX_TRANSACTIONS = (byte) 127;
	public static final byte MAX_SCREENINGS_COUNT = (byte) 5;

	public static final byte [] ADMIN_PUK = { 0, 0, 0, 1 };
	public static final byte [] USER_PIN = { 1, 2, 3, 4 };

	private eCine() {
		balance = (byte) 0;
		transactions = (byte) 0;
		screenings = new Screening[MAX_SCREENINGS_COUNT];
		pastScreenings = new PastScreenings();
		logger = new Logger();
		userPIN = new OwnerPIN((byte) 3, (byte) 4);
		userPIN.update(USER_PIN, (short) 0, (byte) 4);
		adminPUK = new OwnerPIN((byte) 3, (byte) 4);
		adminPUK.update(ADMIN_PUK, (short) 0, (byte) 4);
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
		if (transactions == MAX_TRANSACTIONS) {
			ISOException.throwIt(SW2_MAX_TRANSACTION_AMOUNT_REACHED);
		}
		try {

			switch (buffer[ISO7816.OFFSET_INS]) {
			case INS_BUY_TICKET:
				buyTicket(apdu);
				break;
			case INS_GET_BALANCE:
				buffer[0] = balance;
				apdu.setOutgoingAndSend((short) 0, (short) 1);
				break;
			case INS_REFUND_BALANCE:
				refundBalance(apdu);
				break;
			case INS_UNLOCK_CARD:
				if (userPIN.getTriesRemaining() <= 0) {
					unlockCard(apdu);
				}
				break;
			case INS_ARCHIVE_TICKETS:
				archiveOldTickets(apdu);
				break;
			case INS_VERIFY_PIN:
				verify(apdu);
				break;
			case INS_VERIFY_PUK:
				verifyPuk(apdu);
				break;
			case INS_GET_LOGS:
				if (adminPUK.isValidated() == false)
					ISOException.throwIt(SW2_VERIFICATION_FAILED);
				Util.arrayCopyNonAtomic(logger.toByteArray(), (short) 0,
						buffer, (short) 0, logger.getTotalSize());
				apdu.setOutgoingAndSend((short) 0, logger.getTotalSize());
				break;
			case INS_GET_ARCHIVED_TICKETS:
				if (userPIN.isValidated() == false)
					ISOException.throwIt(SW2_VERIFICATION_FAILED);
				Util.arrayCopyNonAtomic(pastScreenings.toByteArray(),
						(short) 0, buffer, (short) 0,
						pastScreenings.getTotalSize());
				apdu.setOutgoingAndSend((short) 0,
						pastScreenings.getTotalSize());
				break;

			default:
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
			}

		} catch (ISOException exception) {
			logger.logAbort();
			throw exception;
		}
		logger.commit();
		transactions++;
	}

	// -------------------- HANDLE Balance --------------------------------
	private void refundBalance(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		byte numBytes = buffer[ISO7816.OFFSET_LC];
		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		if (userPIN.isValidated() == false)
			ISOException.throwIt(SW2_VERIFICATION_FAILED);

		if (byteRead != 1)
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

		byte refund = buffer[ISO7816.OFFSET_CDATA];

		logger.logRefund(refund, (byte) (balance + refund));

		if ((refund > MAX_REFUND_AMOUNT) || (refund < 0))
			ISOException.throwIt(SW2_INVALID_REFUND_AMOUNT);

		if ((balance + refund) > MAX_BALANCE_AMOUNT)
			ISOException.throwIt(SW2_EXCEED_MAXIMUM_BALANCE);

		balance = (byte) (balance + refund);
	}

	// -------------------- HANDLE Balance --------------------------------
	// -------------------- HANDLE PIN/PUK --------------------------------

	private void verifyPuk(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		// retrieve the PIN data for validation.
		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		// check pin
		if (adminPUK.check(buffer, ISO7816.OFFSET_CDATA, byteRead) == false) {
			if (adminPUK.getTriesRemaining() <= 0) {
				ISOException.throwIt(SW2_CARD_LOCKED);
			} else {
				ISOException.throwIt(SW2_VERIFICATION_FAILED);
			}
		}
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

	private void unlockCard(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		// retrieve the PUK data for validation.
		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		// check pin
		if (adminPUK.check(buffer, ISO7816.OFFSET_CDATA, byteRead) == false) {
			if (adminPUK.getTriesRemaining() <= 0) {
				ISOException.throwIt(SW2_CARD_DEAD);
			} else {
				ISOException.throwIt(SW2_VERIFICATION_FAILED);
			}
		}
		userPIN.resetAndUnblock();
	}

	// -------------------- HANDLE PIN/PUK --------------------------------
	// -------------------- HANDLE Archiving --------------------------------
	private void compareAndArchive(byte tdDay, byte tdMonth, byte tdYear,
			short tdTime) {
		// If any screenings endtime is before now archive it
		for (short i = 0; i < MAX_SCREENINGS_COUNT; ++i) {
			if (screenings[i] != null) {
				byte sDay = screenings[i].getDay();
				byte sMonth = screenings[i].getMonth();
				byte sYear = screenings[i].getYear();
				short eTime = (short) (screenings[i].getTime() + (screenings[i]
						.getDuration() * Screening.DURATION_UNIT));
				if (sYear > tdYear)
					continue;
				else if (sYear < tdYear) {
					pastScreenings.addScreening((byte) screenings[i]
							.getIDMovie());
					screenings[i] = null;

				} else {
					if (sMonth > tdMonth)
						continue;
					else if (sMonth < tdMonth) {
						pastScreenings.addScreening((byte) screenings[i]
								.getIDMovie());
						screenings[i] = null;

					} else {
						if (sDay > tdDay)
							continue;
						else if (sDay < tdDay) {
							pastScreenings.addScreening((byte) screenings[i]
									.getIDMovie());
							screenings[i] = null;
						} else {
							if (eTime > tdTime)
								continue;
							else if (eTime <= tdTime) {
								pastScreenings
										.addScreening((byte) screenings[i]
												.getIDMovie());
								screenings[i] = null;
							}
						}
					}
				}
			}
		}
	}

	private void archiveOldTickets(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		byte numBytes = buffer[ISO7816.OFFSET_LC];
		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		if (byteRead != 5)
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

		byte day = buffer[ISO7816.OFFSET_CDATA];
		byte month = buffer[ISO7816.OFFSET_CDATA + 1];
		byte year = buffer[ISO7816.OFFSET_CDATA + 2];
		short time = (short) (buffer[ISO7816.OFFSET_CDATA + 3] << 8 | buffer[ISO7816.OFFSET_CDATA + 4] & 0xFF);
		compareAndArchive(day, month, year, time);
	}

	// -------------------- HANDLE Archiving --------------------------------
	// -------------------- HANDLE TICKETS --------------------------------

	private boolean isBetween(short fdStart, byte fdDuration, short sdStart,
			byte sdDuration) {
		short fdEnd = (short) (fdStart + (fdDuration * Screening.DURATION_UNIT));
		short sdEnd = (short) (sdStart + (sdDuration * Screening.DURATION_UNIT));
		return (fdStart <= sdEnd && sdStart <= fdEnd);
	}

	private boolean checkOverlaping(Screening s1, Screening s2) {
		if (s1.getYear() == s2.getYear())
			if (s1.getMonth() == s2.getMonth())
				if (s1.getDay() == s2.getDay())
					return isBetween(s1.getTime(), s1.getDuration(),
							s2.getTime(), s2.getDuration());
		return false;
	}

	private void addScreening(Purchase p) {
		short insertIndex = -1;
		// Check overlap
		if (immediateScreening != null
				&& checkOverlaping(immediateScreening, p.getScreening())) {
			ISOException.throwIt(SW2_DATE_CONFLICT);
		}
		for (short i = 0; i < MAX_SCREENINGS_COUNT; ++i) {
			if (screenings[i] == null && insertIndex == -1) {
				insertIndex = i;
			} else if (screenings[i] != null
					&& checkOverlaping(screenings[i], p.getScreening())) {
				ISOException.throwIt(SW2_DATE_CONFLICT);
			}
		}
		// Check if is immediate
		if (immediateScreening == null && isImmediateScreening(p)) {
			immediateScreening = p.getScreening();
			return;
		}
		// If an empty spot is found insert
		if (insertIndex != -1) {
			screenings[insertIndex] = p.getScreening();
			return;
		}
		ISOException.throwIt(SW2_TICKET_MAX_AMOUNT_REACHED);
	}

	private boolean isImmediateScreening(Purchase purchase) {
		if (purchase.getScreening().getYear() == purchase.getTdYear())
			if (purchase.getScreening().getMonth() == purchase.getTdMonth())
				if (purchase.getScreening().getDay() == purchase.getTdDay()) {
					// Screening started 30 min before or Screening starts in 1h
					return (purchase.getTdTime()
							- purchase.getScreening().getTime() <= 30 || (purchase
							.getTdTime() - purchase.getScreening().getTime() <= -60));
				}
		return false;
	}
	
	private boolean isBeforeNow(Purchase purchase) {
			byte sDay = purchase.getScreening().getDay();
			byte sMonth = purchase.getScreening().getMonth();
			byte sYear = purchase.getScreening().getYear();
			short eTime = (short) (purchase.getScreening().getTime() + (purchase.getScreening()
					.getDuration() * Screening.DURATION_UNIT));
			byte tdYear = purchase.getTdYear();
			byte tdMonth = purchase.getTdMonth();
			byte tdDay = purchase.getTdDay();
			short tdTime = purchase.getTdTime();

			if (sYear > tdYear)
				return false;
			else if (sYear < tdYear) {
				return true;
			} else {
				if (sMonth > tdMonth)
					return false;
				else if (sMonth < tdMonth) {
					return true;
				} else {
					if (sDay > tdDay)
						return false;
					else if (sDay < tdDay) {
						return true;

					} else {
						if (eTime > tdTime)
							return false;
						else if (eTime <= tdTime) {
							return true;
						}
					}
				}
			}
		return false;
	}

	private void buyTicket(APDU apdu) {
		if (userPIN.isValidated() == false)
			ISOException.throwIt(SW2_VERIFICATION_FAILED);

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
		if (byteRead != Purchase.ELEMENT_COUNT)
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

		Purchase newPurchase = Purchase.fromByteArray(buffer,
				ISO7816.OFFSET_CDATA);

		Screening newTicket = newPurchase.getScreening();

		// LOG
		if (rewards == 50) {
			logger.logTicketPurchase(newTicket.getIDMovie(),
					newTicket.getPrice(), Logger.BUY_TICKET_REWARD);
		} else {
			logger.logTicketPurchase(newTicket.getIDMovie(),
					newTicket.getPrice(), Logger.BUY_TICKET_BALANCE);
		}
		
		if(isBeforeNow(newPurchase))
			ISOException.throwIt(SW2_DATE_CONFLICT);

		if ((balance - newTicket.getPrice()) < 0)
			ISOException.throwIt(SW2_INSUFFICIENT_BALANCE);

		// Handle immediate Sessions
		addScreening(newPurchase);
		if (rewards == 50) {
			rewards = 0;
		} else {
			rewards += 5;
			balance = (byte) (balance - newTicket.getPrice());
		}
	}
	// -------------------- HANDLE TICKETS --------------------------------
}
