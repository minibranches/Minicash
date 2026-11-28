package com.uramos.minicash.data.preferences

/** Secciones de la pantalla de Inicio que se pueden mostrar, ocultar y reordenar desde Ajustes. */
enum class HomeWidget(val id: String, @androidx.annotation.StringRes val titleRes: Int) {
    PATRIMONY("patrimony", com.uramos.minicash.R.string.home_widget_patrimony),
    ACCOUNTS("accounts", com.uramos.minicash.R.string.home_widget_accounts),
    DISTRIBUTION("distribution", com.uramos.minicash.R.string.home_widget_distribution),
    RECENTS("recents", com.uramos.minicash.R.string.home_widget_recents);

    val title: String get() = com.uramos.minicash.ui.utils.AppStrings.get(titleRes)
}

/** Cómo se dibuja «Distribución del mes». */
enum class DistributionStyle(@androidx.annotation.StringRes val labelRes: Int) {
    DONUT(com.uramos.minicash.R.string.dist_style_donut),
    BARS(com.uramos.minicash.R.string.dist_style_bars),
    STACKED(com.uramos.minicash.R.string.dist_style_stacked),
    TOP5(com.uramos.minicash.R.string.dist_style_top5);

    val displayName: String get() = com.uramos.minicash.ui.utils.AppStrings.get(labelRes)
}

/** Cómo se dibuja el Patrimonio. */
enum class PatrimonyStyle(@androidx.annotation.StringRes val labelRes: Int) {
    STRIP(com.uramos.minicash.R.string.patrimony_style_strip),
    CARD(com.uramos.minicash.R.string.patrimony_style_card);

    val displayName: String get() = com.uramos.minicash.ui.utils.AppStrings.get(labelRes)
}

/** Configuración de Inicio: qué secciones se ven, en qué orden y con qué estilo. */
data class HomeLayout(
    val order: List<HomeWidget> = HomeWidget.entries,
    val hidden: Set<HomeWidget> = emptySet(),
    val distributionStyle: DistributionStyle = DistributionStyle.STACKED,
    val patrimonyStyle: PatrimonyStyle = PatrimonyStyle.STRIP
) {
    /** Secciones a dibujar, en su orden. */
    val visible: List<HomeWidget> get() = order.filter { it !in hidden }
}

/**
 * Orden guardado como «id,id,id». Tolera lo que sobra o falta: los ids desconocidos se ignoran, los repetidos se
 * toman una vez y las secciones que no aparecen (por ejemplo una nueva en una versión futura) se agregan al final
 * en su orden normal. Sin dato guardado, el orden de fábrica.
 */
fun parseHomeOrder(raw: String?): List<HomeWidget> {
    val byId = HomeWidget.entries.associateBy { it.id }
    val seen = LinkedHashSet<HomeWidget>()
    raw?.split(',')?.forEach { token -> byId[token.trim()]?.let(seen::add) }
    HomeWidget.entries.forEach(seen::add)
    return seen.toList()
}

fun encodeHomeOrder(order: List<HomeWidget>): String = order.joinToString(",") { it.id }

/** Secciones ocultas guardadas como «id,id»; ids desconocidos se ignoran. */
fun parseHomeHidden(raw: String?): Set<HomeWidget> {
    val byId = HomeWidget.entries.associateBy { it.id }
    return raw?.split(',')?.mapNotNull { byId[it.trim()] }?.toSet() ?: emptySet()
}

fun encodeHomeHidden(hidden: Set<HomeWidget>): String = hidden.joinToString(",") { it.id }

/** Mueve la sección en [from] a la posición [to], sin salirse de la lista. */
fun moveHomeWidget(order: List<HomeWidget>, from: Int, to: Int): List<HomeWidget> {
    if (from !in order.indices || order.isEmpty()) return order
    val target = to.coerceIn(0, order.lastIndex)
    if (target == from) return order
    return order.toMutableList().apply { add(target, removeAt(from)) }
}

fun distributionStyleOf(raw: String?): DistributionStyle =
    DistributionStyle.entries.firstOrNull { it.name == raw } ?: DistributionStyle.STACKED

fun patrimonyStyleOf(raw: String?): PatrimonyStyle =
    PatrimonyStyle.entries.firstOrNull { it.name == raw } ?: PatrimonyStyle.STRIP
