package com.example.roundupapp.domain.connectivity

/**
 * Checks if the device currently has network connectivity
 */
interface NetworkConnectivityChecker {
  /**
   * Checks if the device currently has network connectivity
   */
  suspend fun isNetworkAvailable(): Boolean
}
