package dev.remodded.recore.velocity

import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.api.proxy.ProxyServer
import dev.remodded.recore.api.lib.LibraryLoader
import dev.remodded.recore.api.platform.Platform
import dev.remodded.recore.api.platform.PlatformInfo
import dev.remodded.recore.common.ReCoreImpl
import dev.remodded.recore.common.ReCorePlatformCommon
import dev.remodded.recore.velocity.command.VelocityCommandManager
import dev.remodded.recore.velocity.messaging.channel.VelocityChannelMessagingManager
import java.nio.file.Path

class ReCoreVelocityPlatform(
    server: ProxyServer,
    override val libraryLoader: LibraryLoader,
    dataFolder: Path,
) : ReCorePlatformCommon {

    override val commandManager = VelocityCommandManager(server)

    override val platformInfo = PlatformInfo(
        Platform.VELOCITY,
        server.version.name,
        server.version.version,
        "[${ProtocolVersion.MINIMUM_VERSION} - ${ProtocolVersion.MAXIMUM_VERSION}]",
        dataFolder,
    )

    init {
        ReCoreImpl.init(this)
    }

    override fun createChannelMessagingManager() = VelocityChannelMessagingManager()
    override fun getPluginInfo() = ReCoreVelocity.INSTANCE.getPluginInfo()
}
