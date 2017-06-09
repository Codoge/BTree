import java.lang.reflect.Field;

public class Record {
    private int ID;
    private char[] Date_Time = new char[20];
    private int Year;
    private char[] Month = new char[10];
    private int Mdate;
    private char[] Day = new char[10];
    private int Time;
    private int Sensor_ID;
    private char[] Sensor_Name = new char[50];
    private int Hourly_Counts;

    public Record() {

    }

    public Record(int ID, String Date_Time, int Year, String Month, int Mdate, String Day, int Time, int Sensor_ID, String Sensor_Name, int Hourly_Counts) {
        this.ID = ID;
        try {
            string2CharArray(this.Date_Time, Date_Time);
            string2CharArray(this.Month, Month);
            string2CharArray(this.Day, Day);
            string2CharArray(this.Sensor_Name, Sensor_Name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.Year = Year;
        this.Mdate = Mdate;
        this.Time = Time;
        this.Sensor_ID = Sensor_ID;
        this.Hourly_Counts = Hourly_Counts;
    }

    public void string2CharArray(char[] c, String s) throws StoreOverflowException {
        if (s.length() >= c.length) {
            throw new StoreOverflowException("length exceeds!");
        }
        for (int i = 0; i < s.length(); i++) {
            c[i] = s.charAt(i);
        }
        c[s.length()] = '\0';
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public char[] getDate_Time() {
        return Date_Time;
    }

    public void setDate_Time(char[] date_Time) {
        Date_Time = date_Time;
    }

    public int getYear() {
        return Year;
    }

    public void setYear(int year) {
        Year = year;
    }

    public char[] getMonth() {
        return Month;
    }

    public void setMonth(char[] month) {
        Month = month;
    }

    public int getMdate() {
        return Mdate;
    }

    public void setMdate(int mdate) {
        Mdate = mdate;
    }

    public char[] getDay() {
        return Day;
    }

    public void setDay(char[] day) {
        Day = day;
    }

    public int getTime() {
        return Time;
    }

    public void setTime(int time) {
        Time = time;
    }

    public int getSensor_ID() {
        return Sensor_ID;
    }

    public void setSensor_ID(int sensor_ID) {
        Sensor_ID = sensor_ID;
    }

    public char[] getSensor_Name() {
        return Sensor_Name;
    }

    public void setSensor_Name(char[] sensor_Name) {
        Sensor_Name = sensor_Name;
    }

    public int getHourly_Counts() {
        return Hourly_Counts;
    }

    public void setHourly_Counts(int hourly_Counts) {
        Hourly_Counts = hourly_Counts;
    }

    @Override
    public String toString() {
        return "Record{" +
                "ID=" + ID +
                ", Date_Time=" + new String(Date_Time) +
                ", Year=" + Year +
                ", Month=" + new String(Month) +
                ", Mdate=" + Mdate +
                ", Day=" + new String(Day) +
                ", Time=" + Time +
                ", Sensor_ID=" + Sensor_ID +
                ", Sensor_Name=" + new String(Sensor_Name) +
                ", Hourly_Counts=" + Hourly_Counts +
                '}';
    }

    public byte[] toByteStream() {
        byte[] b = new byte[Record.bytes()];
        int len = 0;
        try {
            Class clazz = this.getClass();//根据类名获得其对应的Class对象 写上你想要的类名就是了 注意是全名 如果有包的话要加上 比如java.Lang.String
            Field[] fields = clazz.getDeclaredFields();//根据Class对象获得属性 私有的也可以获得
            for(Field f : fields) {
                f.setAccessible(true);
                // 根据不同属性来选择不同的byte流转换函数
                if (f.getType().getName().equals("int")) {
                    int i = (int) f.get(this);
//                    System.out.println(i);
                    byte[] bInt = Util.intToBytes(i);
                    System.arraycopy(bInt, 0, b, len, bInt.length);
                    len += bInt.length;
                } else {
                    char[] c = (char[]) f.get(this);
//                    System.out.println(c);
                    byte[] bCharArray = Util.charArrayToByte(c);
                    System.arraycopy(bCharArray, 0, b, len, bCharArray.length);
                    len += bCharArray.length;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    public static Record byteToRecord(byte[] b, int offset) {
        Record record = new Record();
        int start = offset;
        try {
            Class clazz = record.getClass();//根据类名获得其对应的Class对象 写上你想要的类名就是了 注意是全名 如果有包的话要加上 比如java.Lang.String
            Field[] fields = clazz.getDeclaredFields();//根据Class对象获得属性 私有的也可以获得
            for(Field f : fields) {
                f.setAccessible(true);
                // 根据不同属性来选择不同的byte流转换函数
                if (f.getType().getName().equals("int")) {
                    int value = Util.bytesToInt(b, start);
                    f.set(record, value);
                    start += Integer.BYTES;
                } else {
                    int len = ((char[]) f.get(record)).length;
                    char[] value = Util.byteToCharArray(b, start, start + len * Character.BYTES);
                    f.set(record, value);
                    start += len * Character.BYTES;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return record;
    }

    public static int bytes() {
        return 6 * Integer.BYTES + (20 + 10 + 10 + 50) * Character.BYTES;
    }

    public static void main(String[] args) {
        Record r = new Record(1, "2011-08-25", 2009, "January", 1, "Monday", 1, 1, "haha", 102);
        byte[] b = r.toByteStream();
        Record newR = Record.byteToRecord(b, 0);
        System.out.println(newR);
    }
}

class StoreOverflowException extends Exception {
    public StoreOverflowException(String msg) {
        super(msg);
    }
}
