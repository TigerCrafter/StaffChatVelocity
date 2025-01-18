package io.github.tigercrafter.staffchatvelocity.staffchat;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.github.tigercrafter.staffchatvelocity.discord.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public class DiscordStaffChat extends ListenerAdapter {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final YamlDocument config;
    private final Bot bot;

    public DiscordStaffChat(ProxyServer proxyServer, Logger logger, YamlDocument config, Bot bot) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.config = config;
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!bot.enabled || event.getAuthor() == bot.discordBot.getSelfUser() || !event.getChannel().getId().equals(config.getString("discord-channel-id"))) {
            return;
        }
        String author = event.getAuthor().getEffectiveName();
        String message = event.getMessage().getContentRaw();
        logger.info(config.getString("console-staffchat-message").replace("<author>", author).replace("<message>", message) + "\u001B[0m");
        proxyServer.getAllPlayers().stream().filter(player -> player.hasPermission("staffchat.see"))
                .forEach(player -> player.sendMessage(Component.text(config.getString("player-staffchat-message").replace("<author>", author).replace("<message>", message))));
    }

}
