package ru.noties.lazy;

/**
 * A simple class that postpones a value initialization until first call. After value is
 * computed, a cached value will be returned. Please note that there will be one call to
 * provider, returned value will be cached (even in case of null) and returned during subsequent calls
 *
 * @param <T> type of the value that this instance holds
 * @see Provider
 */
@SuppressWarnings("WeakerAccess")
public class Lazy<T> {

    /**
     * An interface that defines a provider for a value
     * that {@link Lazy} should return.
     *
     * @param <T>
     */
    public interface Provider<T> {
        T provide();
    }

    private final Provider<T> mProvider;
    private volatile boolean mProviderCalled;
    private T mValue;

    /**
     * @param provider A {@link Provider}
     */
    public Lazy(Provider<T> provider) {
        if (provider == null) {
            throw new NullPointerException("Provider cannot be null");
        }
        this.mProvider = provider;
    }

    /**
     * A method to return a value of type T. Provider will be called once, no matter if
     * it returned null.
     * @return a value from {{@link #mProvider}} or a cached one {@link #mValue}, can be null
     *          if provider returned null
     */
    public T get() {
        T out = mValue;
        if (!mProviderCalled) {
            synchronized (this) {
                if (!mProviderCalled) {
                    out = mValue = mProvider.provide();
                    mProviderCalled = true;
                }
            }
        }
        return out;
    }

    /**
     * A method to get the information if a provider was called.
     * Useful if lazy value needs some freeing of resources. For example
     * {@code
     *      if (lazy.isProviderCalled()) {
     *          lazy.get().close();
     *      }
     * }
     * @return a boolean indicating if {@link #mProvider} was called
     */
    public synchronized boolean isProviderCalled() {
        return mProviderCalled;
    }
}
