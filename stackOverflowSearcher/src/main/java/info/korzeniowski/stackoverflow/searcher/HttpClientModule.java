package info.korzeniowski.stackoverflow.searcher;

import android.content.Context;
import android.widget.Toast;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.client.OkClient;

@Module
public class HttpClientModule {

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Context context) {
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
}
