package eu.kanade.tachiyomi.network

import android.content.Context
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import uy.kohesive.injekt.injectLazy
import java.io.File
import java.util.concurrent.TimeUnit

/* SY --> */ open /* SY <-- */ class NetworkHelper(context: Context) {

    private val preferences: PreferencesHelper by injectLazy()

    private val cacheDir = File(context.cacheDir, "network_cache")

    private val cacheSize = 5L * 1024 * 1024 // 5 MiB

    /* SY --> */ open /* SY <-- */ val cookieManager = AndroidCookieJar()

    /* SY --> */ open /* SY <-- */ val client by lazy {
        val builder = OkHttpClient.Builder()
            .cookieJar(cookieManager)
            .cache(Cache(cacheDir, cacheSize))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(UserAgentInterceptor())

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }
            builder.addInterceptor(httpLoggingInterceptor)
        }

        when (preferences.dohProvider()) {
            PREF_DOH_CLOUDFLARE -> builder.dohCloudflare()
            PREF_DOH_GOOGLE -> builder.dohGoogle()
        }

        builder.build()
    }

    /* SY --> */ open /* SY <-- */val cloudflareClient by lazy {
        client.newBuilder()
            .addInterceptor(CloudflareInterceptor(context))
            .build()
    }
}
