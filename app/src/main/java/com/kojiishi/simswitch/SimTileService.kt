package com.kojiishi.simswitch

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class SimTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        update()
    }

    fun update() {
        val labelBuilder = StringBuilder()
        labelBuilder.append(getString(R.string.sim_settings_name))

        val subscriptions = Subscription.getList(this)
        for (subscription in subscriptions) {
            if (subscription.isDefaultData) {
                labelBuilder.append(": ")
                labelBuilder.append(subscription.displayName)
                break
            }
        }

        qsTile.apply {
            state = Tile.STATE_ACTIVE
            label = labelBuilder.toString()
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        Subscription.showSettingsAndCollapse(this)
    }
}
