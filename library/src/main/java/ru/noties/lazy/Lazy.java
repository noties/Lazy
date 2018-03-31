package ru.noties.lazy;

import android.support.annotation.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A simple class that postpones a value initialization until first requested.
 *
 * @see #of(Provider)
 * @see #ofSynchronized(Provider)
 * @see #ofSynchronized(Lazy)
 * @see #ofHidden(Class, Provider)
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class Lazy<T> {

    public interface Provider<T> {

        @NonNull
        T provide();
    }

    /**
     * @since 1.1.0
     */
    public interface Visitor<T> {
        void visit(@NonNull Lazy<T> lazy);
    }

    /**
     * Factory method to obtain simple {@link Lazy} instance. Returned instance is not thread safe.
     * For thread-safe implementation use {@link #ofSynchronized(Lazy)} or {@link #ofSynchronized(Provider)}
     *
     * @param provider {@link Provider}
     * @return an instance of {@link Lazy}
     * @see #ofSynchronized(Provider)
     * @see #ofSynchronized(Lazy)
     * @see #ofHidden(Class, Provider)
     * @since 1.1.0
     */
    @NonNull
    public static <T> Lazy<T> of(@NonNull Provider<T> provider) {
        return new Impl<>(provider);
    }

    /**
     * Factory method to obtain synchronized instance of {@link Lazy}
     *
     * @param provider {@link Provider}
     * @return a synchronized instance of {@link Lazy}
     * @see #of(Provider)
     * @see #ofSynchronized(Lazy)
     * @see #ofHidden(Class, Provider)
     * @since 1.1.0
     */
    @NonNull
    public static <T> Lazy<T> ofSynchronized(@NonNull Provider<T> provider) {
        return ofSynchronized(of(provider));
    }

    /**
     * Factory method to obtain synchronized instance of {@link Lazy}
     *
     * @param lazy {@link Lazy} to synchronize
     * @return synchronized instance of {@link Lazy} (NB, different instance from what was supplied
     * is returned)
     * @see #of(Provider)
     * @see #ofSynchronized(Provider)
     * @see #ofHidden(Class, Provider)
     * @since 1.1.0
     */
    @NonNull
    public static <T> Lazy<T> ofSynchronized(@NonNull Lazy<T> lazy) {
        return new Synchronized<>(lazy);
    }

    /**
     * Factory method to obtain a {@link Lazy} instance that is hidden by real interface type, so there
     * is no need to manually calling `get` on {@link Lazy} instance (or actually hold a reference to it).
     * {@code final CharSequence cs = Lazy.ofHidden(CharSequence.class, () -> "I'm sooo lazy!"); }
     * <p>
     * Please note, that this works <em>ONLY</em> for interface types. Other types (simple classes,
     * abstract classes, enums, etc) will throw an IllegalStateException. This is due to the fact
     * that underneath {@link Lazy} is using `java.lang.reflect.Proxy` that can work only with interfaces.
     *
     * @param type     class of the main type
     * @param provider {@link Provider}
     * @return hidden instance of wrapped into {@link Lazy} type
     * @see #hide(Class)
     * @since 1.1.0
     */
    @NonNull
    public static <T> T ofHidden(@NonNull Class<T> type, @NonNull Provider<T> provider) {
        return of(provider).hide(type);
    }

    /**
     * @return a value that this {@link Lazy} instance holds. As it postpones initialization,
     * supplied during creation {@link Provider} must be called exactly once. So each call of this method
     * must return the same value
     * @see #hasValue()
     */
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

    /**
     * Provides ability to `hide` wrapped type if wrapped type is an interface.
     * Foe example: {@code final CharSequence cs = Lazy.ofHidden(CharSequence.class, () -> "I'm sooo lazy!"); }.
     * So there is no need to explicitly call `lazy.get()` each time, just use interface normally.
     * Please note that if this instance {@link #hasValue()} this value will be returned
     *
     * @param type of `T`
     * @return hidden instance of `T`
     * @throws IllegalStateException if supplied `type` is not an interface
     * @since 1.1.0
     */
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

    /**
     * @param visitor {@link Visitor}
     * @return self for chaining
     * @since 1.1.0
     */
    @NonNull
    public final Lazy<T> accept(@NonNull Visitor<T> visitor) {
        visitor.visit(this);
        return this;
    }

    // @since 1.1.0
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

    // @since 1.1.0
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

    // @since 1.1.0
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
