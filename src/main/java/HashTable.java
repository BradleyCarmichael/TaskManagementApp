import java.util.LinkedList;

// A custom hash table that stores key-value pairs
// Uses chaining (linked lists) to handle collisions
public class HashTable<K, V> {
    // Number of buckets in the hash table
    private static final int SIZE = 100;
    private LinkedList<Entry<K, V>>[] table;

    // Creates a new empty hash table
    @SuppressWarnings("unchecked")
    public HashTable() {
        table = new LinkedList[SIZE];
        for (int i = 0; i < SIZE; i++) {
            table[i] = new LinkedList<>();
        }
    }

    // Helper class to store key-value pairs
    static class Entry<K, V> {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    // Calculates which bucket to use for a given key
    private int hash(K key) {
        return Math.abs(key.hashCode()) % SIZE;
    }

    // Adds or updates a key-value pair in the table
    public void put(K key, V value) {
        int index = hash(key);
        for (Entry<K, V> entry : table[index]) {
            if (entry.key.equals(key)) {
                entry.value = value;  // Update value if key exists
                return;
            }
        }
        table[index].add(new Entry<>(key, value));
    }

    // Gets a value using its key
    public V get(K key) {
        int index = hash(key);
        for (Entry<K, V> entry : table[index]) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null; // Return null if key not found
    }

    // Removes a key-value pair from the table
    public void remove(K key) {
        int index = hash(key);
        table[index].removeIf(entry -> entry.key.equals(key));
    }

    // Checks if a key exists in the table
    public boolean containsKey(K key) {
        return get(key) != null;
    }
}
