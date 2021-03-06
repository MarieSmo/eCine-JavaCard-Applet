package eCineClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;






import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadT1Client;
import com.sun.javacard.apduio.CadTransportException;

import ecinepackage.Logger;
import ecinepackage.Purchase;
import ecinepackage.eCine;
import ecinepackage.Screening;

public class eCineClient {

	public static Map<Integer, AbstractMap.SimpleEntry<String, Screening>> initMovies() {
		/* Movie library */
		Map<Integer, AbstractMap.SimpleEntry<String, Screening>> screenings = new HashMap<Integer, AbstractMap.SimpleEntry<String, Screening>>();
		// 1 Lord of The Rings 10EUR 720min 10/01/2022 02:00
		screenings.put(1, new AbstractMap.SimpleEntry<String, Screening>(
				"The Lord of the Rings",
				new Screening((short) 1, (byte) 10, (byte) 127, (byte) 10,
						(byte) 1, (byte) 22, (short) 120)));
		// 2 Matrix 5EUR 100min 05/07/2022 20:00
		screenings.put(2, new AbstractMap.SimpleEntry<String, Screening>(
				"Matrix", new Screening((short) 2, (byte) 5, (byte) 20,
						(byte) 5, (byte) 7, (byte) 22, (short) 1200)));
		// 3 La La Land 6EUR 105min 10/12/2022 20:00
		screenings.put(3, new AbstractMap.SimpleEntry<String, Screening>(
				"La La Land", new Screening((short) 3, (byte) 6, (byte) 21,
						(byte) 10, (byte) 12, (byte) 22, (short) 1200)));
		// 4 Cars 5EUR 110min 11/12/2022 19:00
		screenings.put(4, new AbstractMap.SimpleEntry<String, Screening>(
				"Cars", new Screening((short) 4, (byte) 5, (byte) 24,
						(byte) 11, (byte) 12, (byte) 22, (short) 1140)));
		// 5 Avengers 12EUR 143min 12/12/2022 20:00
		screenings.put(5, new AbstractMap.SimpleEntry<String, Screening>(
				"Avengers", new Screening((short) 5, (byte) 12, (byte) 29,
						(byte) 12, (byte) 12, (byte) 22, (short) 1200)));
		// 6 Love Actually 5EUR 120min 11/12/2022 21:00
		screenings.put(6, new AbstractMap.SimpleEntry<String, Screening>(
				"Love Actually", new Screening((short) 6, (byte) 5, (byte) 24,
						(byte) 11, (byte) 12, (byte) 22, (short) 1260)));
		// 7 Shrek 5EUR 110min 13/12/2022 20:00
		screenings.put(7, new AbstractMap.SimpleEntry<String, Screening>(
				"Shrek", new Screening((short) 7, (byte) 25, (byte) 24,
						(byte) 13, (byte) 12, (byte) 22, (short) 1200)));
		// 8 Cars 2 5EUR 120min 11/12/2022 20:55
		screenings.put(8, new AbstractMap.SimpleEntry<String, Screening>(
				"Cars 2", new Screening((short) 8, (byte) 5, (byte) 24,
						(byte) 11, (byte) 12, (byte) 22, (short) 1260)));
		// 9 Cars 3 5EUR 120min 11/12/2022 17:05
		screenings.put(9, new AbstractMap.SimpleEntry<String, Screening>(
				"Cars 3", new Screening((short) 9, (byte) 5, (byte) 24,
						(byte) 11, (byte) 12, (byte) 22, (short) 1025)));
		// 10 Cars 4 5EUR 120min 11/03/2022 17:05
		screenings.put(10, new AbstractMap.SimpleEntry<String, Screening>(
						"Cars 4", new Screening((short) 10, (byte) 5, (byte) 24,
								(byte) 11, (byte) 3, (byte) 22, (short) 1025)));
		return screenings;
	}

	// actual date, normally updated automatically
	static byte day;
	static byte month;
	static byte year;
	static short time;

