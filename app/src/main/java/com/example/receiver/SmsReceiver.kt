package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.SmsApplication
import com.example.worker.EventUploadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            val appContainer = (context.applicationContext as SmsApplication).container
            val smsConnector = appContainer.smsConnector
            
            CoroutineScope(Dispatchers.IO).launch {
                var newMessages = false
                for (message in messages) {
                    val sender = message.originatingAddress ?: continue
                    val body = message.messageBody ?: continue
                    val timestamp = message.timestampMillis

                    smsConnector.processIncomingSms(sender, body, timestamp)
                    newMessages = true
                }
                
                if (newMessages) {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                        
                    val workRequest = OneTimeWorkRequestBuilder<EventUploadWorker>()
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            30,
                            TimeUnit.SECONDS
                        )
                        .build()
                    WorkManager.getInstance(context).enqueue(workRequest)
                }
            }
        }
    }
}
