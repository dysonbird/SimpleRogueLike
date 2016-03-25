package me.shuter.roguelike.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * 针对key为Int的HashMap 进行优化
 * @author xieshude
 *
 * @param <V>
 */
public class IntHashMap<V> implements Serializable {
	private static final long serialVersionUID = -2204080465508954511L;

	/**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The bin count threshold for using a tree rather than list for a
     * bin.  Bins are converted to trees when adding an element to a
     * bin with at least this many nodes. The value must be greater
     * than 2 and should be at least 8 to mesh with assumptions in
     * tree removal about conversion back to plain bins upon
     * shrinkage.
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * The bin count threshold for untreeifying a (split) bin during a
     * resize operation. Should be less than TREEIFY_THRESHOLD, and at
     * most 6 to mesh with shrinkage detection under removal.
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * The smallest table capacity for which bins may be treeified.
     * (Otherwise the table is resized if too many nodes in a bin.)
     * Should be at least 4 * TREEIFY_THRESHOLD to avoid conflicts
     * between resizing and treeification thresholds.
     */
    static final int MIN_TREEIFY_CAPACITY = 64;
    
    /**
     * Basic hash bin node, used for most entries.  (See below for
     * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
     */
    static class Node<V> {
        final int hash;
        final int key;
        V value;
        Node<V> next;

