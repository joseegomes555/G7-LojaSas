package ipca.lojasas.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import ipca.lojasas.domain.model.Lote
import ipca.lojasas.domain.model.Pedido
import ipca.lojasas.domain.model.Produto
import ipca.lojasas.domain.repository.LojaRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.Date

class LojaRepositoryImpl(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : LojaRepository {

    override fun getProdutosCatalogo(): Flow<List<Produto>> = callbackFlow {
        val listener = db.collection("produtos").orderBy("nome")
            .addSnapshotListener { value, _ -> trySend(value?.toObjects(Produto::class.java) ?: emptyList()) }
        awaitClose { listener.remove() }
    }

    override fun getLotesStock(): Flow<List<Lote>> = callbackFlow {
        val listener = db.collection("lotes").orderBy("dataValidade")
            .addSnapshotListener { value, _ ->
                val list = value?.documents?.mapNotNull { doc ->
                    val l = doc.toObject(Lote::class.java)
                    l?.id = doc.id
                    l
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override fun getPerfilUsuario(uid: String): Flow<Map<String, Any>?> = callbackFlow {
        val listener = db.collection("utilizadores").whereEqualTo("uid", uid).limit(1)
            .addSnapshotListener { value, _ ->
                val data = if (value != null && !value.isEmpty) value.documents[0].data else null
                trySend(data)
            }
        awaitClose { listener.remove() }
    }

    override fun getMeusPedidos(uid: String): Flow<List<Pedido>> = callbackFlow {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null && user.email != null) {
                db.collection("pedidos")
                    .whereEqualTo("email", user.email)
                    .orderBy("dataPedido", Query.Direction.DESCENDING)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        val list = value?.documents?.mapNotNull { doc ->
                            val p = doc.toObject(Pedido::class.java)
                            p?.id = doc.id
                            p
                        } ?: emptyList()
                        trySend(list)
                    }
            } else {
                trySend(emptyList())
            }
        }

        auth.addAuthStateListener(authListener)
        authListener.onAuthStateChanged(auth)

        awaitClose { auth.removeAuthStateListener(authListener) }
    }

    override fun getTodosPedidos(): Flow<List<Pedido>> = callbackFlow {
        val listener = db.collection("pedidos").orderBy("dataPedido", Query.Direction.DESCENDING)
            .addSnapshotListener { value, _ ->
                val list = value?.documents?.mapNotNull { doc ->
                    val p = doc.toObject(Pedido::class.java)
                    p?.id = doc.id
                    p
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun fazerPedido(pedido: Pedido): Boolean {
        return try { db.collection("pedidos").add(pedido).await(); true } catch (e: Exception) { false }
    }

    override suspend fun cancelarPedido(pedidoId: String, motivo: String) {
        db.collection("pedidos").document(pedidoId)
            .update(
                "estado", "Cancelado",
                "motivoCancelamento", motivo,
                "autorCancelamento", "Beneficiário"
            ).await()
    }

    override suspend fun aceitarReagendamento(pedidoId: String, novaData: Timestamp) {
        db.collection("pedidos").document(pedidoId).update(mapOf("estado" to "Pendente", "dataLevantamento" to novaData, "propostaReagendamento" to null)).await()
    }

    override suspend fun adicionarLote(lote: Lote) { db.collection("lotes").add(lote).await() }

    override suspend fun eliminarLote(loteId: String, motivo: String, loteInfo: Lote) {
        val log = hashMapOf("acao" to "Eliminar", "loteId" to loteId, "motivo" to motivo, "data" to Timestamp.now())
        db.collection("logs_stock").add(log)
        db.collection("lotes").document(loteId).delete().await()
    }

    override suspend fun atualizarEstadoPedido(pedidoId: String, novoEstado: String) {
        db.collection("pedidos").document(pedidoId).update("estado", novoEstado).await()
    }

    override suspend fun reagendarPedido(pedidoId: String, novaData: Long) {
        val timestamp = Timestamp(Date(novaData))
        db.collection("pedidos").document(pedidoId).update(mapOf("estado" to "Reagendamento", "propostaReagendamento" to timestamp, "autorReagendamento" to "Colaborador")).await()
    }

    override fun uploadFotoPerfil(bitmap: Bitmap, uid: String, onComplete: (Boolean) -> Unit) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val base64String = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
        db.collection("utilizadores").whereEqualTo("uid", uid).get().addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                db.collection("utilizadores").document(docs.documents[0].id).update("fotoPerfil", base64String).addOnSuccessListener { onComplete(true) }
            }
        }
    }

    override suspend fun logout() { auth.signOut() }
}