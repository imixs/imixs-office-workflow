package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.imixs.workflow.ItemCollection;

import jakarta.enterprise.context.SessionScoped;

/**
 * Session wide cache for analytic data objects.
 * <p>
 * The cache is shared by all conversations (browser tabs) of one user session.
 * Entries expire after 15 minutes. The cache is cleared when the user processes
 * a workitem (see AnalyticCacheResetObserver).
 *
 * @author rsoika
 */
@SessionScoped
public class AnalyticCache implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Time to live of an entry in milliseconds (15 minutes). */
    private static final long TTL_MILLIS = 15L * 60 * 1000;

    // concurrent, because several browser tabs share the same session
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    /**
     * Returns the cached data object or null if there is none or if it is expired.
     */
    public ItemCollection get(String cacheKey) {
        Entry entry = entries.get(cacheKey);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() - entry.timestamp > TTL_MILLIS) {
            entries.remove(cacheKey);
            return null;
        }
        return entry.data;
    }

    public void put(String cacheKey, ItemCollection data) {
        entries.put(cacheKey, new Entry(data));
    }

    public void clear() {
        entries.clear();
    }

    private static class Entry implements Serializable {

        private static final long serialVersionUID = 1L;

        private final long timestamp = System.currentTimeMillis();
        private final ItemCollection data;

        Entry(ItemCollection data) {
            this.data = data;
        }
    }
}
