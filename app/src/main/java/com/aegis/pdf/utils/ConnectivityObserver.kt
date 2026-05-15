package com.aegis.pdf.utils  
  
import android.content.Context  
import android.net.ConnectivityManager  
import android.net.Network  
import kotlinx.coroutines.channels.awaitClose  
import kotlinx.coroutines.flow.Flow  
import kotlinx.coroutines.flow.callbackFlow  
import kotlinx.coroutines.flow.distinctUntilChanged  
  
class ConnectivityObserver(context: Context) {  
    private val connectivityManager =   
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager  
  
    val status: Flow<Boolean> = callbackFlow {  
        val callback = object : ConnectivityManager.NetworkCallback() {  
            override fun onAvailable(network: Network) { launch { send(true) } }  
            override fun onLost(network: Network) { launch { send(false) } }  
        }  
  
        connectivityManager.registerDefaultNetworkCallback(callback)  
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }  
    }.distinctUntilChanged()  
}