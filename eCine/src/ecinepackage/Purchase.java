package ecinepackage;

public class Purchase{
	final public static byte ELEMENT_COUNT = 14;

	private Screening screening;
	private byte tdDay, tdMonth, tdYear;
	private short tdTime;
	
	public Purchase(Screening screening,byte tdDay,byte tdMonth, byte tdYear, short tdTime){
		this.screening = screening;
		this.tdDay = tdDay;
		this.tdMonth = tdMonth;
		this.tdYear = tdYear;
		this.tdTime = tdTime;
	}
	
	public Screening getScreening() {
		return screening;
	}

	public byte getTdDay() {
		return tdDay;
	}

	public byte getTdMonth() {
		return tdMonth;
	}

	public byte getTdYear() {
		return tdYear;
	}

	public short getTdTime() {
		return tdTime;
	}


	public byte[] toByteArray() {
		byte[] screeningByteArray = screening.toByteArray();
		byte[] array = {screeningByteArray[0],
						screeningByteArray[1],
						screeningByteArray[2],
						screeningByteArray[3],
						screeningByteArray[4],
						screeningByteArray[5],
						screeningByteArray[6],
						screeningByteArray[7],
						screeningByteArray[8],
						tdDay,
						tdMonth,
						tdYear,
						(byte) (tdTime >> 8),
						(byte) (tdTime & 0xFF)				
		};
		return array;
	}
	
	public static Purchase fromByteArray(byte[] array, byte offset) {
		
		return new Purchase(
				Screening.fromByteArray(array, (byte) offset),
				array[offset + Screening.ELEMENT_COUNT ],
				array[offset + Screening.ELEMENT_COUNT + 1],
				array[offset + Screening.ELEMENT_COUNT + 2] ,
				(short) (array[offset + Screening.ELEMENT_COUNT + 3] << 8 | array[offset + Screening.ELEMENT_COUNT + 4] & 0xFF));
	}
}
