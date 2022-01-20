package ecinepackage;

import javacard.framework.Util;

public class PastScreenings {
	public static final byte MAX_HISTORY = 16;

	private static byte[] pastScreenings;
	private static byte index;
	
	public PastScreenings() {
		pastScreenings = new byte[MAX_HISTORY];
		index = -1;
	}
	
	public void addScreening(byte id) {
		index++;
		index = (byte) (index%MAX_HISTORY);
		pastScreenings[index] = id;

	}
	
	public byte getIndex() {
		return index;
	}
	
	public short getTotalSize() {
		return (MAX_HISTORY + 1);
	}
	public byte[] toByteArray() {
		byte[] allLogs = new byte[getTotalSize()];
		allLogs[0] = getIndex();
		
		Util.arrayCopyNonAtomic(pastScreenings, (short) 0,
				allLogs, (short) 1, MAX_HISTORY);
		return allLogs;
	}
}
