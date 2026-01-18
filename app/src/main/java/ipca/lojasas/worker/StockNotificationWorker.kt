package ipca.lojasas.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import ipca.lojasas.domain.model.Lote
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class StockNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = FirebaseFirestore.getInstance()

            // 1. Definir o intervalo de alerta (Hoje até +7 dias)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            val dataLimite = Timestamp(calendar.time)

            val dataHoje = Date()

            // 2. Procurar Lotes no Firestore com data de validade MENOR que o limite
            // (Isto traz produtos expirados e produtos a expirar nos próximos 7 dias)
            val snapshot = db.collection("lotes")
                .whereLessThan("dataValidade", dataLimite)
                .get()
                .await()

            // 3. Filtrar em memória
            // Aqui estamos a filtrar para mostrar apenas os que AINDA não expiraram (estão "quase" a passar)
            // Se quiseres incluir os já expirados, remove a parte: && validade.after(dataHoje)
            val lotesEmRisco = snapshot.toObjects(Lote::class.java).filter { lote ->
                val validade = lote.dataValidade?.toDate()
                validade != null && validade.after(dataHoje)
            }

            // 4. Se houver produtos, enviar notificação
            if (lotesEmRisco.isNotEmpty()) {
                val qtd = lotesEmRisco.size
                val nomeExemplo = lotesEmRisco.first().nomeProduto

                val titulo = "⚠️ Validade a Expirar"
                val mensagem = if (qtd == 1) {
                    "O produto '$nomeExemplo' expira em menos de 7 dias!"
                } else {
                    "Atenção: $qtd produtos expiram em breve (ex: $nomeExemplo)."
                }

                lancarNotificacao(titulo, mensagem)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Se falhar (ex: sem internet), o WorkManager tenta de novo mais tarde automaticamente
            Result.retry()
        }
    }

    private fun lancarNotificacao(titulo: String, mensagem: String) {
        val channelId = "stock_alerts_channel"
        val context = applicationContext

        // Criar Canal de Notificação (Obrigatório para Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alertas de Stock"
            val descriptionText = "Notificações sobre produtos a expirar"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Verificar permissão antes de enviar (Android 13+)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }


        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem)) // Permite texto longo
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(1001, builder.build())
    }
}