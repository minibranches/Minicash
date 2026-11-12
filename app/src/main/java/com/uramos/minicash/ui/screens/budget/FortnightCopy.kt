package com.uramos.minicash.ui.screens.budget

import com.uramos.minicash.data.local.entities.BudgetItemEntity

/** Concepto sin mayúsculas ni espacios de más, para comparar partidas entre quincenas. */
fun normalizeConcept(concept: String): String =
    concept.trim().replace(Regex("\\s+"), " ").lowercase()

/** Una partida "ya existe" en el destino si hay otra con el mismo concepto y tipo. */
fun isDuplicateInTarget(source: BudgetItemEntity, targetItems: List<BudgetItemEntity>): Boolean {
    val concept = normalizeConcept(source.concept)
    return targetItems.any { normalizeConcept(it.concept) == concept && it.type == source.type }
}

/** Mes (1 a 12) y año tras mover [delta] meses desde [year]-[month]. */
fun shiftYearMonth(year: Int, month: Int, delta: Int): Pair<Int, Int> {
    val index = year * 12 + (month - 1) + delta
    return Math.floorDiv(index, 12) to Math.floorMod(index, 12) + 1
}
