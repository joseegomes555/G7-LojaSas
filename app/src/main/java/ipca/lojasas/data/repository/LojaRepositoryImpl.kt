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
import kotlinx.coroutines.tasks.await // Importante para o .await()
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
        android.util.Log.d("PerfilDebug", "1. A tentar buscar perfil para UID: $uid")

        // TENTATIVA 1: Buscar diretamente pelo ID do Documento (Mais rápido e correto)
        val listener = db.collection("utilizadores").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("PerfilDebug", "Erro no Firestore: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    android.util.Log.d("PerfilDebug", "✅ Perfil encontrado pelo ID do Documento!")
                    trySend(snapshot.data)
                } else {
                    android.util.Log.w("PerfilDebug", "⚠️ Não encontrado pelo ID. A tentar pelo campo 'uid'...")

                    // TENTATIVA 2: Fallback - Buscar pelo campo "uid" (caso tenhas gravado com ID aleatório)
                    db.collection("utilizadores")
                        .whereEqualTo("uid", uid)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { query ->
                            if (!query.isEmpty) {
                                android.util.Log.d("PerfilDebug", "✅ Perfil encontrado pelo campo 'uid'!")
                                trySend(query.documents[0].data)
                            } else {
                                android.util.Log.e("PerfilDebug", "❌ PERFIL NÃO ENCONTRADO em lado nenhum.")
                                trySend(null) // Isto é que causa o loading infinito se a UI não tratar o null
                            }
                        }
                        .addOnFailureListener {
                            android.util.Log.e("PerfilDebug", "Erro na pesquisa por campo: ${it.message}")
                        }
                }
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
                        if (error != null) return@addSnapshotListener
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

    override fun getEstadoCandidatura(uid: String): Flow<String?> = callbackFlow {
        val listener = db.collection("candidaturas")
            .whereEqualTo("uid", uid)
            .limit(1) // Assume que só há uma candidatura ativa por ano
            .addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                if (value != null && !value.isEmpty) {
                    // Retorna o estado: "Aprovada", "Pendente", "Rejeitada"
                    val estado = value.documents[0].getString("estado")
                    trySend(estado)
                } else {
                    // Não tem candidatura nenhuma
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getTodosPedidos(): Flow<List<Pedido>> = callbackFlow {
        db.collection("pedidos").orderBy("dataPedido", Query.Direction.DESCENDING)
            .addSnapshotListener { value, _ ->
                val list = value?.documents?.mapNotNull { doc ->
                    val p = doc.toObject(Pedido::class.java)
                    p?.id = doc.id
                    p
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { }
    }

    // --- LÓGICA DE STOCK (FIFO) ---
    override suspend fun fazerPedido(pedido: Pedido): Boolean {
        return try {
            android.util.Log.d("LojaRepository", "--- A INICIAR PEDIDO ---")

            // 1. Gravar o Pedido
            db.collection("pedidos").add(pedido).await()
            android.util.Log.d("LojaRepository", "Pedido gravado na coleção 'pedidos'")

            // 2. Atualizar Stock
            for (item in pedido.itens) {
                android.util.Log.d("LojaRepository", "A processar item: ${item.nome}, Qtd: ${item.quantidade}")

                var qtdParaRemover = item.quantidade

                // AQUI É ONDE GERALMENTE DÁ O ERRO DE ÍNDICE
                val lotesSnapshot = db.collection("lotes")
                    .whereEqualTo("nomeProduto", item.nome)
                    .orderBy("dataValidade")
                    .get()
                    .await()

                android.util.Log.d("LojaRepository", "Encontrados ${lotesSnapshot.size()} lotes para ${item.nome}")

                for (document in lotesSnapshot.documents) {
                    if (qtdParaRemover <= 0) break

                    val qtdNoLote = document.getLong("quantidade")?.toInt() ?: 0
                    val loteId = document.id

                    if (qtdNoLote > 0) {
                        val aAbater = if (qtdNoLote >= qtdParaRemover) qtdParaRemover else qtdNoLote

                        db.collection("lotes").document(loteId)
                            .update("quantidade", qtdNoLote - aAbater)
                            .await() // Adicionei o await() aqui também para garantir que termina antes de continuar

                        android.util.Log.d("LojaRepository", "Abatido $aAbater do lote $loteId")

                        qtdParaRemover -= aAbater
                    }
                }
            }
            android.util.Log.d("LojaRepository", "--- PEDIDO CONCLUÍDO COM SUCESSO ---")
            true
        } catch (e: Exception) {
            // ESTE LOG VAI DIZER-TE O ERRO EXATO
            android.util.Log.e("LojaRepository", "ERRO FATAL AO FAZER PEDIDO: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    // ------------------------------

    override suspend fun cancelarPedido(pedidoId: String, motivo: String) {
        db.collection("pedidos").document(pedidoId)
            .update(
                mapOf(
                    "estado" to "Cancelado",
                    "motivoCancelamento" to motivo,
                    "autorCancelamento" to "Beneficiário"
                )
            ).await()
    }

    override suspend fun aceitarReagendamento(pedidoId: String, novaData: Timestamp) {
        db.collection("pedidos").document(pedidoId)
            .update(
                mapOf(
                    "estado" to "Pendente",
                    "dataLevantamento" to novaData,
                    "propostaReagendamento" to null
                )
            ).await()
    }

    override suspend fun adicionarLote(lote: Lote) {
        db.collection("lotes").add(lote).await()
    }

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
        db.collection("pedidos").document(pedidoId).update(
            mapOf(
                "estado" to "Reagendamento",
                "propostaReagendamento" to timestamp,
                "autorReagendamento" to "Colaborador"
            )
        ).await()
    }

    override fun uploadFotoPerfil(bitmap: Bitmap, uid: String, onComplete: (Boolean) -> Unit) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val base64String = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
        db.collection("utilizadores").whereEqualTo("uid", uid).get().addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                db.collection("utilizadores").document(docs.documents[0].id).update("fotoPerfil", base64String)
                    .addOnSuccessListener { onComplete(true) }
            }
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }
}