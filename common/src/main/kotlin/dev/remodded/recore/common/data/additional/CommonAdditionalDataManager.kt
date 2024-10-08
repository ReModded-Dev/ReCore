package dev.remodded.recore.common.data.additional

import dev.remodded.recore.api.ReCore
import dev.remodded.recore.api.data.additional.AdditionalData
import dev.remodded.recore.api.data.additional.AdditionalDataHolder
import dev.remodded.recore.api.data.additional.AdditionalDataManager
import dev.remodded.recore.api.plugins.ReCorePlugin
import dev.remodded.recore.api.service.getLazyService
import kotlin.getValue

abstract class CommonAdditionalDataManager : AdditionalDataManager {

    abstract fun getAdditionalDataPlugins(holder: AdditionalDataHolder): Collection<ReCorePlugin>

    override fun load(holder: AdditionalDataHolder) {
        holder.clearAdditionalData()
        for (plugin in getAdditionalDataPlugins(holder))
            holder.getAdditionalData(plugin) // Loads data for all plugins
    }

    override fun save(holder: AdditionalDataHolder) {
        holder.getAllAdditionalData().forEach(AdditionalData::save)
    }

    companion object {
        val INSTANCE: AdditionalDataManager by ReCore.INSTANCE.serviceProvider.getLazyService()
    }
}
