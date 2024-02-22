package com.spit.fam.Response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.spit.fam.Entity.Asset;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class AssetResponse implements List<Asset> {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return false;
    }

    @NonNull
    @Override
    public Iterator<Asset> iterator() {
        return null;
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] ts) {
        return null;
    }

    @Override
    public boolean add(Asset asset) {
        return false;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends Asset> collection) {
        return false;
    }

    @Override
    public boolean addAll(int i, @NonNull Collection<? extends Asset> collection) {
        return false;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public Asset get(int i) {
        return null;
    }

    @Override
    public Asset set(int i, Asset asset) {
        return null;
    }

    @Override
    public void add(int i, Asset asset) {

    }

    @Override
    public Asset remove(int i) {
        return null;
    }

    @Override
    public int indexOf(@Nullable Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(@Nullable Object o) {
        return 0;
    }

    @NonNull
    @Override
    public ListIterator<Asset> listIterator() {
        return null;
    }

    @NonNull
    @Override
    public ListIterator<Asset> listIterator(int i) {
        return null;
    }

    @NonNull
    @Override
    public List<Asset> subList(int i, int i1) {
        return null;
    }
}
