package com.example.roundupapp.domain.connectivity

/**
 * Interface for checking network connectivity
 * Allows for easy testing and platform-independent implementation
 */
interface NetworkConnectivityChecker {
  /**
   * Checks if the device currently has network connectivity
   * @return true if network is available, false otherwise
   */
  suspend fun isNetworkAvailable(): Boolean
}
