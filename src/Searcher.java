import java.util.ArrayList;
import java.util.List;

/**
 * search capability: given a value for Hourly_Counts, looks up the B-tree index and outputs all the matching records
 * from the Heap file.
 */
public class Searcher {
    BTree.BTreeStore bst;

    public Searcher(BTree.BTreeStore bst) {
        this.bst = bst;
    }

    public List<Record> get(int key) {
        List<Record> ret = new ArrayList<>();
        List<Integer> records = bst.get(key);
        if (records == null)
            return null;
        for (int i = 0; i < records.size(); i++) {
            ret.add(HeapFile.findByID(records.get(i)));
        }
        return ret;
    }

    public void printSearch(int key) {
        List<Record> records = get(key);
        System.out.println("Search key " + key + ":");
        if (records == null) {
            System.out.println("this key not exist.");
            return;
        }

        for (int i = 0; i < records.size(); i++) {
            System.out.println(records.get(i));
        }
        System.out.println();
    }
}
