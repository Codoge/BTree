import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BTree implements Serializable{
    // (2*M + 3) * Integer.BYTES is a DNode stored size
//    private static int M = (HeapPage.size - 100) / Integer.BYTES / 2;    // max children per B-tree node = M-1

    private static int M = 300;
    private Node root;             // root of the B-tree
    private int HT;                // height of the B-tree
    private int N;                 // number of key-value pairs in the B-tree
    private static final int MAX_KEYS = M - 1;
    private BTreeStore store = new BTreeStore();

    // helper B-tree node data type
    private static final class Node implements Serializable {
        private int m;                               // number of children
        private Entry[] data = new Entry[M+1];       // 0, M position not used
        private Node[] children = new Node[M+1];     // the array of children
        private Node parent;
        private Node(int k, Node parent) {
            m = k;
            this.parent = parent;
        }               // create a node with k children
    }

    // internal nodes: use all
    // leaf nodes: only use key and value
    private static class Entry implements Serializable {
        private int key;
        private List<Integer> value;
        public Entry(int key) {
            this.key = key;
            this.value = new ArrayList<>();
        }

        public Entry(int key, List<Integer> value) {
            this.key   = key;
            this.value = value;
        }

        public void add(Integer v) {
            this.value.add(v);
        }
    }

    // constructor
    public BTree() {
        root = new Node(0, null);
    }

    // return number of key-value pairs in the B-tree
    public int size() { return N; }

    // return height of B-tree
    public int height() { return HT; }


    // search for given key, return associated value; return null if no such key
    public List<Integer> get(int key) { return search(root, key); }

    private List<Integer> search(Node x, int key) {
        if (x == null) {
            return null;
        }
        Entry[] data = x.data;
        Node[] children = x.children;

        for (int i = 1; i <= x.m; i++) {
            if (eq(key, data[i].key)) return (List<Integer>) data[i].value;
            else if (less(key, data[i].key)) return search(children[i-1], key);
        }
        return search(children[x.m], key);
    }

    // insert key-value pair
    // add code to check for duplicate keys
    public void put(int key, Integer value) {
        List<Integer> result = search(root, key);
        if (result != null) {
            result.add(value);
            return;
        }
        List<Integer> list = new ArrayList<>();
        list.add(value);
        insert(root, key, list, HT);
    }

    private void insert(Node h, int key, List<Integer> value, int height) {
        if (height == 0) {
            int j;
            Entry t = new Entry(key, (List<Integer>) value);

            for (j = 1; j <= h.m; j++) {
                if (less(key, h.data[j].key)) break;
            }

            for (int i = h.m+1; i > j; i--) {
                h.data[i] = h.data[i-1];
                h.children[i] = h.children[i-1];
            }
            h.data[j] = t;
            h.children[j] = null; // insert position always on leaf
            h.m++;
            N++;
            split(h);
        } else {
            int j;
            for (j = 1; j <= h.m; j++) {
                if (less(key, h.data[j].key)) break;
            }
            if (h.children[j-1] == null) {
                Node node = new Node(1, h);
                node.data[1] = new Entry(key, (List<Integer>) value);
                h.children[j-1] = node;
                N++;
            } else {
                insert(h.children[j-1], key, value, height-1);
            }
        }
    }

    // split node in half
    private void split(Node h) {
        if (h == null)
            return;
        if (h.m > MAX_KEYS) { // need split
            int mid = (h.m + 1) / 2;
            Node right = new Node(h.m - mid, h.parent);
            h.m = mid - 1;
            right.children[0] = h.children[mid];
            if (right.children[0] != null)
                right.children[0].parent = right;
            for (int i = 1; i <= right.m; i++) {
                right.data[i] = h.data[mid+i];
                right.children[i] = h.children[mid+i];
                if (right.children[i] != null)
                    right.children[i].parent = right;
            }
            if (h.parent != null) {
                int j;
                for (j = 1; j <= h.parent.m; j++) {
                    if (less(h.data[mid].key, h.parent.data[j].key)) break;
                }
                for (int i = h.parent.m+1 ; i > j; i--) {
                    h.parent.data[i] = h.parent.data[i-1];
                    h.parent.children[i] = h.parent.children[i-1];
                }
                h.parent.data[j] = h.data[mid];
                h.parent.children[j] = right;
                h.parent.m++;
                split(h.parent);
            } else {  // h is root node
                Node newRoot = new Node(1, null);
                newRoot.data[1] = h.data[mid];
                newRoot.children[0] = h;
                newRoot.children[1] = right;
                newRoot.children[0].parent = newRoot;
                newRoot.children[1].parent = newRoot;
                HT++;
                root = newRoot;
            }
        }
    }

    // for debugging
    public String toString() {
        return toString(root, HT, "") + "\n";
    }

    private String toString(Node h, int ht, String indent) {
        String s = "";
        Entry[] data = h.data;

        if (ht == 0) {
            for (int j = 1; j <= h.m; j++) {
                s += indent + data[j].key + " " + data[j].value + "\n";
            }
        }
        else {
            for (int j = 1; j <= h.m; j++) {
                s += toString(h.children[j-1], ht-1, indent + "     ");
                s += indent + "(" + data[j].key + " " + data[j].value + ")\n";
            }
            s+= toString(h.children[h.m], ht-1, indent + "     ");
        }
        return s;
    }


    // comparison functions - make Comparable instead of Key to avoid casts
    private boolean less(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean eq(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }

    public int storeBTree(Node node, String dir) throws Exception {
        if (node == null)
            return -1;
        BTreeStore.DNode dNode = new BTreeStore.DNode(node.m);
        dNode.children[0] = storeBTree(node.children[0], dir);
        for (int i = 1; i <= node.m; i++) {
            dNode.children[i] = storeBTree(node.children[i], dir);
            dNode.key[i] = node.data[i].key;
            store.table.put(node.data[i].key, node.data[i].value);
        }
        int index = node.m > 0 ? dNode.key[1] : 0;
        File file = new File(dir + "/" + index + ".index");
        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(file));
        oos.writeObject(dNode);
        oos.close();
        return index;
    }

    public void store(String dir) {
        store.HT = HT;
        store.N = N;
        store.dir = dir;
        try {
            store.rootIndex = storeBTree(root, dir);
            File file = new File(dir + "/" + "tree.index");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(store);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BTreeStore getStoreTree(String dir) {
        BTreeStore bst = null;
        try {
            File file = new File(dir + "/" + "tree.index");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            bst = (BTreeStore) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bst;
    }


    static class BTreeStore implements Serializable {

        private HashMap<Integer, List<Integer>> table = new HashMap<>();
        private int rootIndex;
        private DNode root;             // root of the B-tree
        private int HT;                // height of the B-tree
        private int N;                 // number of key-value pairs in the B-tree
        private String dir;

        // helper B-tree node data type
        static class DNode implements Serializable {
            private int m;                               // number of children
            private int[] key = new int[M+1];       // 0, M position not used
            private int[] children = new int[M+1];     // the array of children
            DNode(int k) {
                m = k;
            }               // create a node with k children

            private void writeObject(ObjectOutputStream oos) throws IOException {
                oos.defaultWriteObject();
                oos.writeInt(m);
                for (int i = 1; i <= m; i++) {
                    oos.writeInt(key[i]);
                }
                for (int i = 0; i <= m; i++) {
                    oos.writeInt(children[i]);
                }
            }

            private void readObject(ObjectInputStream ois) throws IOException,
                    ClassNotFoundException {
                ois.defaultReadObject();
                m = ois.readInt();
                for (int i = 1; i <= m; i++) {
                    key[i] = ois.readInt();
                }
                for (int i = 0; i <= m; i++) {
                    children[i] = ois.readInt();
                }
            }
        }

        // search for given key, return associated value; return null if no such key
        public List<Integer> get(int key) {
            return search(root, key);
        }

        private List<Integer> search(DNode x, int key) {
            if (x == null) {
                return null;
            }
            int[] data = x.key;
            int[] children = x.children;

            for (int i = 1; i <= x.m; i++) {
                if (key == data[i]) return table.get(key);
                else if (key < data[i]) {
                    DNode next = getByIndex(children[i-1]);
                    return search(next, key);
                }
            }
            DNode next = getByIndex(children[x.m]);
            return search(next, key);
        }

        private DNode getByIndex(int index) {
            DNode node = null;
            if (index == -1)
                return node;
            try {
                File file = new File(dir + "/" + index + ".index");
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                node = (DNode)ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return node;
        }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            oos.defaultWriteObject();
            oos.writeObject(table);
            oos.writeInt(rootIndex);
            oos.writeInt(HT);
            oos.writeInt(N);
            oos.writeObject(dir);
        }

        private void readObject(ObjectInputStream ois) throws IOException,
                ClassNotFoundException {
            ois.defaultReadObject();
            table = (HashMap<Integer, List<Integer>>) ois.readObject();
            rootIndex = -1;
            rootIndex = ois.readInt();
            if (rootIndex != -1)
                root = getByIndex(rootIndex);
            else
                root = null;
            HT = ois.readInt();
            N = ois.readInt();
            dir = (String) ois.readObject();
        }
    }

    /*************************************************************************
     *  test client
     *************************************************************************/
    public static void main(String[] args) {
        BTree st = new BTree();
//        System.out.println(st);
//        Random random = new Random();
//        for (int j = 1; j <= 50; j++) {
//            for (int i = 1; i <= 100; i++) {
//                st.put(i, random.nextInt(300));
//            }
//        }




        st.put(3, 136);
        st.put(4, 127);
        st.put(7,    12);
        st.put(8,         34);
        st.put(13,     34);
        st.put(3,        33);
        st.put(8,       12);
        st.put(5,         90);
        st.put(12,          356);
        st.put(8,       216);
        st.put(9,      199);
        st.put(11,    207);
        st.put(12,         166);
        st.put(6,     151);
        st.put(5,         199);
        st.put(6,      111);
        st.put(2,        65);


//        System.out.println(" 1:  " + st.get(1));
//        System.out.println(" 3:  " + st.get(3));
//        System.out.println(" 5:  " + st.get(5));
//        System.out.println(" 8:  " + st.get(8));
//        System.out.println("12:  " + st.get(12));
//        System.out.println("11:  " + st.get(11));
//        System.out.println();

        System.out.println("size:    " + st.size());
        System.out.println("height:  " + st.height());
        System.out.println(st);
        System.out.println();

        st.store("test/index");
        BTreeStore bst = st.getStoreTree("test/index");

        System.out.println(" 8:  " + bst.get(8));
        System.out.println(" 6:  " + bst.get(6));
        System.out.println(" 2:  " + bst.get(2));
    }
}

