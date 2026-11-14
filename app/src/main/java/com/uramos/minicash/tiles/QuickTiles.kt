package com.uramos.minicash.tiles

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.uramos.minicash.MainActivity
import com.uramos.minicash.widget.ACTION_EXPENSE
import com.uramos.minicash.widget.ACTION_INCOME
import com.uramos.minicash.widget.ACTION_TRANSFER

/**
 * Mosaico de ajustes rápidos que abre Minicash directo en el registro de un movimiento. Usa las mismas acciones
 * que los atajos y los widgets: `MainActivity` las traduce a nuevo gasto, ingreso o transferencia.
 */
abstract class QuickRecordTileService(
    private val action: String,
    private val shortcutExtra: String
) : TileService() {

    override fun onStartListening() {
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onClick() {
        // Con el teléfono bloqueado se pide desbloquear antes de abrir la app con datos financieros
        if (isLocked) unlockAndRun { openRecord() } else openRecord()
    }

    private fun openRecord() {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = this@QuickRecordTileService.action
            putExtra("shortcut_action", shortcutExtra)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pending = PendingIntent.getActivity(
                this, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}

class ExpenseTileService : QuickRecordTileService(ACTION_EXPENSE, "EXPENSE")
class IncomeTileService : QuickRecordTileService(ACTION_INCOME, "INCOME")
class TransferTileService : QuickRecordTileService(ACTION_TRANSFER, "TRANSFER")
