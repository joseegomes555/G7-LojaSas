package ipca.lojasas.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import ipca.lojasas.domain.model.*
import ipca.lojasas.domain.repository.LojaRepository
import ipca.lojasas.domain.model.Beneficiario
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class LojaRepositoryImpl(
    private val db: FirebaseFirestore
) : LojaRepository {

    override fun getProdutos(): Flow<List<Produto>> = callbackFlow {
        val listener = db.collection("produtos").addSnapshotListener { value, _ ->
            val lista = value?.toObjects(Produto::class.java) ?: emptyList()
            value?.documents?.forEachIndexed { i, doc -> lista[i].id = doc.id }
            trySend(lista)
        }
        awaitClose { listener.remove() }
    }

    override fun getLotes(): Flow<List<Lote>> = callbackFlow {
        val listener = db.collection("lotes").addSnapshotListener { value, _ ->
            val lista = mutableListOf<Lote>()
            value?.documents?.forEach { doc ->
                doc.toObject(Lote::class.java)?.let { lote ->
                    lote.id = doc.id
                    lista.add(lote)
                }
            }
            trySend(lista)
        }
        awaitClose { listener.remove() }
    }

    override fun getPedidosPorAluno(uid: String): Flow<List<Pedido>> = callbackFlow {
        val listener = db.collection("pedidos")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { value, e ->
                if (e != null) { return@addSnapshotListener }
                val lista = mutableListOf<Pedido>()
                value?.documents?.forEach { doc ->
                    doc.toObject(Pedido::class.java)?.let { p ->
                        p.id = doc.id
                        lista.add(p)
                    }
                }
                lista.sortByDescending { it.dataPedido }
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    override fun getPedidosPendentes(): Flow<List<Pedido>> = callbackFlow {
        val listener = db.collection("pedidos")
            .whereEqualTo("estado", "Pendente")
            .orderBy("dataPedido", Query.Direction.ASCENDING)
            .addSnapshotListener { value, _ ->
                val lista = mutableListOf<Pedido>()
                value?.documents?.forEach { doc ->
                    doc.toObject(Pedido::class.java)?.let { p ->
                        p.id = doc.id
                        lista.add(p)
                    }
                }
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun criarPedido(pedido: Pedido): Result<Boolean> {
        return try {
            db.collection("pedidos").add(pedido).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun abaterStock(itens: List<ItemPedido>): Result<Boolean> {
        return try {
            itens.forEach { item ->
                var qtdParaRemover = item.quantidade
                // Pesquisa simples
                val snapshot = db.collection("lotes")
                    .whereEqualTo("nomeProduto", item.nome)
                    .get().await()

                // Ordenação em memória (FIFO)
                val lotesOrdenados = snapshot.documents.sortedBy {
                    it.getTimestamp("dataValidade") ?: Timestamp.now()
                }

                for (doc in lotesOrdenados) {
                    if (qtdParaRemover <= 0) break
                    val qtdNoLote = doc.getLong("quantidade")?.toInt() ?: 0

                    if (qtdNoLote > 0) {
                        if (qtdNoLote > qtdParaRemover) {
                            db.collection("lotes").document(doc.id)
                                .update("quantidade", qtdNoLote - qtdParaRemover)
                            qtdParaRemover = 0
                        } else {
                            db.collection("lotes").document(doc.id).delete()
                            qtdParaRemover -= qtdNoLote
                        }
                    }
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Log.e("Repo", "Erro abate", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelarPedido(pedido: Pedido): Result<Boolean> {
        return try {
            db.collection("pedidos").document(pedido.id).update("estado", "Cancelado").await()
            // Repor Stock
            pedido.itens.forEach { item ->
                val snapshot = db.collection("lotes")
                    .whereEqualTo("nomeProduto", item.nome).limit(1).get().await()

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val atual = doc.getLong("quantidade")?.toInt() ?: 0
                    db.collection("lotes").document(doc.id).update("quantidade", atual + item.quantidade)
                } else {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, 30)
                    val lote = hashMapOf(
                        "nomeProduto" to item.nome,
                        "quantidade" to item.quantidade,
                        "dataValidade" to Timestamp(cal.time),
                        "categoria" to "Reposição",
                        "origem" to "Devolução",
                        "dataEntrada" to Timestamp.now()
                    )
                    db.collection("lotes").add(lote)
                }
            }
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun processarEntrega(pedidoId: String): Result<Boolean> {
        return try {
            db.collection("pedidos").document(pedidoId).update("estado", "Entregue").await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun adicionarLote(lote: Lote): Result<Boolean> {
        return try {
            db.collection("lotes").add(lote).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun removerStockManual(loteId: String, qtdAtual: Int, qtdRemover: Int): Result<Boolean> {
        return try {
            val novaQtd = qtdAtual - qtdRemover
            if (novaQtd <= 0) {
                db.collection("lotes").document(loteId).delete().await()
            } else {
                db.collection("lotes").document(loteId).update("quantidade", novaQtd).await()
            }
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getPerfilUsuario(uid: String): Beneficiario? {
        return try {
            val doc = db.collection("utilizadores").document(uid).get().await()
            doc.toObject(Beneficiario::class.java)
        } catch (e: Exception) { null }
    }
}