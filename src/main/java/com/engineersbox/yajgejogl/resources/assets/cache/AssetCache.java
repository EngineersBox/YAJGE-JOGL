package com.engineersbox.yajgejogl.resources.assets.cache;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AssetCache<K, V> {

    private final int size;
    private final ConcurrentLinkedDeque<Pair<K, V>> entries;
    private final ReentrantReadWriteLock lock;

    public AssetCache(final int size) {
        this.size = size;
        this.lock = new ReentrantReadWriteLock();
        this.entries = new ConcurrentLinkedDeque<>();
    }

    public Optional<V> query(final K key) {
        this.lock.writeLock().lock();
        try {
            final Optional<Pair<K, V>> optionalEntry = this.entries.stream()
                    .filter((final Pair<K, V> pair) -> pair.getKey().equals(key))
                    .findFirst();
            if (optionalEntry.isEmpty()) {
                return Optional.empty();
            }
            final Pair<K, V> entry = optionalEntry.get();
            this.entries.remove(entry);
            this.entries.push(entry);
            return Optional.of(entry.getValue());
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public boolean request(final K key, final V value) {
        this.lock.writeLock().lock();
        try {
            if (this.entries.size() >= this.size) {
                this.entries.removeLast();
            }
            final Optional<Pair<K, V>> optionalEntry = this.entries.stream()
                    .filter((final Pair<K, V> pair) -> pair.getKey().equals(key))
                    .findFirst();
            final Pair<K, V> entry;
            final boolean newEntry = optionalEntry.isPresent();
            if (newEntry) {
                entry = optionalEntry.get();
                this.entries.remove(entry);
                entry.setValue(value);
            } else {
                entry = MutablePair.of(key, value);
            }
            this.entries.push(entry);
            return newEntry;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public int size() {
        this.lock.readLock().lock();
        try {
            return this.entries.size();
        } finally {
            this.lock.readLock().unlock();
        }
    }

}
