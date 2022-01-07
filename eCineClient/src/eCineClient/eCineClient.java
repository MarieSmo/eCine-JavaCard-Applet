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
			System.out.println("Application cliente Javacard");
			System.out.println("----------------------------");
			System.out.println();
			System.out.println("0 - Buy a ticket");
			System.out.println("1 - Display the balance");
			System.out.println("3 - Decrementer le compteur");
			System.out.println("4 - Reinitialiser le compteur");
			System.out.println("5 - Quitter");
			System.out.println();
			System.out.println("Votre choix ?");

			Scanner scan = new Scanner(System.in);
			try {

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
					apdu.command[Apdu.INS] = eCine.INS_VERIFY_PIN;
					System.out.println("Please enter your PIN:");
					byte[] pin = readPin();
					apdu.setDataIn(pin);
					cad.exchangeApdu(apdu);

					if (manageError(apdu.getStatus())) {
						apdu.command[Apdu.INS] = eCine.INS_BUY_TICKET;
						for (Entry<Integer, SimpleEntry<String, Screening>> item : screenings
								.entrySet()) {
							System.out.println("For "
									+ item.getValue().getKey() + " press "
									+ item.getKey());
						}
						int screeningChoice = Integer.parseInt(scan.nextLine());
						byte[] data = screenings.get(screeningChoice)
								.getValue().toByteArray();

						apdu.setDataIn(data);
						cad.exchangeApdu(apdu);
						manageError(apdu.getStatus());
					}
					break;

				case 1:
					apdu.command[Apdu.INS] = eCine.INS_GET_BALANCE;
					cad.exchangeApdu(apdu);
					if (apdu.getStatus() != 0x9000) {
						System.out.println(apdu.getStatus());
						System.out
								.println("Erreur : status word different de 0x9000");
					} else {
						System.out.println("Balance : " + apdu.dataOut[0]);
					}
					break;

				case 2:
					apdu.command[Apdu.INS] = eCine.INS_REFUND_BALANCE;
					cad.exchangeApdu(apdu);
					if (apdu.getStatus() != 0x9000) {
						System.out
								.println("Erreur : status word different de 0x9000");
					} else {
						System.out.println("OK");
					}
					break;

				case 3:
					apdu.command[Apdu.INS] = eCine.INS_UNLOCK_CARD;
					byte[] donnees = new byte[1];
					donnees[0] = 0;
					apdu.setDataIn(donnees);
					cad.exchangeApdu(apdu);
					if (apdu.getStatus() != 0x9000) {
						System.out
								.println("Erreur : status word different de 0x9000");
					} else {
						System.out.println("OK");
					}
					break;

				case 4:
					apdu.command[Apdu.INS] = eCine.INS_ARCHIVE_TICKET;

					break;

				case 5:
					fin = true;
					break;
				}
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

	public static byte[] readPin() {
		Scanner scan = new Scanner(System.in).useDelimiter("");
		byte[] pin = new byte[4];
		pin[0] = (byte) scan.nextInt();
		pin[1] = (byte) scan.nextInt();
		pin[2] = (byte) scan.nextInt();
		pin[3] = (byte) scan.nextInt();
		scan.nextLine();
		return pin;
	}

	public static boolean manageError(int status) {
		switch (status) {
		case 0x9000:
			System.out.println("Ok");
			return true;
		case eCine.SW2_CARD_LOCKED:
			System.out
					.println("Your card is locked. PLease call an admin to unlock it.");
			break;
		case eCine.SW2_VERIFICATION_FAILED:
			System.out.println("Invalid PIN. Please try again");
			break;
		default:
			System.out.println("Error : " + status);
			break;
		}
		return false;
	}
}