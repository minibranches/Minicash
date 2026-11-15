package com.uramos.minicash.data.util

import com.uramos.minicash.data.local.dao.AccountDao
import com.uramos.minicash.data.local.dao.TransactionDao
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType

/** Cambio de saldo (cuenta, centavos) que produce un movimiento ya registrado. */
fun transactionEffect(tx: TransactionEntity): List<Pair<Long, Long>> = when (tx.type) {
    TransactionType.INCOME -> listOf(tx.sourceAccountId to tx.amount)
    TransactionType.EXPENSE -> listOf(tx.sourceAccountId to -tx.amount)
    TransactionType.TRANSFER -> buildList {
        add(tx.sourceAccountId to -tx.amount)
        tx.destinationAccountId?.let { add(it to tx.amount) }
    }
}

/** Borra los movimientos y revierte su efecto en los saldos. */
suspend fun removeTransactions(accountDao: AccountDao, transactionDao: TransactionDao, transactions: List<TransactionEntity>) {
    transactions.forEach { tx ->
        transactionEffect(tx).forEach { (accountId, delta) -> accountDao.updateBalance(accountId, -delta) }
        transactionDao.deleteTransaction(tx)
    }
}

/**
 * Deshace [removeTransactions]: vuelve a insertar cada movimiento con su mismo id y reaplica su efecto. Si una
 * cuenta ya no existe, ese movimiento se omite en lugar de dejar saldos a medias.
 */
suspend fun restoreTransactions(accountDao: AccountDao, transactionDao: TransactionDao, transactions: List<TransactionEntity>) {
    transactions.forEach { tx ->
        val restored = runCatching { transactionDao.insertTransaction(tx) }.isSuccess
        if (restored) transactionEffect(tx).forEach { (accountId, delta) -> accountDao.updateBalance(accountId, delta) }
    }
}
