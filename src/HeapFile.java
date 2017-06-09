import java.io.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 *
 * @see
 * @author Sam Madden
 */
public class HeapFile {
    public static String PAGE_PATH = "disc/page";

    public void generateHeapFile(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));//换成你的文件名
            reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
            String line;
            long count = 0;
            int size = HeapPage.size / Record.bytes();
            HeapPage page = null;
            while((line = reader.readLine())!= null){
                int index = (int) count % size;
                if (index == 0) {
                    if (count != 0) {
                        page.writeToFile(PAGE_PATH, size);
                        System.out.println("write page No." + (count / size - 1));
                    }
                    page = new HeapPage((int)(count / size));
                }
//                System.out.println(line);
                String item[] = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
                page.addRecord(index, item);
                count++;
            }
            int remainder = (int)((count-1) % size);
            if (remainder != 0) {
                page.writeToFile(PAGE_PATH, remainder);
                System.out.println("write page No." + count / size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Record findByID(int recordID) {
        int size = HeapPage.size / Record.bytes();
        int pageNum = (recordID) / size;
        int recordNum = (recordID) % size;
        HeapPage page = HeapPage.readFromFile(PAGE_PATH + "/" + pageNum + ".page");
        return page.getRecords()[recordNum];
    }

    public static void main(String[] args) {
        HeapFile hf = new HeapFile();
        hf.generateHeapFile("data/Pedestrian_volume__updated_monthly_.csv");
    }
}

class HeapPage {
    private int pageID;
    public static final int size = 8192; // bytes
    private Record[] records;

    public HeapPage(int pageID) {
        this.pageID = pageID;
        records = new Record[size/Record.bytes()];
    }

    public Record[] getRecords() {
        return records;
    }

    public int getPageID() {
        return pageID;
    }

    public void writeToFile(String dir, int len) {
        File file = new File(dir + "/" + pageID + ".page");
        try {
            createFile(file, size);
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            for (int i = 0; i < len; i++) {
                out.write(records[i].toByteStream());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean addRecord(int index, String[] item) {
        if (index >= records.length || item.length != 10)
            return false;
        records[index] = new Record(Integer.parseInt(item[0]), item[1], Integer.parseInt(item[2]),item[3],
                Integer.parseInt(item[4]), item[5], Integer.parseInt(item[6]), Integer.parseInt(item[7]),
                item[8], Integer.parseInt(item[9]));
        return true;
    }

    /***
     * 从输入流获取字节数组,当文件很大时，报java.lang.OutOfMemoryError: Java heap space
     *
     * @param in
     * @param len
     * @return
     * @throws IOException
     */
    public static byte[] readBytesFromInputStream(InputStream in,
                                                  int len) throws IOException {
        int readSize;
        byte[] bytes = new byte[len];
        long length_tmp = len;
        long index = 0;// start from zero
        while ((readSize = in.read(bytes, (int) index, (int) length_tmp)) != -1) {
            length_tmp -= readSize;
            if (length_tmp == 0) {
                break;
            }
            index = index + readSize;
        }
        return bytes;
    }

    public static HeapPage readFromFile(String path) {
        String[] pathArr = path.split("/");
        String fileName = pathArr[pathArr.length - 1];
        int pageID = Integer.parseInt(fileName.substring(0, fileName.length() - 5));
        HeapPage page = new HeapPage(pageID);

        File file = new File(path);
        InputStream in = null;
        try {
            System.out.println("read page file: " + path);
            in = new FileInputStream(file);
            for (int i = 0; i < page.records.length; i++) {
                // 读入多个字节到字节数组中，byteread为一次读入的字节数
                byte[] tempbytes = readBytesFromInputStream(in, Record.bytes());
                page.records[i] = Record.byteToRecord(tempbytes, 0);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return page;
    }

    public static void createFile(File file, long length) throws IOException{
        RandomAccessFile r = null;
        try {
            r = new RandomAccessFile(file, "rw");
            r.setLength(length);
        } finally{
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
//        System.out.println(Record.bytes());
        HeapPage page = new HeapPage(1);
//        for (int i = 0; i < page.records.length; i++) {
//            page.records[i] = new Record(i, "2011-08-25", 2009+i,"March", 1, "Monday", 4, 9527, "LuoPing", 103);
//        }
//        page.writeToFile("disc", page.records.length);
        HeapPage newPage = HeapPage.readFromFile("disc/page/33.page");
        for (int i = 0; i < page.records.length; i++) {
            System.out.println(newPage.records[i]);
        }
    }

}