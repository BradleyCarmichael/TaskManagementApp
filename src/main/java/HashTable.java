import java.util.LinkedList;

public class HashTable<K, V> {
    private static final int SIZE = 100; // Number of buckets
    private LinkedList<Entry<K, V>>[] table;

    public HashTable() {
        table = new LinkedList[SIZE];
        for (int i = 0; i < SIZE; i++) {
            table[i] = new LinkedList<>();
        }
    }

    // Key-value entry
    static class Entry<K, V> {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    // Hash function
    private int hash(K key) {
        return Math.abs(key.hashCode()) % SIZE;
    }

    // Add key-value pair to the table
    public void put(K key, V value) {
        int index = hash(key);
        for (Entry<K, V> entry : table[index]) {
            if (entry.key.equals(key)) {
                entry.value = value;  // Replace the value if key already exists
                return;
            }
        }
        table[index].add(new Entry<>(key, value));
    }

    // Get value by key
    public V get(K key) {
        int index = hash(key);
        for (Entry<K, V> entry : table[index]) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null; // Return null if the key doesn't exist
    }

    // Remove key-value pair by key
    public void remove(K key) {
        int index = hash(key);
        table[index].removeIf(entry -> entry.key.equals(key));
    }

    // Check if a key exists in the table
    public boolean containsKey(K key) {
        return get(key) != null;
    }
}
