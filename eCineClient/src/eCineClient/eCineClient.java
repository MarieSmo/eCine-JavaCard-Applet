package eCineClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadT1Client;
import com.sun.javacard.apduio.CadTransportException;

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
		// 3 La La Land 6EUR 105min 10/01/2022 20:00
		screenings.put(3, new AbstractMap.SimpleEntry<String, Screening>(
				"La La Land", new Screening((short) 3, (byte) 6, (byte) 21,
						(byte) 10, (byte) 1, (byte) 22, (short) 1200)));
		// 4 Cars 5EUR 110min 11/01/2022 19:00
		screenings.put(4, new AbstractMap.SimpleEntry<String, Screening>(
				"Cars", new Screening((short) 4, (byte) 5, (byte) 19,
						(byte) 11, (byte) 1, (byte) 22, (short) 1140)));
		// 5 Avengers 12EUR 143min 12/01/2022 20:00
		screenings.put(5, new AbstractMap.SimpleEntry<String, Screening>(
				"Avengers", new Screening((short) 5, (byte) 12, (byte) 29,
						(byte) 12, (byte) 1, (byte) 22, (short) 1200)));
		// 6 Love Actually 5EUR 120min 11/01/2022 21:00
		screenings.put(6, new AbstractMap.SimpleEntry<String, Screening>(
				"Love Actually", new Screening((short) 6, (byte) 5, (byte) 24,
						(byte) 11, (byte) 1, (byte) 22, (short) 1260)));
		// 7 Shrek 5EUR 110min 13/01/2022 20:00
		screenings.put(7, new AbstractMap.SimpleEntry<String, Screening>(
				"Shrek", new Screening((short) 4, (byte) 25, (byte) 24,
						(byte) 13, (byte) 1, (byte) 22, (short) 1200)));

		return screenings;
	}
	
	//actual date, normally updated automatically
	static byte day = (byte) 7;
	static byte month = (byte) 1;
	static byte year = (byte) 22;
	static short time = 890;

	public static void dsplayScreenings(
			Map<Integer, AbstractMap.SimpleEntry<String, Screening>> screenings) {
		System.out.println(String.format("Nb: � %-25s Duration Date","Movie"));

		for (Entry<Integer, SimpleEntry<String, Screening>> item : screenings
				.entrySet()) {
			Integer index = item.getKey();
			String name = item.getValue().getKey();
			Screening s = item.getValue().getValue();
			System.out.println(String.format("%s: %02d %-25s %03dMins %02d/%02d/20%2d",index, s.getPrice(),name, s.getDuration() * 5, s.getDay(), s.getMonth(), s.getYear()));
		}
	}

	public static void main(String[] args) throws IOException,
			CadTransportException {
		
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

		Apdu apdu = new Apdu();

		/* Select the installer applet */
		cad.powerUp();
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
					.println("Erreur : impossible d'envoyer l'apdu à la Javacard");
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
					.println("Erreur : impossible d'envoyer l'apdu à la Javacard");
			System.exit(2);
		}
		if (apdu.getStatus() != 0x9000) {
			System.out.println("Erreur lors de la création de l'applet");
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
					.println("Erreur : impossible d'envoyer l'apdu à la Javacard");
			System.exit(2);
		}
		if (apdu.getStatus() != 0x9000) {
			System.out.println("Erreur lors de la sélection de l'applet");
			System.exit(1);
		}

		/* Menu principal */
		boolean fin = false;
		while (!fin) {
			System.out.println();
			System.out.println("POG Cinema");
			System.out.println("----------------------------");
			System.out.println();
			System.out.println("0 - Buy a ticket");
			System.out.println("1 - Display the balance");
			System.out.println("2 - Refund your balance");
			System.out.println("3 - Unlock your card");
			System.out.println("4 - Quit");
			System.out.println();
			System.out.println("Type Your Choice ?");

			Scanner scan = new Scanner(System.in);
			byte[] pin;
			byte[] data;
			try {
				//archivage des tickets
				apdu.command[Apdu.INS] = eCine.INS_ARCHIVE_TICKETS;
				data = new byte[4];
				data[0] = day;
				data[1] = month;
				
				cad.exchangeApdu(apdu);
				manageError(apdu.getStatus());

				int choix = Integer.parseInt(scan.nextLine());
				while (!(choix >= 0 && choix <= 5)) {
					choix = Integer.parseInt(scan.nextLine());
				}

				apdu = new Apdu();
				apdu.command[Apdu.CLA] = eCine.CLA_ECINE;
				apdu.command[Apdu.P1] = 0x04;
				apdu.command[Apdu.P2] = 0x00;

				switch (choix) {
				case 0:
				
					if (manageError(verifyPin(apdu, cad))) {
						apdu.command[Apdu.INS] = eCine.INS_BUY_TICKET;
						dsplayScreenings(screenings);
						int screeningChoice = Integer.parseInt(scan.nextLine());
						data = new byte[9];
						data = screenings.get(screeningChoice).getValue().toByteArray();

						apdu.setDataIn(data);
						cad.exchangeApdu(apdu);
						manageError(apdu.getStatus());
					}
					break;

				case 1:
					apdu.command[Apdu.INS] = eCine.INS_GET_BALANCE;
					cad.exchangeApdu(apdu);
					if (manageError(apdu.getStatus())) {
						System.out.println("Balance : " + apdu.dataOut[0]);
					}
					break;

				case 2:
					if (manageError(verifyPin(apdu, cad))) {
						apdu.command[Apdu.INS] = eCine.INS_REFUND_BALANCE;
						System.out.println("Enter the amount you want to credit:");
						int refund = Integer.parseInt(scan.nextLine()); 
						data = new byte[1];
						data[0] = (byte) refund;
						apdu.setDataIn(data);
						cad.exchangeApdu(apdu);
						manageError(apdu.getStatus());
					}
					break;

				case 3:
					apdu.command[Apdu.INS] = eCine.INS_UNLOCK_CARD;
					System.out.println("Please enter admin PUK:");
					pin = readPin();
					apdu.setDataIn(pin);
					cad.exchangeApdu(apdu);
					manageError(apdu.getStatus());
					break;

				case 4:
					apdu.command[Apdu.INS] = eCine.INS_ARCHIVE_TICKETS;

					break;

				case 5:
					fin = true;
					break;
				}
			} catch (IncorrectPinFormatException ignored) {
				System.err.println("Incorrect Pin Format, aborting");
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

	public static byte[] readPin() throws IncorrectPinFormatException {
		Scanner scan = new Scanner(System.in).useDelimiter("");
		byte[] pin = new byte[4];
		for(int i=0; i < 4; i++) {
			if(scan.hasNextInt())
				pin[i] = (byte) scan.nextInt();
			else 
				throw new IncorrectPinFormatException();
		}
		scan.nextLine();
		return pin;
	}
	
	public static int verifyPin(Apdu apdu, CadT1Client cad) throws IOException, CadTransportException, IncorrectPinFormatException {
		apdu.command[Apdu.INS] = eCine.INS_VERIFY_PIN;
		System.out.println("Please enter your PIN:");
		byte[] pin = readPin();
		apdu.setDataIn(pin);
		cad.exchangeApdu(apdu);
		return apdu.getStatus();
	}
	
	public static byte[] dateToArg() {
		byte[] data = new byte[5];
		data[0] = day;
		data[1] = month;
		data[2] = year;
		data[3] = (byte) (time >> 8);
		data[4] = (byte) (time & 0xFF);
		return data;		
	}

	public static boolean manageError(int status) {
		switch (status) {
		case 0x9000:
			System.out.println("Ok");
			return true;
		case eCine.SW2_CARD_LOCKED:
			System.err
					.println("Your card is locked. PLease call an admin to unlock it.");
			break;
		case eCine.SW2_VERIFICATION_FAILED:
			System.err.println("Invalid PIN. Please try again");
			break;
		default:
			System.err.println("Error : " + status);
			break;
		}
		return false;
	}
}