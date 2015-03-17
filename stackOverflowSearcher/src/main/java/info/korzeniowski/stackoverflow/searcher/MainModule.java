package info.korzeniowski.stackoverflow.searcher;

import android.content.Context;
import android.widget.Toast;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.stackoverflow.searcher.util.Utils;
import retrofit.client.OkClient;

@Module
public class MainModule {

    private final Context context;

    public MainModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    Utils provideUtils() {
        return new Utils(context);
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }

}
