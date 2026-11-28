package com.uramos.minicash.ui.screens.credits

import com.uramos.minicash.ui.utils.AppStrings
import com.uramos.minicash.R
import com.uramos.minicash.ui.utils.monthKey
import com.uramos.minicash.ui.utils.toCents
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.uramos.minicash.data.local.MinicashDatabase
import com.uramos.minicash.data.local.dao.AccountDao
import com.uramos.minicash.data.local.dao.CreditDao
import com.uramos.minicash.data.local.dao.CreditInstitutionDao
import com.uramos.minicash.data.local.dao.TransactionDao
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.CreditEntity
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class CreditsSummary(
    val totalPendingDebt: Long = 0L,
    val monthlyPaymentCommitment: Long = 0L,
    val activeCreditsCount: Int = 0
)

@Immutable
data class CreditsUiState(
    /** Compras con mensualidades pendientes y compras ya liquidadas, ambas con los filtros aplicados. */
    val activeCredits: List<CreditEntity> = emptyList(),
    val settledCredits: List<CreditEntity> = emptyList(),
    val activeCount: Int = 0,
    val settledCount: Int = 0,
    val summary: CreditsSummary = CreditsSummary(),
    val selectedResponsible: String? = null,
    val selectedInstitution: String? = null,
    val selectedMonth: String? = null,
    val availableMonths: List<String> = emptyList(),
    val availableResponsibles: List<String> = emptyList(),
    val availableInstitutions: List<String> = emptyList(),
    val hasActiveFilters: Boolean = false,
    val isLoading: Boolean = false
)

private data class CreditFilters(val responsible: String?, val institution: String?, val month: String?)

