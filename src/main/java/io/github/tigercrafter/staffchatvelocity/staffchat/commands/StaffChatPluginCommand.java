package io.github.tigercrafter.staffchatvelocity.staffchat.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.github.tigercrafter.staffchatvelocity.StaffChatVelocity;
import io.github.tigercrafter.staffchatvelocity.discord.DiscordBotManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;

public class StaffChatPluginCommand {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxyServer, final Logger logger, final YamlDocument config, final DiscordBotManager discordBotManager, final YamlDocument[] configurationFilesToReload) {
        LiteralCommandNode<CommandSource> commandNode = BrigadierCommand.literalArgumentBuilder("staffchatplugin")
                .executes(context -> {
                    CommandSource source = context.getSource();
                    if (!source.hasPermission("staffchat.manage")) {
                        source.sendMessage(Component.text("You don't have permission to execute this Command", NamedTextColor.RED));
                        return 0;
                    }
                    source.sendMessage(Component.text("-----HELP-----", NamedTextColor.AQUA)
                            .append(Component.text("\n/staffchatplugin info", NamedTextColor.RED).append(Component.text("Displays Information", NamedTextColor.GREEN)))
                            .append(Component.text("\n/staffchatplugin reload ", NamedTextColor.RED).append(Component.text("Reloads the Plugin", NamedTextColor.GREEN))));
                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            CommandSource source = context.getSource();
                            if (source.hasPermission("staffchat.manage")) {
                                builder.suggest("info");
                                builder.suggest("reload");
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            CommandSource source = context.getSource();
                            String action = context.getArgument("action", String.class);
                            if (action.equals("reload")) {
                                if (!source.hasPermission("staffchat.manage.reload")) {
                                    source.sendMessage(Component.text("You don't have permission to reload the Plugin", NamedTextColor.RED));
                                    return 0;
                                }
                                source.sendMessage(Component.text("Usage /staffchatplugin reload (all|config|discord)"));
                                return 0;
                            } else if (action.equals("info")) {
                                Optional<PluginContainer> plugin = proxyServer.getPluginManager().fromInstance(StaffChatVelocity.instance);
                                if (plugin.isEmpty()) {
                                    source.sendMessage(Component.text("Something went wrong", NamedTextColor.DARK_RED));
                                    return 0;
                                }
                                if (discordBotManager.enabled) {
                                    source.sendMessage(Component.text("-----INFO-----", NamedTextColor.AQUA)
                                            .append(Component.text("\nStaffChatVelocity Version: ", NamedTextColor.GREEN).append(Component.text("\n" + StaffChatVelocity.version, NamedTextColor.WHITE)))
                                            .append(Component.text("\nStaffChatVelocity Description: ", NamedTextColor.GREEN).append(Component.text("\n" + StaffChatVelocity.description, NamedTextColor.WHITE)))
                                            .append(Component.text("\nDiscordIntegration: ", NamedTextColor.GREEN).append(Component.text("\nEnabled", NamedTextColor.WHITE)))
                                            .append(Component.text("\nDiscordIntegration-Status: ", NamedTextColor.GREEN).append(Component.text("\n" + (discordBotManager.discordBot.getStatus().isInit() ? "Initialized" : "Not Initialized"), NamedTextColor.WHITE)))
                                            .append(Component.text("\nDiscordIntegration-Channel: ", NamedTextColor.GREEN).append(Component.text("\n" + discordBotManager.config.getString("discord-channel-id"), NamedTextColor.WHITE)))
                                            .append(Component.text("\nDiscordIntegration-BotName: ", NamedTextColor.GREEN).append(Component.text("\n" + discordBotManager.discordBot.getSelfUser().getName(), NamedTextColor.WHITE)))
                                            .append(Component.text("\nStaffChatMessage-Player: ", NamedTextColor.GREEN).append(Component.text("\n" + config.getString("player-staffchat-message"), NamedTextColor.WHITE)))
                                            .append(Component.text("\nStaffChatMessage-Console: ", NamedTextColor.GREEN).append(Component.text("\n" + config.getString("console-staffchat-message"), NamedTextColor.WHITE))));
                                } else {
                                    source.sendMessage(Component.text("-----INFO-----", NamedTextColor.AQUA)
                                            .append(Component.text("\nStaffChatVelocity Version: ", NamedTextColor.GREEN).append(Component.text("\n" + StaffChatVelocity.version, NamedTextColor.WHITE)))
                                            .append(Component.text("\nStaffChatVelocity Description: ", NamedTextColor.GREEN).append(Component.text("\n" + StaffChatVelocity.description, NamedTextColor.WHITE)))
                                            .append(Component.text("\nDiscordIntegration: ", NamedTextColor.GREEN).append(Component.text("\nDisabled", NamedTextColor.WHITE)))
                                            .append(Component.text("\nStaffChatMessage-Player: ", NamedTextColor.GREEN).append(Component.text("\n" + config.getString("player-staffchat-message"), NamedTextColor.WHITE)))
                                            .append(Component.text("\nStaffChatMessage-Console: ", NamedTextColor.GREEN).append(Component.text("\n" + config.getString("console-staffchat-message"), NamedTextColor.WHITE))));
                                }
                                return Command.SINGLE_SUCCESS;
                            }
                            source.sendMessage(Component.text("The action " + action + " wasn't found", NamedTextColor.RED));
                            return 0;
                        })
                    .then(BrigadierCommand.requiredArgumentBuilder("parameter", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                String action = context.getArgument("action", String.class);
                                if (action.equals("reload")) {
                                    builder.suggest("all");
                                    builder.suggest("config");
                                    builder.suggest("discord");
                                }
                                return builder.buildFuture();
                            })
                            .executes(context -> {
                                CommandSource source = context.getSource();
                                String action = context.getArgument("action", String.class);
                                String parameter = context.getArgument("parameter", String.class);
                                String name;
                                if (source instanceof Player player) {
                                    name = player.getUsername();
                                } else if (source instanceof ConsoleCommandSource) {
                                    name = "Console";
                                } else {
                                    return 0;
                                }
                                if (action.equals("reload")) {
                                    if (parameter.equals("all")) {
                                        if (reloadAll(logger, configurationFilesToReload, discordBotManager)) {
                                            source.sendMessage(Component.text("Successfully reloaded plugin", NamedTextColor.GREEN));
                                            logger.info(name + " has reloaded the plugin");
                                            return Command.SINGLE_SUCCESS;
                                        } else {
                                            source.sendMessage(Component.text("Error reloading plugin", NamedTextColor.DARK_RED));
                                        }
                                    } else if (parameter.equals("config")) {
                                        if (reloadConfig(logger, configurationFilesToReload)) {
                                            source.sendMessage(Component.text("Successfully reloaded configuration files", NamedTextColor.GREEN));
                                            logger.info(name + " has reloaded the configuration files");
                                            return Command.SINGLE_SUCCESS;
                                        } else {
                                            source.sendMessage(Component.text("Error reloading the configuration files", NamedTextColor.DARK_RED));
                                            return 0;
                                        }
                                    } else if (parameter.equals("discord")) {
                                        if (discordBotManager.reload()) {
                                            source.sendMessage(Component.text("Successfully reloaded discord bot", NamedTextColor.GREEN));
                                            logger.info(name + " has reloaded the discord bot");
                                            return Command.SINGLE_SUCCESS;
                                        } else {
                                            source.sendMessage(Component.text("Error reloading discord bot", NamedTextColor.DARK_RED));
                                            return 0;
                                        }
                                    }
                                }
                                source.sendMessage(Component.text("Unknown arguments. For more help do /staffchatplugin", NamedTextColor.RED));
                                return 0;
                            })
                    )
                )
                .build();
        return new BrigadierCommand(commandNode);
    }
    private static boolean reloadAll(Logger logger, YamlDocument[] configurationFilesToReload, DiscordBotManager discordBotManager) {
        return reloadConfig(logger, configurationFilesToReload) && discordBotManager.reload();
    }
    private static boolean reloadConfig(Logger logger, YamlDocument[] configurationFilesToReload) {
        for (YamlDocument configurationFile: configurationFilesToReload) {
            try {
                configurationFile.reload();
            } catch (IOException e) {
                logger.error("Error reloading Configuration Files");
                return false;
            }
        }
        return true;
    }
}
