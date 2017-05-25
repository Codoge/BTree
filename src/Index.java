import java.io.File;

/**
 * Created by codoge on 2017/5/24.
 */
public class Index {
    BTree tree;

    public Index() {
        tree = new BTree();
    }

    public void buildTree(String path) {
        File dir = new File(path);
        File[] files = dir.listFiles();
        for (File file : files) {
            HeapPage page = HeapPage.readFromFile(file.getAbsolutePath());
            Record[] records = page.getRecords();
            for (int i = 0; i < records.length; i++) {
                tree.put(records[i].getHourly_Counts(), records[i].getID());
            }
        }
        System.out.println("size:  " + tree.size());
        System.out.println("height:" + tree.height());
        System.out.println(tree);
        System.out.println();

        tree.store("disc/index");
        BTree.BTreeStore bst = tree.getStoreTree("disc/index");

        System.out.println(" 4712:  " + bst.get(4712));
        System.out.println(" 9:  " + bst.get(9));
    }

    public void buildIndex(String path) {

    }

    public static void main(String[] args) {
        Index index = new Index();
        index.buildTree("disc/page");
    }

}
