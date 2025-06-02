package dev.hirth.heapq

/**
 * Heap queue algorithm (priority queue) implementation.
 * 
 * Heaps are binary trees for which every parent node has a value less than or
 * equal to any of its children. This implementation uses a list where heap[k] <= heap[2*k+1]
 * and heap[k] <= heap[2*k+2] for all k, counting elements from zero.
 */

/**
 * Transform list into a heap, in-place, in O(len(x)) time.
 */
fun <T : Comparable<T>> heapify(heap: MutableList<T>) {
    val n = heap.size
    // Start from the last non-leaf node and heapify each node
    for (i in (n / 2 - 1) downTo 0) {
        siftDown(heap, i, n - 1)
    }
}

/**
 * Transform list into a heap using a custom comparator, in-place, in O(len(x)) time.
 */
fun <T> heapify(heap: MutableList<T>, comparator: (T, T) -> Int) {
    val n = heap.size
    for (i in (n / 2 - 1) downTo 0) {
        siftDown(heap, i, n - 1, comparator)
    }
}

/**
 * Push the value item onto the heap, maintaining the heap invariant.
 */
fun <T : Comparable<T>> heappush(heap: MutableList<T>, item: T) {
    heap.add(item)
    siftUp(heap, heap.size - 1)
}

/**
 * Push the value item onto the heap using a custom comparator.
 */
fun <T> heappush(heap: MutableList<T>, item: T, comparator: (T, T) -> Int) {
    heap.add(item)
    siftUp(heap, heap.size - 1, comparator)
}

/**
 * Pop and return the smallest item from the heap, maintaining the heap invariant.
 * If the heap is empty, throws NoSuchElementException.
 */
fun <T : Comparable<T>> heappop(heap: MutableList<T>): T {
    if (heap.isEmpty()) {
        throw NoSuchElementException("pop from empty heap")
    }
    
    val lastElement = heap.removeAt(heap.size - 1)
    if (heap.isEmpty()) {
        return lastElement
    }
    
    val returnItem = heap[0]
    heap[0] = lastElement
    siftDown(heap, 0, heap.size - 1)
    return returnItem
}

/**
 * Pop and return the smallest item from the heap using a custom comparator.
 */
fun <T> heappop(heap: MutableList<T>, comparator: (T, T) -> Int): T {
    if (heap.isEmpty()) {
        throw NoSuchElementException("pop from empty heap")
    }
    
    val lastElement = heap.removeAt(heap.size - 1)
    if (heap.isEmpty()) {
        return lastElement
    }
    
    val returnItem = heap[0]
    heap[0] = lastElement
    siftDown(heap, 0, heap.size - 1, comparator)
    return returnItem
}

/**
 * Pop and return the current smallest value, and add the new item.
 * This is more efficient than heappop() followed by heappush().
 */
fun <T : Comparable<T>> heapreplace(heap: MutableList<T>, item: T): T {
    if (heap.isEmpty()) {
        throw NoSuchElementException("heapreplace on empty heap")
    }
    
    val returnItem = heap[0]
    heap[0] = item
    siftDown(heap, 0, heap.size - 1)
    return returnItem
}

/**
 * Pop and return the current smallest value using a custom comparator, and add the new item.
 */
fun <T> heapreplace(heap: MutableList<T>, item: T, comparator: (T, T) -> Int): T {
    if (heap.isEmpty()) {
        throw NoSuchElementException("heapreplace on empty heap")
    }
    
    val returnItem = heap[0]
    heap[0] = item
    siftDown(heap, 0, heap.size - 1, comparator)
    return returnItem
}

/**
 * Fast version of a heappush followed by a heappop.
 */
fun <T : Comparable<T>> heappushpop(heap: MutableList<T>, item: T): T {
    if (heap.isNotEmpty() && heap[0] < item) {
        val returnItem = heap[0]
        heap[0] = item
        siftDown(heap, 0, heap.size - 1)
        return returnItem
    }
    return item
}

/**
 * Fast version of a heappush followed by a heappop using a custom comparator.
 */
fun <T> heappushpop(heap: MutableList<T>, item: T, comparator: (T, T) -> Int): T {
    if (heap.isNotEmpty() && comparator(heap[0], item) < 0) {
        val returnItem = heap[0]
        heap[0] = item
        siftDown(heap, 0, heap.size - 1, comparator)
        return returnItem
    }
    return item
}

/**
 * Find the n largest elements in an iterable.
 */
