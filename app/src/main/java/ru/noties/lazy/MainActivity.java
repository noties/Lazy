package ru.noties.lazy;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

public class MainActivity extends Activity {

    private final Lazy<SQLiteDatabase> mLazy = new Lazy<>(new Lazy.Provider<SQLiteDatabase>() {
        @Override
        public SQLiteDatabase provide() {
            // obtain somewhere a database
            // for brevity of this sample, we return null here
            return null;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mLazy.isProviderCalled()) {
            // of cause in case of this sample code, if run, here will be NullPointerException
            // but the main reason is to show how to free up some resource that is obtained via Lazy
            mLazy.get().close();
        }
    }

}
