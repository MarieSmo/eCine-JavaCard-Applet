package ecinepackage;

public class Logger {
	public static final byte BUY_TICKET_REWARD = 0x0;
	public static final byte BUY_TICKET_BALANCE = 0x1;
	
	public static final byte MAX_LOG = (byte) 16;
	public static final byte MESSAGE_SIZE = (byte) 6;

	private static byte[][] logs;
	private static byte logIndex;

	public Logger() {
		logs = new byte[MAX_LOG][MESSAGE_SIZE];
		logIndex = -1;
	}
	
	private void logOperation(byte... params) {
		if(logIndex == MAX_LOG)
			logIndex = 0;
		else
			logIndex++;
		
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
		logOperation(eCine.INS_BUY_TICKET, (byte) (movieID >> 8), (byte) (movieID & 0xFF), amount, type );
	}
	
	public void logRefund (byte amount, byte totalBalance) {
		logOperation(eCine.INS_REFUND_BALANCE, amount, totalBalance, (byte) 0);
	}
	
	public void logAbort () {
		logOperation((byte)-1);
	}
	
	public byte[] toByteArray() {
		byte[] allLogs = new byte[MAX_LOG * MESSAGE_SIZE];
		short gIndex = 0;
		for (byte i=0; i< logs.length ; i++) {
			for(byte j=0; j< logs[i].length; j++) {
				allLogs[gIndex] = logs[i][j];
				gIndex++;
			}
		}
		return allLogs;
	}
}
