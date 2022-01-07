package ecinepackage;

public class Screening {
	private short IDMovie;
	private byte price;
	private byte duration;
	
	private byte day, month, year;
	private short time;
	private byte tdDay, tdMonth, tdYear;
	private short tdTime;
	
	private Screening(){
		IDMovie = -1;
	}
	
	public Screening(short iDMovie, byte price, byte duration, byte day,
			byte month, byte year, short time) {
	
		IDMovie = iDMovie;
		this.price = price;
		this.duration = duration;
		this.day = day;
		this.month = month;
		this.year = year;
		this.time = time;
		/*this.tdDay = tdDay;
		this.tdMonth = tdMonth;
		this.tdYear = tdYear;
		this.tdTime = tdTime;*/
	}

	public short getIDMovie() {
		return IDMovie;
	}
	
	public short getPrice() {
		return price;
	}

	public byte[] toByteArray() {
		byte[] array = new byte[9];
		array[0] = (byte) (IDMovie >> 8);
		array[1] = (byte) (IDMovie & 0xFF);
		array[2] = price;
		array[3] = duration;
		array[4] = day;
		array[5] = month;
		array[6] = year;
		array[7] = (byte) (time >> 8);
		array[8] = (byte) (time & 0xFF);
		return array;
	}
	
	public static Screening fromByteArray(byte[] array, byte offset) {
		return new Screening(
				(short) (array[offset + 0] << 8 | array[offset + 1] & 0xFF),
				array[offset + 2],
				array[offset + 3],
				array[offset + 4],
				array[offset + 5],
				array[offset + 6],
				(short) (array[offset + 7] << 8 | array[offset + 8] & 0xFF)
				);
	}

}