	public static void main(String[] args) throws IOException,
			CadTransportException {
		
		// Initialize Movies List
		Map<Integer, AbstractMap.SimpleEntry<String, Screening>> screenings = initMovies();

		/* Connexion a la Javacard */
		CadT1Client cad;
		Socket sckCarte;
		try {
			sckCarte = new Socket("localhost", 9025);
			sckCarte.setTcpNoDelay(true);
			BufferedInputStream input = new BufferedInputStream(
					sckCarte.getInputStream());
			BufferedOutputStream output = new BufferedOutputStream(
					sckCarte.getOutputStream());
			cad = new CadT1Client(input, output);
		} catch (Exception e) {
			System.out
					.println("Erreur : impossible de se connecter a la Javacard");
			return;
		}

		/* Select the installer applet */
		cad.powerUp();
		Apdu apdu = new Apdu();
		apdu.command[Apdu.CLA] = 0x00;
		apdu.command[Apdu.INS] = (byte) 0xA4;
		apdu.command[Apdu.P1] = 0x04;
		apdu.command[Apdu.P2] = 0x00;
		byte[] appletAID1 = { (byte) 0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01,
				0x08, 0x01 };
		apdu.setDataIn(appletAID1);
		System.out.println(apdu.toString());
		try {
			cad.exchangeApdu(apdu);
		} catch (Exception e) {
			System.out
					.println("Erreur : impossible d'envoyer l'apdu ?? la Javacard");
			System.exit(2);
		}
		if (apdu.getStatus() != 0x9000) {
			System.out.println("Erreur lors de l'installation de l'applet");
			System.exit(1);
		}

		/* create applet */
		apdu = new Apdu();
		apdu.command[Apdu.CLA] = (byte) 0x80;
		apdu.command[Apdu.INS] = (byte) 0xB8;
		apdu.command[Apdu.P1] = 0x00;
		apdu.command[Apdu.P2] = 0x00;
		byte[] appletAID2 = { (byte) 0x0B, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
				0x07, 0x08, 0x09, 0x00, 0x00, 0x00 };
		apdu.setDataIn(appletAID2);
		System.out.println(apdu.toString());
		try {
			cad.exchangeApdu(apdu);
		} catch (Exception e) {
			System.out
					.println("Erreur : impossible d'envoyer l'apdu ?? la Javacard");
			System.exit(2);
		}
		if (apdu.getStatus() != 0x9000) {
			System.out.println("Erreur lors de la cr??ation de l'applet");
			System.exit(1);
		}

		/* Selection de l'applet */
		cad.powerUp();
		apdu = new Apdu();
		apdu.command[Apdu.CLA] = 0x00;
		apdu.command[Apdu.INS] = (byte) 0xA4;
		apdu.command[Apdu.P1] = 0x04;
		apdu.command[Apdu.P2] = 0x00;
		byte[] appletAID = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
				0x09, 0x00, 0x00 };
		apdu.setDataIn(appletAID);
		System.out.println(apdu.toString());
		try {
			cad.exchangeApdu(apdu);
		} catch (Exception e) {
			System.out
					.println("Erreur : impossible d'envoyer l'apdu ?? la Javacard");
			System.exit(2);
		}
		if (apdu.getStatus() != 0x9000) {
			System.out.println("Erreur lors de la s??lection de l'applet");
			System.exit(1);
		}
		// Update Time
		setDateTime();
		// Archive cards
		archiveTickets(cad, null);
		manageError(apdu.getStatus(), false);
		System.out.println("Today Date : " + year + "/"  + month + "/" + day + " " + time);

		/* Menu principal */
		boolean fin = false;

