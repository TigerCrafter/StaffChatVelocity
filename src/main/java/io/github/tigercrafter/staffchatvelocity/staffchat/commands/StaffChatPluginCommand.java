package io.github.tigercrafter.staffchatvelocity.staffchat.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.github.tigercrafter.staffchatvelocity.discord.Bot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.io.IOException;

public class StaffChatPluginCommand {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxyServer, final Logger logger, Bot bot, final YamlDocument... configurationFiles) {
        LiteralCommandNode<CommandSource> commandNode = BrigadierCommand.literalArgumentBuilder("staffchatplugin")
                .executes(context -> {
                    CommandSource source = context.getSource();
                    if (!source.hasPermission("staffchat.manage.help")) {
                        source.sendMessage(Component.text("You don't have permission to display help", NamedTextColor.RED));
                        return 0;
                    }
                    source.sendMessage(Component.text("-----HELP-----", NamedTextColor.AQUA)
                            .append(Component.text("\n/staffchatplugin ", NamedTextColor.DARK_GRAY).append(Component.text("Shows this Info", NamedTextColor.GRAY)))
                            .append(Component.text("\n/staffchatplugin reload ", NamedTextColor.DARK_GRAY).append(Component.text("Reloads the Configuration Files", NamedTextColor.GRAY))));
                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("reload");
                            return builder.buildFuture();
                        })
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
                            String action = context.getArgument("action", String.class);
                            if (action.equals("reload")) {
                                if (!source.hasPermission("staffchat.manage.reload")) {
                                    source.sendMessage(Component.text("You don't have permission to reload the Plugin", NamedTextColor.RED));
                                    return 0;
                                }
                                for (YamlDocument configurationFile : configurationFiles) {
                                    try {
                                        configurationFile.reload();
                                    } catch (IOException e) {
                                        logger.error("Error reloading Plugin");
                                        source.sendMessage(Component.text("Error reloading Plugin", NamedTextColor.DARK_RED));
                                        return 0;
                                    }
                                }
                                bot.reload();
                                logger.info(name + " has reloaded the Plugin");
                                source.sendMessage(Component.text("Successfully reloaded Plugin", NamedTextColor.GREEN));
                                return Command.SINGLE_SUCCESS;
                            }
                            source.sendMessage(Component.text("The action " + action + " wasn't found", NamedTextColor.RED));
                            return 0;
                        })
                )
                .build();
        return new BrigadierCommand(commandNode);
    }
}
