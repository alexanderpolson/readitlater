package com.orbitalsoftware.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MapBuilder<K, V> {

    private final Map<K, V> map;

    public final MapBuilder<K, V> put(@NonNull final K key, @NonNull final V value) {
        map.put(key, value);
        return this;
    }
}
