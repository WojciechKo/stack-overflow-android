package info.korzeniowski.stackoverflow.searcher.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.stackoverflow.searcher.util.Utils;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

@Module
public class StackOverflowModule {
    @Provides
    @Singleton
    StackOverflowApi provideStackOverflowApi(final Utils utils, OkClient okClient) {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Accept", "application/json;versions=1");
                if (utils.isNetworkAvailable()) {
                    int maxAge = 60; // read from cache for 1 minute
                    request.addHeader("Cache-Control", "public, max-age=" + maxAge);
                } else {
                    int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                    request.addHeader("Cache-Control",
                            "public, only-if-cached, max-stale=" + maxStale);
                }
            }
        };

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(json.getAsJsonPrimitive().getAsLong() * 1000);
                return calendar.getTime();
            }
        });
        Gson gson = builder.create();

        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://api.stackexchange.com/2.2/")
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(okClient)
                .build();

        return restAdapter.create(StackOverflowApi.class);
    }
}
