
import java.util.Random;

/**
 * Created by codoge on 2017/5/24.
 */
public class Index {
    public static int MAX_ID = 1764576;
    public static int MAX_KEY = 11284;
    public static int MAX_PAGE_ID = 89695;
    BTree tree;
    public static String INDEX_PATH = "disc/index";

    public Index() {
        tree = new BTree();
    }

    public void buildIndex(String path) {
        int size = HeapPage.size / Record.bytes();
        for (int pageNum = 0; pageNum < MAX_PAGE_ID; pageNum++) {
            HeapPage page = HeapPage.readFromFile(path + "/" + pageNum + ".page");
            Record[] records = page.getRecords();
            for (int i = 0; i < records.length; i++) {
                if (records[i].getID() > MAX_ID)
                    break;
                tree.put(records[i].getHourly_Counts(), pageNum * size + i);
            }
        }
        System.out.println("size:  " + tree.size());
        System.out.println("height:" + tree.height());
        System.out.println(tree);
        System.out.println();

        tree.store("disc/index");

    }
    public BTree.BTreeStore rebuildTree(String path) {
        BTree.BTreeStore bst = tree.getStoreTree(path);
        return bst;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Index index = new Index();
//        index.buildIndex(HeapFile.PAGE_PATH);
        BTree.BTreeStore bst = index.rebuildTree(INDEX_PATH);
        long end = System.currentTimeMillis();
        System.out.println("Building index last for " + (end - start) + "ms.");
        Searcher searcher = new Searcher(bst);
        Random rand = new Random();
        start = System.currentTimeMillis();
//        for (int i = 0; i < 3; i++) {
//            int randomID = rand.nextInt(MAX_KEY) + 1;
//            System.out.println(randomID);
//            searcher.printSearch(randomID);
//        }
        searcher.printSearch(9);
        searcher.printSearch(4712);
        searcher.printSearch(3682);
        end = System.currentTimeMillis();
        System.out.println("Using index to search spending " + (end - start)/3 + "ms for average.");
    }

}
