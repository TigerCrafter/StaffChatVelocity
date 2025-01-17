package io.github.tigercrafter.staffchatvelocity.staffchat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StaffChatCommand {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> helloNode = BrigadierCommand.literalArgumentBuilder("staffchat")
                .requires(source -> {
                    if (source.hasPermission("staffchat.use")) {
                        return true;
                    }
                    return false;
                })
                .executes(context -> {
                    CommandSource source = context.getSource();
                    Component message = Component.text("You have to provide a message", NamedTextColor.RED);
                    source.sendMessage(message);
                    return 0;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString())
                        .executes(context -> {
                            CommandSource source = context.getSource();
                            String name;
                            if (source instanceof Player player) {
                                name = player.getUsername();
                            } else if (source instanceof ConsoleCommandSource) {
                                name = "Console";
                            } else {
                                return 0;
                            }
                            String message = context.getArgument("message", String.class);
                            Component text = Component.text("Staff | ", NamedTextColor.LIGHT_PURPLE)
                                    .append(Component.text(name + ": ", NamedTextColor.DARK_GRAY))
                                    .append(Component.text(message, NamedTextColor.RED));
                            proxyServer.getConsoleCommandSource().sendMessage(text);
                            proxyServer.getAllPlayers().forEach(player -> {
                                if (player.hasPermission("staffchat.see")) {
                                    player.sendMessage(text);
                                }
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();
        return new BrigadierCommand(helloNode);
    }
}
