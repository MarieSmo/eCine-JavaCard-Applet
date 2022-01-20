package ecinepackage;

/** Rolling Log Class
 * Log actions done on the card
 * @author AL KES ISHAQ Yousif Smolinski Marie
 */
public class Logger {
	public static final byte BUY_TICKET_REWARD = 0x0;
	public static final byte BUY_TICKET_BALANCE = 0x1;
	
	public static final byte MAX_LOG = (byte) 16;
	public static final byte MESSAGE_SIZE = (byte) 6;

	private static byte[][] logs;
	private static byte logIndex;
	private static byte modified;
	public Logger() {
		logs = new byte[MAX_LOG][MESSAGE_SIZE];
		logIndex = -1;
		modified = 0;
	}
	
	private void logOperation(byte[] params) {
		logIndex++;
		logIndex = (byte) (logIndex%MAX_LOG);
		modified = 1;
		logs[logIndex] = new byte[MESSAGE_SIZE];
		
		for(short i =0; i< MESSAGE_SIZE; ++i) {
			if( i < params.length){
				logs[logIndex][i] = params[i];
			} else {
				logs[logIndex][i] = 0;
			}
		}
	}
	
	public void logTicketPurchase (short movieID, byte amount, byte type) {
		byte[] params = new byte[MESSAGE_SIZE];
		params[0] = eCine.INS_BUY_TICKET;
		params[1] =	(byte) (movieID >> 8);
		params[2] = (byte) (movieID & 0xFF);
		params[3] = amount;
		params[4] = type;
		logOperation(params);
	}
	
	public void logRefund (byte amount, byte totalBalance) {
		byte[] params = new byte[3];
		params[0] = eCine.INS_REFUND_BALANCE;
		params[1] =	amount;
		params[2] = totalBalance;
		logOperation(params);
	}
	
	public void logAbort () {
		byte[] params = new byte[1];
		params[0] = (byte) -1;
		if(modified == 1)
			logOperation(params);
		commit();
	}
	
	public void commit () {
		modified = 0;
	}
	
	public byte getIndex() {
		return logIndex;
	}
	
	public short getTotalSize() {
		return (MAX_LOG * MESSAGE_SIZE) + 1;
	}
	public byte[] toByteArray() {
		byte[] allLogs = new byte[getTotalSize()];
		allLogs[0] = getIndex();
		short gIndex = 1;
		for (byte i=0; i< logs.length ; i++) {
			for(byte j=0; j< logs[i].length; j++) {
				allLogs[gIndex] = logs[i][j];
				gIndex++;
			}
		}
		return allLogs;
	}
}