        Node(int hash, int key, V value, Node<V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final int getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Node<?>) {
            	Node<?> node = (Node<?>)o;
                if (Objects.equals(key, node.getKey()) &&
                    Objects.equals(value, node.getValue()))
                    return true;
            }
            return false;
        }
    }
    
    /* ---------------- Static utilities -------------- */

    /**
     * Computes key.hashCode() and spreads (XORs) higher bits of hash
     * to lower.  Because the table uses power-of-two masking, sets of
     * hashes that vary only in bits above the current mask will
     * always collide. (Among known examples are sets of Float keys
     * holding consecutive whole numbers in small tables.)  So we
     * apply a transform that spreads the impact of higher bits
     * downward. There is a tradeoff between speed, utility, and
     * quality of bit-spreading. Because many common sets of hashes
     * are already reasonably distributed (so don't benefit from
     * spreading), and because we use trees to handle large sets of
     * collisions in bins, we just XOR some shifted bits in the
     * cheapest possible way to reduce systematic lossage, as well as
     * to incorporate impact of the highest bits that would otherwise
     * never be used in index calculations because of table bounds.
     */
    static final int hash(int key) {
        int h = key;
        return (h) ^ (h >>> 16);
    }
    
    /**
     * Returns a power of two size for the given target capacity.
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
    
    /* ---------------- Fields -------------- */

    /**
     * The table, initialized on first use, and resized as
     * necessary. When allocated, length is always a power of two.
     * (We also tolerate length zero in some operations to allow
     * bootstrapping mechanics that are currently not needed.)
     */
    transient Node<V>[] table;
    
    /**
     * The number of key-value mappings contained in this map.
     */
    transient int size;

    /**
     * The number of times this HashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the HashMap or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the HashMap fail-fast.  (See ConcurrentModificationException).
     */
    transient int modCount;

    /**
     * The next size value at which to resize (capacity * load factor).
     *
     * @serial
     */
    // (The javadoc description is true upon serialization.
    // Additionally, if the table array has not been allocated, this
    // field holds the initial array capacity, or zero signifying
    // DEFAULT_INITIAL_CAPACITY.)
    int threshold;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    final float loadFactor;
    
    /* ---------------- Public operations -------------- */
    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public IntHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }
    
    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     * The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @see #put(Object, Object)
     */
    public V get(int key) {
        Node<V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }
    
    /**
     * Implements Map.get and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @return the node, or null if none
     */
    final Node<V> getNode(int hash, int key) {
        Node<V>[] tab; 
        Node<V> first;
        Node<V> e; 
        int n; 
        @SuppressWarnings("unused")
		int k;
        if ((tab = table) != null && (n = tab.length) > 0 && (first = tab[(n - 1) & hash]) != null) {
            if (first.hash == hash && // always check first node
                (k = first.key) == key)
                return first;
            if ((e = first.next) != null) {
                do {
                    if (e.hash == hash && (k = e.key) == key)
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
    
    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(int key) {
        return getNode(hash(key), key) != null;
    }
    
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public V put(int key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
    
    /**
     * Implements Map.put and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to put
     * @param onlyIfAbsent if true, don't change existing value
     * @param evict if false, the table is in creation mode.
     * @return previous value, or null if none
     */
    final V putVal(int hash, int key, V value, boolean onlyIfAbsent, boolean evict) {
        Node<V>[] tab; 
        Node<V> p; 
        int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<V> e; 
            @SuppressWarnings("unused")
			int k;
            if (p.hash == hash && ((k = p.key) == key))
                e = p;
            else {
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash && ((k = e.key) == key))
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
    
    /**
     * Initializes or doubles table size.  If null, allocates in
     * accord with initial capacity target held in field threshold.
     * Otherwise, because we are using power-of-two expansion, the
     * elements from each bin must either stay at same index, or move
     * with a power of two offset in the new table.
     *
     * @return the table
     */
    final Node<V>[] resize() {
        Node<V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"unchecked"})
        Node<V>[] newTab = (Node<V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else { // preserve order
                        Node<V> loHead = null, loTail = null;
                        Node<V> hiHead = null, hiTail = null;
                        Node<V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
    
    /**
     * Replaces all linked nodes in bin at index for given hash unless
     * table is too small, in which case resizes instead.
     */
    final void treeifyBin(Node<V>[] tab, int hash) {
        @SuppressWarnings("unused")
		int n;
//        int index; 
//        Node<V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
//        else if ((e = tab[index = (n - 1) & hash]) != null) {
//            TreeNode<K,V> hd = null, tl = null;
//            do {
//                TreeNode<K,V> p = replacementTreeNode(e, null);
//                if (tl == null)
//                    hd = p;
//                else {
//                    p.prev = tl;
//                    tl.next = p;
//                }
//                tl = p;
//            } while ((e = e.next) != null);
//            if ((tab[index] = hd) != null)
//                hd.treeify(tab);
//        }
    }
    
    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param  key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public V remove(int key) {
        Node<V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ? null : e.value;
    }
    
    /**
     * Implements Map.remove and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to match if matchValue, else ignored
     * @param matchValue if true only remove if value is equal
     * @param movable if false do not move other nodes while removing
     * @return the node, or null if none
     */
    final Node<V> removeNode(int hash, int key, Object value, boolean matchValue, boolean movable) {
        Node<V>[] tab; 
        Node<V> p; 
        int n, index;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            Node<V> node = null, e; 
            @SuppressWarnings("unused")
			int k; 
            V v;
            if (p.hash == hash && (k = p.key) == key)
                node = p;
            else if ((e = p.next) != null) {
            	do {
                    if (e.hash == hash && (k = e.key) == key) {
                        node = e;
                        break;
                    }
                    p = e;
                } while ((e = e.next) != null);
            }
            if (node != null 
            		&& (!matchValue 
            				|| (v = node.value) == value 
            				|| (value != null && value.equals(v)))) {
                	if (node == p)
                    tab[index] = node.next;
                else
                    p.next = node.next;
                ++modCount;
                --size;
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }
    
    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        Node<V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }
    
    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value
     */
    public boolean containsValue(Object value) {
        Node<V>[] tab; 
        V v;
        if ((tab = table) != null && size > 0) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                        (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Each of these fields are initialized to contain an instance of the
     * appropriate view the first time this view is requested.  The views are
     * stateless, so there's no reason to create more than one of each.
     */
    transient volatile Set<Integer>        keySet;
    transient volatile Collection<V> values;
    
    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @return a set view of the keys contained in this map
     */
    public Set<Integer> keySet() {
        Set<Integer> ks;
        return (ks = keySet) == null ? (keySet = new KeySet()) : ks;
    }
    
    final class KeySet extends AbstractSet<Integer> {
        public final int size()                 { return size; }
        public final void clear()               { IntHashMap.this.clear(); }
        public final Iterator<Integer> iterator()     { return new KeyIterator(); }
        public final boolean contains(int o) { return containsKey(o); }
        public final boolean remove(int key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        public final Spliterator<Integer> spliterator() {
            return new KeySpliterator<>(IntHashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super Integer> action) {
            Node<V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.key);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }
    
    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a view of the values contained in this map
     */
    public Collection<V> values() {
        Collection<V> vs;
        return (vs = values) == null ? (values = new Values()) : vs;
    }
    
    final class Values extends AbstractCollection<V> {
        public final int size()                 { return size; }
        public final void clear()               { IntHashMap.this.clear(); }
        public final Iterator<V> iterator()     { return new ValueIterator(); }
        public final boolean contains(Object o) { return containsValue(o); }
        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(IntHashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super V> action) {
            Node<V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    //serialization

    // These methods are also used when serializing HashSets
    final float loadFactor() { return loadFactor; }
    final int capacity() {
        return (table != null) ? table.length :
            (threshold > 0) ? threshold :
            DEFAULT_INITIAL_CAPACITY;
    }

    /**
     * Save the state of the <tt>IntHashMap</tt> instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <i>capacity</i> of the HashMap (the length of the
     *             bucket array) is emitted (int), followed by the
     *             <i>size</i> (an int, the number of key-value
     *             mappings), followed by the key (Object) and value (Object)
     *             for each key-value mapping.  The key-value mappings are
     *             emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException {
        int buckets = capacity();
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(size);
        internalWriteEntries(s);
    }

    /**
     * Reconstitute the {@code IntHashMap} instance from a stream (i.e.,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        // Read in the threshold (ignored), loadfactor, and any hidden stuff
        s.defaultReadObject();
        reinitialize();
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new InvalidObjectException("Illegal load factor: " + loadFactor);
        s.readInt();                // Read and ignore number of buckets
        int mappings = s.readInt(); // Read number of mappings (size)
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " +
                                             mappings);
        else if (mappings > 0) { // (if zero, use defaults)
            // Size the table using given load factor only if within
            // range of 0.25...4.0
            float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
            float fc = (float)mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                       DEFAULT_INITIAL_CAPACITY :
                       (fc >= MAXIMUM_CAPACITY) ?
                       MAXIMUM_CAPACITY :
                       tableSizeFor((int)fc));
            float ft = (float)cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                         (int)ft : Integer.MAX_VALUE);
            @SuppressWarnings({"unchecked"})
                Node<V>[] tab = (Node<V>[])new Node[cap];
            table = tab;

            // Read the keys and values, and put the mappings in the HashMap
            for (int i = 0; i < mappings; i++) {
                    int key = (int) s.readObject();
                @SuppressWarnings("unchecked")
                    V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    // iterators
    
    abstract class HashIterator {
        Node<V> next;        // next entry to return
        Node<V> current;     // current entry
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
            expectedModCount = modCount;
            Node<V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<V> nextNode() {
            Node<V>[] t;
            Node<V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            int key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }
    
    final class KeyIterator extends HashIterator implements Iterator<Integer> {
    	public final Integer next() { return nextNode().key; }
    }
    
    final class ValueIterator extends HashIterator implements Iterator<V> {
	    public final V next() { return nextNode().value; }
	}
    
    /* ------------------------------------------------------------ */
    // spliterators
    
    static class HashMapSpliterator<V> {
        final IntHashMap<V> map;
        Node<V> current;          // current node
        int index;                  // current index, modified on advance/split
        int fence;                  // one past last index
        int est;                    // size estimate
        int expectedModCount;       // for comodification checks

        HashMapSpliterator(IntHashMap<V> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                IntHashMap<V> m = map;
                est = m.size;
                expectedModCount = m.modCount;
                Node<V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }
    
    static final class KeySpliterator<V> extends HashMapSpliterator<V> implements Spliterator<Integer> {
	    KeySpliterator(IntHashMap<V> m, int origin, int fence, int est,
	                   int expectedModCount) {
	        super(m, origin, fence, est, expectedModCount);
	    }
	
	    public KeySpliterator<V> trySplit() {
	        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
	        return (lo >= mid || current != null) ? null :
	            new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
	                                    expectedModCount);
	    }
	
	    public void forEachRemaining(Consumer<? super Integer> action) {
	        int i, hi, mc;
	        if (action == null)
	            throw new NullPointerException();
	        IntHashMap<V> m = map;
	        Node<V>[] tab = m.table;
	        if ((hi = fence) < 0) {
	            mc = expectedModCount = m.modCount;
	            hi = fence = (tab == null) ? 0 : tab.length;
	        }
	        else
	            mc = expectedModCount;
	        if (tab != null && tab.length >= hi &&
	            (i = index) >= 0 && (i < (index = hi) || current != null)) {
	            Node<V> p = current;
	            current = null;
	            do {
	                if (p == null)
	                    p = tab[i++];
	                else {
	                    action.accept(p.key);
	                    p = p.next;
	                }
	            } while (p != null || i < hi);
	            if (m.modCount != mc)
	                throw new ConcurrentModificationException();
	        }
	    }
	
	    public boolean tryAdvance(Consumer<? super Integer> action) {
	        int hi;
	        if (action == null)
	            throw new NullPointerException();
	        Node<V>[] tab = map.table;
	        if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
	            while (current != null || index < hi) {
	                if (current == null)
	                    current = tab[index++];
	                else {
	                    int k = current.key;
	                    current = current.next;
	                    action.accept(k);
	                    if (map.modCount != expectedModCount)
	                        throw new ConcurrentModificationException();
	                    return true;
	                }
	            }
	        }
	        return false;
	    }
	
	    public int characteristics() {
	        return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
	            Spliterator.DISTINCT;
	    }
	}
    
    static final class ValueSpliterator<V> extends HashMapSpliterator<V> implements Spliterator<V> {
	    ValueSpliterator(IntHashMap<V> m, int origin, int fence, int est,
	                     int expectedModCount) {
	        super(m, origin, fence, est, expectedModCount);
	    }
	
	    public ValueSpliterator<V> trySplit() {
	        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
	        return (lo >= mid || current != null) ? null :
	            new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
	                                      expectedModCount);
	    }
	
	    public void forEachRemaining(Consumer<? super V> action) {
	        int i, hi, mc;
	        if (action == null)
	            throw new NullPointerException();
	        IntHashMap<V> m = map;
	        Node<V>[] tab = m.table;
	        if ((hi = fence) < 0) {
	            mc = expectedModCount = m.modCount;
	            hi = fence = (tab == null) ? 0 : tab.length;
	        }
	        else
	            mc = expectedModCount;
	        if (tab != null && tab.length >= hi &&
	            (i = index) >= 0 && (i < (index = hi) || current != null)) {
	            Node<V> p = current;
	            current = null;
	            do {
	                if (p == null)
	                    p = tab[i++];
	                else {
	                    action.accept(p.value);
	                    p = p.next;
	                }
	            } while (p != null || i < hi);
	            if (m.modCount != mc)
	                throw new ConcurrentModificationException();
	        }
	    }
	
	    public boolean tryAdvance(Consumer<? super V> action) {
	        int hi;
	        if (action == null)
	            throw new NullPointerException();
	        Node<V>[] tab = map.table;
	        if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
	            while (current != null || index < hi) {
	                if (current == null)
	                    current = tab[index++];
	                else {
	                    V v = current.value;
	                    current = current.next;
	                    action.accept(v);
	                    if (map.modCount != expectedModCount)
	                        throw new ConcurrentModificationException();
	                    return true;
	                }
	            }
	        }
	        return false;
	    }
	
	    public int characteristics() {
	        return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
	    }
	}
    
    /*
     * The following package-protected methods are designed to be
     * overridden by LinkedHashMap, but not by any other subclass.
     * Nearly all other internal methods are also package-protected
     * but are declared final, so can be used by LinkedHashMap, view
     * classes, and HashSet.
     */

    // Create a regular (non-tree) node
    Node<V> newNode(int hash, int key, V value, Node<V> next) {
        return new Node<>(hash, key, value, next);
    }
    
    /**
     * Reset to initial default state.  Called by clone and readObject.
     */
    void reinitialize() {
        table = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }
    
    // Callbacks to allow LinkedHashMap post-actions
    void afterNodeAccess(Node<V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<V> p) { }
    
    // Called only from writeObject, to ensure compatible ordering.
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<V>[] tab;
        if (size > 0 && (tab = table) != null) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<V> e = tab[i]; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }
}
