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
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;
import info.korzeniowski.stackoverflow.searcher.ui.list.ListFragment;
import info.korzeniowski.stackoverflow.searcher.ui.list.MainActivity;
import info.korzeniowski.stackoverflow.searcher.util.Utils;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

@Module(
        injects = {
                MainActivity.class,
                ListFragment.class
        }
)
public class MyModule {

    private final Context context;

    public MyModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        OkHttpClient client = new OkHttpClient();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client.setCookieHandler(cookieManager);

        try {
            Cache cache = new Cache(context.getCacheDir(), 10 * 1024 * 1024); // 10 MiB
            client.setCache(cache);
        } catch (IOException e) {
            Toast.makeText(context, "Cannot create cache for http responses", Toast.LENGTH_SHORT).show();
        }

        return client;
    }

    @Provides
    @Singleton
    OkClient provideOkClient(OkHttpClient client) {
        return new OkClient(client);
    }

    @Provides
    @Singleton
    StackOverflowApi provideRestAdapter(OkClient okClient) {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Accept", "application/json;versions=1");
                if (Utils.isNetworkAvailable(context)) {
                    int maxAge = 60; // read from cache for 1 minute
                    request.addHeader("Cache-Control", "public, max-age=" + maxAge);
                } else {
                    int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                    request.addHeader("Cache-Control",
                            "public, only-if-cached, max-stale=" + maxStale);
                }
            }
        };
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://api.stackexchange.com/2.2/")
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(okClient)
                .build();

        return restAdapter.create(StackOverflowApi.class);
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }
}
