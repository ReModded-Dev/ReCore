package dev.remodded.recore.paper.command.source.utils

import dev.remodded.recore.api.command.source.CommandSender
import dev.remodded.recore.api.entity.Player
import dev.remodded.recore.paper.command.source.PaperCommandSender
import dev.remodded.recore.paper.entity.utils.native
import dev.remodded.recore.paper.entity.utils.wrap

fun CommandSender.native(): org.bukkit.command.CommandSender = when(this) {
    is Player -> this.native()
    is PaperCommandSender -> this.native
    else -> throw IllegalArgumentException("Unknown CommandSender type")
}
fun org.bukkit.command.CommandSender.wrap(): CommandSender = when(this) {
    is org.bukkit.entity.Player -> this.wrap()
    else -> PaperCommandSender(this)
}
