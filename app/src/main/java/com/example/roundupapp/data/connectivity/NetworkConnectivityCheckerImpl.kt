package com.example.roundupapp.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Network connectivity checker
 * Uses ConnectivityManager to check for active network connection
 */
class NetworkConnectivityCheckerImpl @Inject constructor(
  @ApplicationContext private val context: Context
) : NetworkConnectivityChecker {

  override suspend fun isNetworkAvailable(): Boolean = withContext(Dispatchers.IO) {
    try {
      val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return@withContext false

      val network = connectivityManager.activeNetwork ?: return@withContext false
      val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@withContext false

      // Check if device has internet capability
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } catch (e: Exception) {
      // If cannot be determined, assume no connectivity to be safe
      false
    }
  }
}
