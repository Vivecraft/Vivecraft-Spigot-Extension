package org.vivecraft.util;

public class Pair<K, V> {
    public final K left;
    public final V right;

    protected Pair(K left, V right) {
        this.left = left;
        this.right = right;
    }

    public static <K, V> Pair<K, V> of(K left, V right) {
        return new Pair<>(left, right);
    }
}
