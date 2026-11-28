package com.uramos.minicash

import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.AccountType
import com.uramos.minicash.ui.transactions.resolveTransferAccounts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransferAccountsTest {

    private fun account(id: Long, balance: Long) =
        AccountEntity(id = id, name = "Cuenta $id", type = AccountType.DEBIT, balance = balance)

    private val accounts = listOf(account(1, 5_000), account(2, 90_000), account(3, 20_000))

    @Test
    fun `sin nada elegido propone el origen de mayor saldo y otro distinto de destino`() {
        val (source, destination) = resolveTransferAccounts(null, null, accounts)!!
        assertEquals(2L, source.id)
        assertEquals(1L, destination.id)
    }

    @Test
    fun `respeta el origen y el destino elegidos`() {
        val (source, destination) = resolveTransferAccounts(1L, 3L, accounts)!!
        assertEquals(1L, source.id)
        assertEquals(3L, destination.id)
    }

    @Test
    fun `si el destino elegido es el origen toma otra cuenta`() {
        val (source, destination) = resolveTransferAccounts(3L, 3L, accounts)!!
        assertEquals(3L, source.id)
        assertNotEquals(source.id, destination.id)
    }

    @Test
    fun `un destino que ya no existe cae a la primera cuenta distinta al origen`() {
        val (source, destination) = resolveTransferAccounts(2L, 99L, accounts)!!
        assertEquals(2L, source.id)
        assertEquals(1L, destination.id)
    }

    @Test
    fun `con una sola cuenta o ninguna no hay transferencia posible`() {
        assertNull(resolveTransferAccounts(null, null, listOf(account(1, 100))))
        assertNull(resolveTransferAccounts(null, null, emptyList()))
    }
}