fun <T : Comparable<T>> nlargest(n: Int, iterable: Iterable<T>): List<T> {
    val items = iterable.toList()
    
    return when {
        n <= 0 -> emptyList()
        n >= items.size -> items.sortedDescending()
        n * 10 <= items.size -> {
            // Use a min heap of size n
            val heap = mutableListOf<T>()
            for (item in items) {
                if (heap.size < n) {
                    heappush(heap, item)
                } else if (item > heap[0]) {
                    heapreplace(heap, item)
                }
            }
            heap.sortedDescending()
        }
        else -> items.sortedDescending().take(n)
    }
}

/**
 * Find the n largest elements using a key function.
 */
fun <T, K : Comparable<K>> nlargest(n: Int, iterable: Iterable<T>, key: (T) -> K): List<T> {
    val items = iterable.toList()
    
    return when {
        n <= 0 -> emptyList()
        n >= items.size -> items.sortedByDescending { key(it) }
        else -> items.sortedByDescending { key(it) }.take(n)
    }
}

/**
 * Find the n smallest elements in an iterable.
 */
fun <T : Comparable<T>> nsmallest(n: Int, iterable: Iterable<T>): List<T> {
    val items = iterable.toList()
    
    return when {
        n <= 0 -> emptyList()
        n >= items.size -> items.sorted()
        n * 10 <= items.size -> {
            // Use a max heap of size n (invert comparisons)
            val heap = mutableListOf<T>()
            val comparator: (T, T) -> Int = { a, b -> b.compareTo(a) } // Reverse for max heap
            
            for (item in items) {
                if (heap.size < n) {
                    heappush(heap, item, comparator)
                } else if (item < heap[0]) {
                    heapreplace(heap, item, comparator)
                }
            }
            heap.sorted()
        }
        else -> items.sorted().take(n)
    }
}

/**
 * Find the n smallest elements using a key function.
 */
fun <T, K : Comparable<K>> nsmallest(n: Int, iterable: Iterable<T>, key: (T) -> K): List<T> {
    val items = iterable.toList()
    
    return when {
        n <= 0 -> emptyList()
        n >= items.size -> items.sortedBy { key(it) }
        else -> items.sortedBy { key(it) }.take(n)
    }
}

/**
 * Merge multiple sorted inputs into a single sorted output.
 */
fun <T : Comparable<T>> merge(vararg iterables: Iterable<T>): Sequence<T> = sequence {
    val heap = mutableListOf<IndexedValue<T>>()
    val iterators = iterables.map { it.iterator() }.toMutableList()
    
    // Initialize heap with first element from each iterator
    for (i in iterators.indices) {
        if (iterators[i].hasNext()) {
            val value = iterators[i].next()
            heappush(heap, IndexedValue(i, value)) { a, b -> a.value.compareTo(b.value) }
        }
    }
    
    while (heap.isNotEmpty()) {
        val (index, value) = heappop(heap) { a, b -> a.value.compareTo(b.value) }
        yield(value)
        
        if (iterators[index].hasNext()) {
            val nextValue = iterators[index].next()
            heappush(heap, IndexedValue(index, nextValue)) { a, b -> a.value.compareTo(b.value) }
        }
    }
}

/**
 * Internal function to maintain heap property by moving element up.
 */
private fun <T : Comparable<T>> siftUp(heap: MutableList<T>, pos: Int) {
    siftUp(heap, pos) { a, b -> a.compareTo(b) }
}

/**
 * Internal function to maintain heap property by moving element up with comparator.
 */
private fun <T> siftUp(heap: MutableList<T>, pos: Int, comparator: (T, T) -> Int) {
    var childPos = pos
    val newItem = heap[pos]
    
    while (childPos > 0) {
        val parentPos = (childPos - 1) / 2
        val parent = heap[parentPos]
        if (comparator(newItem, parent) >= 0) {
            break
        }
        heap[childPos] = parent
        childPos = parentPos
    }
    heap[childPos] = newItem
}

/**
 * Internal function to maintain heap property by moving element down.
 */
private fun <T : Comparable<T>> siftDown(heap: MutableList<T>, pos: Int, endPos: Int) {
    siftDown(heap, pos, endPos) { a, b -> a.compareTo(b) }
}

/**
 * Internal function to maintain heap property by moving element down with comparator.
 */
private fun <T> siftDown(heap: MutableList<T>, pos: Int, endPos: Int, comparator: (T, T) -> Int) {
    var parentPos = pos
    val newItem = heap[pos]
    var childPos = 2 * pos + 1
    
    while (childPos <= endPos) {
        val rightPos = childPos + 1
        if (rightPos <= endPos && comparator(heap[rightPos], heap[childPos]) < 0) {
            childPos = rightPos
        }
        
        heap[parentPos] = heap[childPos]
        parentPos = childPos
        childPos = 2 * childPos + 1
    }
    
    heap[parentPos] = newItem
    siftUp(heap, parentPos, comparator)
}

