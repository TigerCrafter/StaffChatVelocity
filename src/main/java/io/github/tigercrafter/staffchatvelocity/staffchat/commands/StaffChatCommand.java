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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

public class StaffChatCommand {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxyServer, final Logger logger, final Bot bot, final YamlDocument config) {
        LiteralCommandNode<CommandSource> commandNode = BrigadierCommand.literalArgumentBuilder("staffchat")
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
                            String author;
                            if (source instanceof Player player) {
                                author = player.getUsername();
                            } else if (source instanceof ConsoleCommandSource) {
                                author = "Console";
                            } else {
                                return 0;
                            }
                            String message = context.getArgument("message", String.class);
                            logger.info(config.getString("console-staffchat-message").replace("<author>", author).replace("<message>", message) + "\u001B[0m");
                            proxyServer.getAllPlayers().forEach(player -> {
                                if (player.hasPermission("staffchat.see")) {
                                    player.sendMessage(Component.text(config.getString("player-staffchat-message").replace("<author>", author).replace("<message>", message)));
                                }
                            });
                            if (!bot.enabled) {
                                return Command.SINGLE_SUCCESS;
                            }
                            String channelID = config.getString("discord-channel-id");
                            TextChannel channel = bot.discordBot.getTextChannelById(channelID);
                            if (channel == null) {
                                logger.error("Couldn't find TextChannel with ID " + channelID);
                                return 0;
                            }
                            MessageCreateAction messageCreateAction = channel.sendMessage(author + ": " + message);
                            messageCreateAction.queue();
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();
        return new BrigadierCommand(commandNode);
    }
}