class CreditsViewModel(
    private val creditDao: CreditDao,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val creditInstitutionDao: CreditInstitutionDao,
    private val database: MinicashDatabase
) : ViewModel() {

    val accounts: StateFlow<List<AccountEntity>> = accountDao.getAllActiveAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutions: StateFlow<List<CreditInstitutionEntity>> = creditInstitutionDao.getAllInstitutions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val selectedResponsible = MutableStateFlow<String?>(null)
    private val selectedInstitution = MutableStateFlow<String?>(null)
    private val selectedMonth = MutableStateFlow<String?>(null)

    private val filters = combine(selectedResponsible, selectedInstitution, selectedMonth) { resp, inst, month ->
        CreditFilters(resp, inst, month)
    }

    val uiState: StateFlow<CreditsUiState> = combine(
        creditDao.getAllCredits(),
        institutions,
        filters
    ) { list, instList, activeFilters ->
        val (respFilter, instFilter, monthFilter) = activeFilters
        val availableResponsibles = list.map { it.responsible.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val catalogInstitutions = instList.map { it.name.trim() }
        val creditInstitutions = list.map { it.institution.trim() }
        val availableInstitutions = (catalogInstitutions + creditInstitutions)
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val availableMonths = list.map { monthKey(it.purchaseDate) }.distinct().sorted()

        val filteredList = list.filter { credit ->
            val matchesResp = respFilter == null || credit.responsible.trim().equals(respFilter.trim(), ignoreCase = true)
            val matchesInst = instFilter == null || credit.institution.trim().equals(instFilter.trim(), ignoreCase = true)
            val matchesMonth = monthFilter == null || monthKey(credit.purchaseDate) == monthFilter
            matchesResp && matchesInst && matchesMonth
        }

        val activeCredits = filteredList.filter { it.paidInstallments < it.totalInstallments }
        val settledCredits = filteredList.filter { it.paidInstallments >= it.totalInstallments }

        val pendingDebt = activeCredits.sumOf {
            (it.totalInstallments - it.paidInstallments) * it.monthlyAmount
        }
        val monthlyCommitment = activeCredits.sumOf { it.monthlyAmount }

        CreditsUiState(
            activeCredits = activeCredits,
            settledCredits = settledCredits,
            activeCount = activeCredits.size,
            settledCount = settledCredits.size,
            summary = CreditsSummary(
                totalPendingDebt = pendingDebt,
                monthlyPaymentCommitment = monthlyCommitment,
                activeCreditsCount = activeCredits.size
            ),
            selectedResponsible = respFilter,
            selectedInstitution = instFilter,
            selectedMonth = monthFilter,
            availableMonths = availableMonths,
            availableResponsibles = availableResponsibles,
            availableInstitutions = availableInstitutions,
            hasActiveFilters = respFilter != null || instFilter != null || monthFilter != null,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CreditsUiState(isLoading = true))

    fun onSelectResponsibleFilter(responsible: String?) {
        selectedResponsible.value = responsible
    }

    fun onSelectInstitutionFilter(institution: String?) {
        selectedInstitution.value = institution
    }

    fun onSelectMonthFilter(monthKey: String?) {
        selectedMonth.value = monthKey
    }

    fun clearFilters() {
        selectedResponsible.value = null
        selectedInstitution.value = null
        selectedMonth.value = null
    }

    fun addInstitution(name: String, colorHex: String = "#1A73E8", onSuccess: (CreditInstitutionEntity) -> Unit = {}) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val entity = CreditInstitutionEntity(name = name.trim(), colorHex = colorHex)
            val id = creditInstitutionDao.insertInstitution(entity)
            onSuccess(entity.copy(id = id))
        }
    }

    fun addCredit(
        article: String,
        institution: String,
        responsible: String,
        totalInstallments: Int,
        monthlyAmountDouble: Double,
        originalPriceDouble: Double,
        purchaseDate: Long = System.currentTimeMillis(),
        paidInstallments: Int = 0,
        paymentDay: Int? = null,
        reminderDaysBefore: Int? = null,
        paymentOverride: Boolean = false
    ) {
        val monthlyCents = monthlyAmountDouble.toCents()
        val originalCents = originalPriceDouble.toCents()
        val totalCents = monthlyCents * totalInstallments

        viewModelScope.launch {
            val institutionName = institution.ifBlank { AppStrings.get(R.string.credit_default_institution) }.trim()
            val resolved = resolveInstitution(institutionName)
            creditDao.insertCredit(
                CreditEntity(
                    article = article,
                    institution = resolved.name,
                    institutionId = resolved.id,
                    responsible = responsible.ifBlank { AppStrings.get(R.string.credit_default_responsible) },
                    purchaseDate = purchaseDate,
                    totalInstallments = totalInstallments,
                    paidInstallments = paidInstallments.coerceIn(0, totalInstallments),
                    monthlyAmount = monthlyCents,
                    originalPrice = originalCents,
                    totalAmount = totalCents,
                    paymentDay = paymentDay,
                    reminderDaysBefore = reminderDaysBefore,
                    paymentOverride = paymentOverride
                )
            )
        }
    }

    /** Busca la institución por nombre (sin distinguir mayúsculas) y la crea si no existe. */
    private suspend fun resolveInstitution(name: String): CreditInstitutionEntity {
        creditInstitutionDao.findByName(name)?.let { return it }
        val created = CreditInstitutionEntity(name = name)
        return created.copy(id = creditInstitutionDao.insertInstitution(created))
    }

    fun deleteInstitution(institution: CreditInstitutionEntity) {
        viewModelScope.launch {
            creditInstitutionDao.deleteInstitution(institution)
        }
    }

    fun payInstallment(
        credit: CreditEntity,
        sourceAccount: AccountEntity,
        createExpenseTransaction: Boolean
    ) {
        if (credit.paidInstallments >= credit.totalInstallments) return

        viewModelScope.launch {
            val nextInstallment = credit.paidInstallments + 1

            // Transacción, saldo y avance del crédito se confirman juntos o no se aplica ninguno.
            database.withTransaction {
                if (createExpenseTransaction) {
                    val noteText = AppStrings.get(R.string.credit_payment_note, credit.article, nextInstallment, credit.totalInstallments)
                    transactionDao.insertTransaction(
                        TransactionEntity(
                            sourceAccountId = sourceAccount.id,
                            categoryId = null,
                            amount = credit.monthlyAmount,
                            type = TransactionType.EXPENSE,
                            date = System.currentTimeMillis(),
                            note = noteText
                        )
                    )
                    accountDao.updateBalance(sourceAccount.id, -credit.monthlyAmount)
                }

                creditDao.updateCredit(credit.copy(paidInstallments = nextInstallment))
            }
        }
    }

    fun incrementInstallment(credit: CreditEntity) {
        if (credit.paidInstallments < credit.totalInstallments) {
            viewModelScope.launch {
                creditDao.updateCredit(credit.copy(paidInstallments = credit.paidInstallments + 1))
            }
        }
    }

    fun deleteCredit(credit: CreditEntity) {
        viewModelScope.launch {
            creditDao.deleteCredit(credit)
        }
    }

    fun updateCredit(credit: CreditEntity) {
        viewModelScope.launch {
            val name = credit.institution.trim()
            val updated = if (name.isBlank()) {
                credit.copy(institutionId = null)
            } else {
                val resolved = resolveInstitution(name)
                credit.copy(institution = resolved.name, institutionId = resolved.id)
            }
            creditDao.updateCredit(updated)
        }
    }
}
