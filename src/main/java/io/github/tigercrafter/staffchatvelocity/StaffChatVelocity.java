package io.github.tigercrafter.staffchatvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.tigercrafter.staffchatvelocity.staffchat.StaffChatCommand;
import org.slf4j.Logger;

@Plugin(id = "staffchatvelocity",
        name = "StaffChatVelocity",
        version = "1.0-SNAPSHOT",
        description = "A simple StaffChat Plugin for Velocity",
        url = "https://github.com/TigerCrafter/StaffChatVelocity",
        authors = {"TigerCrafter"})
public class StaffChatVelocity {

    private final ProxyServer proxyServer;
    private final Logger logger;

    @Inject
    public StaffChatVelocity(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = proxyServer.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("staffchat")
                .aliases("sc")
                .plugin(this)
                .build();
        BrigadierCommand commandToRegister = StaffChatCommand.createBrigadierCommand(proxyServer);
        commandManager.register(commandMeta, commandToRegister);
        logger.info("Successfully initialized!");
    }
}
