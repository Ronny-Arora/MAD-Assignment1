package com.example.assignment1

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlacesResolver(context: Context, apiKey: String? = null) {

    companion object {
        private const val TAG = "PlacesResolver"
        private const val META_API_KEY = "com.google.android.geo.API_KEY"
    }

    private val appContext = context.applicationContext

    private val client: PlacesClient by lazy {
        if (!Places.isInitialized()) {
            val key = apiKey ?: readKeyFromManifest(appContext)
            require(!key.isNullOrBlank()) {
                "Google Maps/Places API key missing. Add <meta-data android:name=\"$META_API_KEY\" android:value=\"@string/google_maps_key\"/>"
            }
            Places.initialize(appContext, key)
        }
        Places.createClient(appContext)
    }

    private fun readKeyFromManifest(ctx: Context): String? = try {
        val ai = ctx.packageManager.getApplicationInfo(ctx.packageName, PackageManager.GET_META_DATA)
        ai.metaData?.getString(META_API_KEY)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to read API key from manifest", e)
        null
    }

    /**
     * Resolve a restaurant title to (lat, lng).
     * 1) Autocomplete (biased to Perth), then
     * 2) Fetch Place details (LAT_LNG field).
     */
    suspend fun resolveLatLng(
        title: String,
        bias: RectangularBounds = RectangularBounds.newInstance(
            LatLng(-32.35, 115.45), // SW of Perth metro
            LatLng(-31.40, 116.10)  // NE of Perth metro
        )
    ): Pair<Double, Double>? {
        // First attempt: with Perth bias + “Perth WA” suffix
        val placeId = findPlaceId("$title Perth WA", bias)
            ?: run {
                // Fallback attempt: no suffix, same bias
                findPlaceId(title, bias)
            }
            ?: run {
                Log.w(TAG, "No predictions for \"$title\"")
                return null
            }

        val fields = listOf(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS)
        val fetchReq = FetchPlaceRequest.newInstance(placeId, fields)
        val place = client.fetchPlace(fetchReq).await().place
        val ll = place.latLng ?: return null
        return ll.latitude to ll.longitude
    }

    private suspend fun findPlaceId(query: String, bias: RectangularBounds): String? {
        val req = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setLocationBias(bias)
            .setCountries(listOf("AU"))
            .build()

        val preds = client.findAutocompletePredictions(req).await().autocompletePredictions
        val id = preds.firstOrNull()?.placeId
        if (id == null) Log.d(TAG, "No predictions for query \"$query\"")
        return id
    }
}

/** Small Task<T>.await() helper (no extra deps). */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
