package ru.noties.lazy;

import android.support.annotation.NonNull;

public class Blah {

    public static void main(String[] args) {

        final Lazy<CharSequence> lazy = Lazy.of(new Lazy.Provider<CharSequence>() {
            @NonNull
            @Override
            public CharSequence provide() {
                return "Whatever dude!";
            }
        });

        final CharSequence hidden = lazy.hide(CharSequence.class);

        log("class: %s", hidden.getClass());
        log("Hidden: %s", hidden);
        log("charAt(0): %s", hidden.charAt(0));
    }

    private static void log(@NonNull String msg, Object... args) {
        System.out.printf(msg, args);
        System.out.println();
    }
}
