package io.github.tigercrafter.staffchatvelocity.discord;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.github.tigercrafter.staffchatvelocity.staffchat.DiscordStaffChat;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;

public class Bot {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final YamlDocument config;
    public JDA discordBot;
    public boolean enabled = false;

    public Bot(ProxyServer proxyServer, Logger logger, YamlDocument config) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.config = config;
        if (config.getBoolean("enable-discord-integration")) {
            discordBot = JDABuilder.createDefault(config.getString("discord-bot-token")).addEventListeners(new DiscordStaffChat(proxyServer, logger, config, this)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
            enabled = true;
        }
    }

    public void reload() {
        if (config.getBoolean("enable-discord-integration")) {
            discordBot = JDABuilder.createDefault(config.getString("discord-bot-token")).addEventListeners(new DiscordStaffChat(proxyServer, logger, config, this)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
            enabled = true;
        } else {
            enabled = false;
        }
    }
}
