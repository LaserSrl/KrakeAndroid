package com.krake.core.collection;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class FixedSizeList<E> implements Collection<E> {

    final int maxSize;
    final List<E> delegate;

    public FixedSizeList(int maxSize) {
        delegate = new ArrayList<>(maxSize + 1);
        this.maxSize = maxSize;
    }

    public int remainingCapacity() {
        return maxSize - size();
    }


    public boolean add(E e) {
        if (maxSize == 0) {
            return true;
        }
        if (size() == maxSize) {
            delegate.remove(0);
        }
        delegate.add(e);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        throw new NoSuchMethodError();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean contains(Object object) {
        return delegate.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return delegate.containsAll(collection);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    @Override
    public boolean remove(Object object) {
        return delegate.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return delegate.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return delegate.retainAll(collection);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] array) {
        return delegate.toArray(array);
    }

}