package dev.remodded.recore.paper

import dev.remodded.recore.api.ReCore
import dev.remodded.recore.api.data.additional.AdditionalDataManager
import dev.remodded.recore.api.lib.LibraryLoader
import dev.remodded.recore.api.platform.Platform
import dev.remodded.recore.api.platform.PlatformInfo
import dev.remodded.recore.api.plugins.PluginInfo
import dev.remodded.recore.api.service.registerService
import dev.remodded.recore.common.Constants
import dev.remodded.recore.common.ReCoreImpl
import dev.remodded.recore.common.ReCorePlatformCommon
import dev.remodded.recore.paper.command.PaperCommandManager
import dev.remodded.recore.paper.data.additional.PaperAdditionalDataManager
import dev.remodded.recore.paper.messaging.channel.PaperChannelMessagingManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class ReCorePaperPlatform(
    override val libraryLoader: LibraryLoader,
) : JavaPlugin(), ReCorePlatformCommon {

    companion object {
        lateinit var INSTANCE: ReCorePaperPlatform
    }

    init {
        INSTANCE = this
    }

    override val commandManager = PaperCommandManager()

    override fun createChannelMessagingManager() = PaperChannelMessagingManager()

    override val platformInfo = PlatformInfo(
        Platform.PAPER,
        server.name,
        server.version,
        server.minecraftVersion,
        Bukkit.getPluginsFolder().toPath(),
    )

    override fun onEnable() {
        ReCoreImpl.init(this)

        ReCore.INSTANCE.serviceProvider.registerService<AdditionalDataManager, PaperAdditionalDataManager>()
    }

    override fun getPluginInfo(): PluginInfo {
        return PluginInfo(
            Constants.ID,
            Constants.NAME,
            Constants.VERSION,
            this,
        )
    }
}
