package ru.noties.lazy;

import android.support.annotation.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A simple class that postpones a value initialization until first call. After value is
 * computed, a cached value will be returned. Please note that there will be one call to
 * provider, returned value will be cached (even in case of null) and returned during subsequent calls
 *
 * @param <T> type of the value that this instance holds
 * @see Provider
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class Lazy<T> {

    /**
     * An interface that defines a provider for a value
     * that {@link Lazy} should return.
     *
     * @param <T>
     */
    public interface Provider<T> {

        @NonNull
        T provide();
    }

    public interface Visitor<T> {
        void visit(@NonNull Lazy<T> lazy);
    }

    @NonNull
    public static <T> Lazy<T> of(@NonNull Provider<T> provider) {
        return new Impl<>(provider);
    }

    @NonNull
    public static <T> Lazy<T> ofSynchronized(@NonNull Provider<T> provider) {
        return ofSynchronized(of(provider));
    }

    @NonNull
    public static <T> Lazy<T> ofSynchronized(@NonNull Lazy<T> lazy) {
        return new Synchronized<>(lazy);
    }

    @NonNull
    public static <T> T ofHidden(@NonNull Class<T> type, @NonNull Provider<T> provider) {
        return of(provider).hide(type);
    }


    @NonNull
    public abstract T get();

    /**
     * Indicates if this lazy instance has called underlying {@link Provider}. Can be useful to
     * check if lazy contains some resource that needs some state management (like releasing). So
     * before releasing we do not actually obtain a new value.
     *
     * @return a flag indicating if underlying {@link Provider} has been called
     */
    public abstract boolean hasValue();


    @NonNull
    public final T hide(@NonNull Class<T> type) throws IllegalStateException {

        // we will check if type is interface even before check if we have value, so
        // behaviour is predictable no matter if we have value or not
        if (!type.isInterface()) {
            throw new IllegalStateException("Provided type is not an interface: " + type.getName());
        }

        // no need to create a new proxy (if it was requested) when we already have value -
        // just return it
        if (hasValue()) {
            return get();
        }

        //noinspection unchecked
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                new LazyInvocationHandler(this)
        );
    }

    @NonNull
    public final Lazy<T> accept(@NonNull Visitor<T> visitor) {
        visitor.visit(this);
        return this;
    }

    private static class Impl<T> extends Lazy<T> {

        private final Provider<T> provider;

        private T value;

        Impl(@NonNull Provider<T> provider) {
            this.provider = provider;
        }

        @NonNull
        @Override
        public T get() {
            T out = value;
            if (out == null) {
                out = value = provider.provide();
            }
            return out;
        }

        @Override
        public boolean hasValue() {
            return value != null;
        }
    }

    private static class Synchronized<T> extends Lazy<T> {

        private final Object lock = new Object();

        private final Lazy<T> origin;

        Synchronized(@NonNull Lazy<T> origin) {
            this.origin = origin;
        }

        @NonNull
        @Override
        public T get() {
            synchronized (lock) {
                return origin.get();
            }
        }

        @Override
        public boolean hasValue() {
            synchronized (lock) {
                return origin.hasValue();
            }
        }
    }

    private static class LazyInvocationHandler implements InvocationHandler {

        private final Lazy lazy;

        private LazyInvocationHandler(@NonNull Lazy lazy) {
            this.lazy = lazy;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            return method.invoke(lazy.get(), objects);
        }
    }
}
