/**
 * Created by codoge on 2017/5/23.
 */
public class Util {

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用
     * @param value
     *            要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value )
    {
        byte[] src = new byte[4];
        src[3] =  (byte) ((value>>24) & 0xFF);
        src[2] =  (byte) ((value>>16) & 0xFF);
        src[1] =  (byte) ((value>>8) & 0xFF);
        src[0] =  (byte) (value & 0xFF);
        return src;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src
     *            byte数组
     * @param offset
     *            从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset+1] & 0xFF)<<8)
                | ((src[offset+2] & 0xFF)<<16)
                | ((src[offset+3] & 0xFF)<<24));
        return value;
    }

    public static byte[] charToByte(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }

    public static byte[] charArrayToByte(char[] arr) {
        byte[] bytes = new byte[2 * arr.length];
        for (int i = 0; i < arr.length; i++) {
            bytes[2*i] = (byte) ((arr[i] & 0xFF00) >> 8);
            bytes[2*i+1] = (byte) (arr[i] & 0xFF);
        }
        return bytes;
    }

    public static char[] byteToCharArray(byte[] bytes, int start, int stop) {
        char[] arr = new char[(stop - start)/ 2];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (char) (((bytes[start+2*i] & 0xFF) << 8) | (bytes[start+2*i+1] & 0xFF));
        }
        return arr;
    }
}
