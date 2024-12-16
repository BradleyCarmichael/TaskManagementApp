import java.util.LinkedHashMap;
import java.util.Map;


 // A cache that stores recently used tasks.
 // When it gets full, it removes the oldest tasks that haven't been used in a while.

public class TaskCache<K, V> {
    // The map that holds all our cached items
    private final Map<K, V> cache;


    // Creates a new cache that can hold a specific number of items

    public TaskCache(int capacity) {

        // Make a new map that:
        // - can hold the specified number of items
        // - keeps track of which items we use most often

        this.cache = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                // Remove old items when we're over capacity
                return size() > capacity;
            }
        };
    }


    // Gets an item from the cache using its key
    // Returns null if the item isn't in the cache

    public V get(K key) {
        return cache.get(key);
    }


    // Adds a new item to the cache
    // If the cache is full, removes the oldest unused item first

    public void put(K key, V value) {
        cache.put(key, value);
    }


    // Removes everything from the cache

    public void clear() {
        cache.clear();
    }


    // Checks if something is in the cache
    // Returns true if it is, false if it isn't

    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }
} 