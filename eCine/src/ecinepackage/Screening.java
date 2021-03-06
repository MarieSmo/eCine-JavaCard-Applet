package ecinepackage;

public class Screening {
	final public static byte DURATION_UNIT = 5;
	final public static byte ELEMENT_COUNT = 9;

	private short IDMovie;
	private byte price;
	private byte duration;

	private byte day, month, year;
	private short time;
	
	public Screening(short iDMovie, byte price, byte duration, byte day,
			byte month, byte year, short time) {
	
		IDMovie = iDMovie;
		this.price = price;
		this.duration = duration;
		this.day = day;
		this.month = month;
		this.year = year;
		this.time = time;
	}

	public short getIDMovie() {
		return IDMovie;
	}
	
	public byte getPrice() {
		return price;
	}
	
	public byte getDuration() {
		return duration;
	}

	public short getTime() {
		return time;
	}
	
	
	public byte getDay() {
		return day;
	}

	public byte getMonth() {
		return month;
	}

	public byte getYear() {
		return year;
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