package com.uramos.minicash.data.local.dao

import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.TransactionType

/**
 * Alta rápida desde el selector de categorías: si ya hay una categoría activa del mismo tipo con ese nombre
 * (sin distinguir mayúsculas) se reutiliza; si no, se crea con color e ícono por defecto. Devuelve su id.
 */
suspend fun CategoryDao.findOrCreateByName(name: String, type: TransactionType): Long {
    val clean = name.trim().replace(Regex("\\s+"), " ")
    val existing = getAllCategoriesList().firstOrNull {
        it.isActive && it.type == type && it.name.trim().equals(clean, ignoreCase = true)
    }
    if (existing != null) return existing.id
    return insertCategory(CategoryEntity(name = clean, type = type))
}