		while (!fin) {
			Scanner scan = new Scanner(System.in);
			byte[] pin;
			byte[] data;
			try {

				// Init Connection
				apdu = new Apdu();
				apdu.command[Apdu.CLA] = eCine.CLA_ECINE;
				apdu.command[Apdu.P1] = 0x04;
				apdu.command[Apdu.P2] = 0x00;

				// Demande De Selection
				System.out.println();
				System.out.println("POG Cinema");
				System.out.println("----------------------------");
				System.out.println();
				System.out.println("0 - Buy a ticket");
				System.out.println("1 - Display the balance");
				System.out.println("2 - Refund your balance");
				System.out.println("3 - Unlock your card");
				System.out.println("4 - Archive");
				System.out.println("5 - Get Logs");
				System.out.println("6 - Display Archived Movies");
				System.out.println("7 - Quit");
				System.out.println();
				System.out.println("Type Your Choice ?");

				int choix = Integer.parseInt(scan.nextLine());

				switch (choix) {
				case 0: //INS_BUY_TICKET
					if (manageError(verifyPin(apdu, cad, false), false)) {
						apdu.command[Apdu.INS] = eCine.INS_BUY_TICKET;
						displayScreenings(screenings);
						int screeningChoice = Integer.parseInt(scan.nextLine());
						data = new byte[Purchase.ELEMENT_COUNT];
						data = (new Purchase(screenings.get(screeningChoice).getValue(), day, month, year, time )).toByteArray();
						apdu.setDataIn(data);
						cad.exchangeApdu(apdu);
						manageError(apdu.getStatus(), true);
					}
					break;

				case 1: //INS_GET_BALANCE
					apdu.command[Apdu.INS] = eCine.INS_GET_BALANCE;
					cad.exchangeApdu(apdu);
					if (manageError(apdu.getStatus(), false)) {
						System.out.println("Balance : " + apdu.dataOut[0]);
					}
					break;
				case 2: //INS_REFUND_BALANCE
					if (manageError(verifyPin(apdu, cad, false), false)) {
						apdu.command[Apdu.INS] = eCine.INS_REFUND_BALANCE;
						System.out
								.println("Enter the amount you want to credit:");
						int refund = Integer.parseInt(scan.nextLine());
						data = new byte[1];
						data[0] = (byte) refund;
						apdu.setDataIn(data);
						cad.exchangeApdu(apdu);
						manageError(apdu.getStatus(), true);
					}
					break;

				case 3: //INS_UNLOCK_CARD
					apdu.command[Apdu.INS] = eCine.INS_UNLOCK_CARD;
					System.out.println("Please enter admin PUK:");
					pin = readPin();
					apdu.setDataIn(pin);
					cad.exchangeApdu(apdu);
					manageError(apdu.getStatus(), true);
					break;

				case 4://INS_ARCHIVE_TICKETS
					apdu.command[Apdu.INS] = eCine.INS_ARCHIVE_TICKETS;
					archiveTickets(cad, apdu);
					manageError(apdu.getStatus(), true);
					break;
				case 5://INS_GET_LOGS
					if (manageError(verifyPin(apdu, cad, true), false)) {
						apdu.command[Apdu.INS] = eCine.INS_GET_LOGS;
						cad.exchangeApdu(apdu);
						displayLogs(apdu.dataOut);
						manageError(apdu.getStatus(), false);
					}
					break;
				case 6://INS_GET_ARCHIVED_TICKETS
					if (manageError(verifyPin(apdu, cad, false), false)) {
						apdu.command[Apdu.INS] = eCine.INS_GET_ARCHIVED_TICKETS;
						cad.exchangeApdu(apdu);
						displayMovieLogs(apdu.dataOut, screenings);
						manageError(apdu.getStatus(), false);
					}
					break;
				case 7://Quit
					fin = true;
					break;
				default:
					throw new InvalidSelectionException();
				}
			} catch (InvalidSelectionException | NumberFormatException ignored) {
				System.err.println("Invalid Selection");
			} catch (IncorrectPinFormatException ignored) {
				System.err
						.println("Incorrect Pin Format, Returning you to the Main Menu");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/* Mise hors tension de la carte */
		try {
			cad.powerDown();
		} catch (Exception e) {
			System.out.println("Error during powerdown command");
			return;
		}
	}
	/* 
	 * Display Formated Screenings List
	 */
	public static void displayScreenings(
			Map<Integer, AbstractMap.SimpleEntry<String, Screening>> screenings) {
		System.out.println(String
				.format("Nb: ??? %-25s Duration Date", "Movie"));

		for (Entry<Integer, SimpleEntry<String, Screening>> item : screenings
				.entrySet()) {
			Integer index = item.getKey();
			String name = item.getValue().getKey();
			Screening s = item.getValue().getValue();
			System.out.println(String.format(
					"%s: %02d %-25s %03dMins %02d:%02d %02d/%02d/20%2d", index,
					s.getPrice(), name, s.getDuration() * 5, 
					s.getTime()/60,
					s.getTime()%60,
					s.getDay(),
					s.getMonth(), s.getYear()));
		}
	}
	
	/* 
	 * Get Current Time And Date And Update Variables
	 */
	public static void setDateTime() {
		LocalDateTime now = LocalDateTime.now(); 
		year = ( byte) (now.getYear() % 2000);
		month =( byte) now.getMonthValue();
		day = ( byte) now.getDayOfMonth();
		time = (short) ((now.getHour() * 60) + now.getMinute());
	}
	
	/* 
	 * Display Formated Movie Archive
	 */
	public static void displayMovieLogs(byte[] data, Map<Integer, AbstractMap.SimpleEntry<String, Screening>> screenings){
		byte currentIndex = data[0];
		System.out.println("Archived Movie current index : " + (currentIndex + 1));
		for(int i=1; i < data.length ; i++) {
			if(data[i] <= 0) continue;
			if(i == currentIndex + 1) System.out.print("> ");
			Integer id = Integer.valueOf(data[i]);
			SimpleEntry<String, Screening> item = screenings.get(id);
			System.out.print(item.getValue().getIDMovie() + " " + item.getKey() + "\n");
		}
	}
	
	/* 
	 * Display Formated Logs
	 */
	public static void displayLogs(byte[] data){
		byte currentElement = 0;
		byte loggerIndex = data[0];
		System.out.println("Log current index : " + loggerIndex);
		for(int i=1; i < data.length ; i+=Logger.MESSAGE_SIZE) {
			if(loggerIndex == currentElement) System.out.print("> ");
			switch (data[i]) {
			case eCine.INS_BUY_TICKET:
				System.out.printf("Purchase: ID: %02d Price:%02d PaymentMethod:%02d\n", (short) (data[i+1] << 8 | data[i+2] & 0xFF) , data[i+3], data[i+4]);
				break;
			case eCine.INS_REFUND_BALANCE:
				System.out.printf("TopUp   : Amount: %02d newBalance: %02d\n", data[i+1] ,data[i+2]);
				break;
			case (byte) 0xFF:
				System.out.printf("Aborted\n");
				break;
			default:
				System.out.printf("Unknown : 0x%02X 0x%02X 0x%02X 0x%02X 0x%02X 0x%02X\n", data[i], data[i+1], data[i+2], data[i+3], data[i+4], data[i+5]);
			}
			currentElement++;

		}
	}

	/* 
	 * Send archive old tickets command
	 */
	public static boolean archiveTickets(CadT1Client cad, Apdu apdu) {
		// Init Connection
		try {
			if(apdu == null){
				apdu = new Apdu();
				apdu.command[Apdu.CLA] = eCine.CLA_ECINE;
				apdu.command[Apdu.P1] = 0x04;
				apdu.command[Apdu.P2] = 0x00;
			}
			// archivage des tickets
			apdu.command[Apdu.INS] = eCine.INS_ARCHIVE_TICKETS;
			byte[] data = new byte[5];
			data[0] = day;
			data[1] = month;
			data[2] = year;
			data[3] = (byte) (time >> 8);
			data[4] = (byte) (time & 0xFF);
			apdu.setDataIn(data);
			cad.exchangeApdu(apdu);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/* 
	 * Read a 4 digit number from the console
	 * 
	 * All Extra input is ignored
	 */
	public static byte[] readPin() throws IncorrectPinFormatException {
		Scanner scan = new Scanner(System.in).useDelimiter("");
		byte[] pin = new byte[4];
		for (int i = 0; i < 4; i++) {
			if (scan.hasNextInt())
				pin[i] = (byte) scan.nextInt();
			else
				throw new IncorrectPinFormatException();
		}
		scan.nextLine();
		return pin;
	}

	/* 
	 * Send Verify pin command to the card
	 */
	public static int verifyPin(Apdu apdu, CadT1Client cad, boolean admin) throws IOException,
			CadTransportException, IncorrectPinFormatException {
		if (!admin) {
			apdu.command[Apdu.INS] = eCine.INS_VERIFY_PIN;
			System.out.println("Please enter your PIN:");
		} else {
			apdu.command[Apdu.INS] = eCine.INS_VERIFY_PUK;
			System.out.println("Please enter admin PUK:");
		}
		byte[] pin = readPin();
		apdu.setDataIn(pin);
		cad.exchangeApdu(apdu);
		return apdu.getStatus();
	}
	
	/* 
	 * Format Error Messages
	 */
	public static boolean manageError(int status, boolean notify) {
		switch (status) {
		case 0x9000:
			if(notify)
				System.out.println("Operation Successful");
			return true;
		case eCine.SW2_CARD_LOCKED:
			System.err.println("Your card is locked. PLease call an admin to unlock it.");
			break;
		case eCine.SW2_VERIFICATION_FAILED:
			System.err.println("Invalid PIN. Please try again");
			break;
		case eCine.SW2_INSUFFICIENT_BALANCE:
			System.err.println("Insfficiant Funds");
			break;
		case eCine.SW2_DATE_CONFLICT :
			System.err.println("Date Conflict");
			break;
		case eCine.SW2_TICKET_MAX_AMOUNT_REACHED:
			System.err.println("You already bought 5 tickets");
			break;
		case eCine.SW2_INVALID_REFUND_AMOUNT:
			System.err.println("The amount of your refund is too high");
			break;
		case eCine.SW2_EXCEED_MAXIMUM_BALANCE:
			System.err.println("You reached the maximum balance amount");
			break;
		case eCine.SW2_MAX_TRANSACTION_AMOUNT_REACHED :
			System.err.println("You reached the max amount of transactions. You card is now unusable");
			break;
		case eCine.SW2_CARD_DEAD :
			System.err.println("Your card is dead");
			break;
		default:
			System.err.println("Error : " + status);
			break;
		}
		return false;
	}
}