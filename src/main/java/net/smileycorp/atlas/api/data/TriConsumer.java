package net.smileycorp.atlas.api.data;

public interface TriConsumer<T, U, V> {

    void apply(T t, U u, V v);

}
