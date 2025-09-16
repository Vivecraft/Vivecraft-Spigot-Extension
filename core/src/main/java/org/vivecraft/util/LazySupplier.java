package org.vivecraft.util;

import java.util.function.Supplier;

public class LazySupplier<T> implements Supplier<T> {

    private final Supplier<T> sup;
    private T cached;

    public LazySupplier(Supplier<T> sup) {
        this.sup = sup;
    }

    @Override
    public T get() {
        if (this.cached == null) {
            this.cached = this.sup.get();
        }
        return this.cached;
    }
}
