import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GenericBTree<Key extends Comparable<Key>, Value>  {
    private static int M = 4;    // max children per B-tree node = M-1

    private Node root;             // root of the B-tree
    private int HT;                // height of the B-tree
    private int N;                 // number of key-value pairs in the B-tree
    private static final int MAX_KEYS = M - 1;

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
        private Comparable key;
        private List<Object> value;
        public Entry(Comparable key) {
            this.key = key;
            this.value = new ArrayList<>();
        }

        public Entry(Comparable key, List<Object> value) {
            this.key   = key;
            this.value = value;
        }

        public void add(String v) {
            this.value.add(v);
        }
    }

    // constructor
    public GenericBTree() {
        root = new Node(0, null);
    }

    // return number of key-value pairs in the B-tree
    public int size() { return N; }

    // return height of B-tree
    public int height() { return HT; }


    // search for given key, return associated value; return null if no such key
    public List<Value> get(Key key) { return search(root, key); }

    private List<Value> search(Node x, Key key) {
        if (x == null) {
            return null;
        }
        Entry[] data = x.data;
        Node[] children = x.children;

        for (int i = 1; i <= x.m; i++) {
            if (eq(key, data[i].key)) return (List<Value>) data[i].value;
            else if (less(key, data[i].key)) return search(children[i-1], key);
        }
        return search(children[x.m], key);
    }

    // insert key-value pair
    // add code to check for duplicate keys
    public void put(Key key, Value value) {
        List<Value> result = search(root, key);
        if (result != null) {
            result.add(value);
            return;
        }
        List<Value> list = new ArrayList<>();
        list.add(value);
        insert(root, key, list, HT);
    }

    private void insert(Node h, Key key, List<Value> value, int height) {
        if (height == 0) {
            int j;
            Entry t = new Entry(key, (List<Object>) value);

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
                node.data[1] = new Entry(key, (List<Object>) value);
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
            int mid = h.m / 2;
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
                s += indent + "(" + data[j].key + ")\n";
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


    /*************************************************************************
     *  test client
     *************************************************************************/
    public static void main(String[] args) {
        GenericBTree<String, String> st = new GenericBTree<String, String>();
//        System.out.println(st);

        st.put("www.cs.princeton.edu", "128.112.136.12");
        st.put("www.cs.princeton.edu", "128.112.136.11");
        st.put("www.princeton.edu",    "128.112.128.15");
        st.put("www.yale.edu",         "130.132.143.21");
        st.put("www.simpsons.com",     "209.052.165.60");
        st.put("www.apple.com",        "17.112.152.32");
        st.put("www.amazon.com",       "207.171.182.16");
        st.put("www.ebay.com",         "66.135.192.87");
        st.put("www.cnn.com",          "64.236.16.20");
        st.put("www.google.com",       "216.239.41.99");
        st.put("www.nytimes.com",      "199.239.136.200");
        st.put("www.microsoft.com",    "207.126.99.140");
        st.put("www.dell.com",         "143.166.224.230");
        st.put("www.slashdot.org",     "66.35.250.151");
        st.put("www.espn.com",         "199.181.135.201");
        st.put("www.weather.com",      "63.111.66.11");
        st.put("www.yahoo.com",        "216.109.118.65");


        System.out.println("cs.princeton.edu:  " + st.get("www.cs.princeton.edu"));
        System.out.println("hardvardsucks.com: " + st.get("www.harvardsucks.com"));
        System.out.println("simpsons.com:      " + st.get("www.simpsons.com"));
        System.out.println("apple.com:         " + st.get("www.apple.com"));
        System.out.println("ebay.com:          " + st.get("www.ebay.com"));
        System.out.println("dell.com:          " + st.get("www.dell.com"));
        System.out.println();

        System.out.println("size:    " + st.size());
        System.out.println("height:  " + st.height());
        System.out.println(st);
        System.out.println();
    }

}