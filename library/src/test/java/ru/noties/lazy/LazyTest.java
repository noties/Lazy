package ru.noties.lazy;

import android.support.annotation.NonNull;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LazyTest {

    @Test
    public void provider_return_null_throws() {

        final Lazy lazy = Lazy.of(new Lazy.Provider() {
            @NonNull
            @Override
            public Object provide() {
                return null;
            }
        });

        try {
            lazy.get();
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void provider_called_only_once() {

        final Lazy lazy = Lazy.of(new Lazy.Provider() {
            @NonNull
            @Override
            public Object provide() {
                return new Object();
            }
        });

        assertTrue(lazy.get() == lazy.get());
    }

    @Test
    public void has_value() {

        final Lazy lazy = Lazy.of(new Lazy.Provider() {
            @NonNull
            @Override
            public Object provide() {
                return new Object();
            }
        });

        assertFalse(lazy.hasValue());

        lazy.get();

        assertTrue(lazy.hasValue());
    }

    @Test
    public void hide_not_interface_throws() {

        final Lazy lazy = Lazy.of(new Lazy.Provider() {
            @NonNull
            @Override
            public Object provide() {
                return new Object();
            }
        });

        try {
            lazy.hide(Object.class);
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void hide() {

        final Object o = new Object();

        final Lazy<GetIt> lazy = Lazy.of(new Lazy.Provider<GetIt>() {
            @NonNull
            @Override
            public GetIt provide() {
                return new GetIt() {
                    @Override
                    public Object hey() {
                        return o;
                    }
                };
            }
        });

        final GetIt getIt = lazy.hide(GetIt.class);
        assertTrue(o == getIt.hey());
    }

    @Test
    public void hide_when_has_value_returns_it() {

        final Lazy<Item> lazy = Lazy.of(new Lazy.Provider<Item>() {
            @NonNull
            @Override
            public Item provide() {
                return new Item() {
                };
            }
        });

        final Item item = lazy.get();
        assertTrue(item == lazy.hide(Item.class));
    }

    private interface Item {

    }

    private interface GetIt {
        Object hey();
    }
}
