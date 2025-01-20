package io.github.tigercrafter.staffchatvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import io.github.tigercrafter.staffchatvelocity.discord.DiscordBotManager;
import io.github.tigercrafter.staffchatvelocity.staffchat.commands.StaffChatCommand;
import io.github.tigercrafter.staffchatvelocity.staffchat.commands.StaffChatPluginCommand;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

@Plugin(id = "staffchatvelocity",
        name = "StaffChatVelocity",
        version = "1.0-SNAPSHOT",
        description = "A simple StaffChat Plugin for Velocity",
        url = "https://github.com/TigerCrafter/StaffChatVelocity",
        authors = {"TigerCrafter"})
public class StaffChatVelocity {
    public static String version = "1.0-SNAPSHOT";
    public static String description = "A simple StaffChat Plugin for Velocity";

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    public static StaffChatVelocity instance;
    private static YamlDocument config;

    @Inject
    public StaffChatVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        instance = this;

        try {
            config = YamlDocument.create(new File(dataDirectory.toFile(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
            );

            config.update();
            config.save();
        } catch (IOException e) {
            logger.error("Couldn't create or load config file! Plugin will now shutdown");
            Optional<PluginContainer> plugin = proxyServer.getPluginManager().fromInstance(instance);
            plugin.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());

        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = proxyServer.getCommandManager();
        CommandMeta staffChatCommandMeta = commandManager.metaBuilder("staffchat")
                .aliases("sc")
                .plugin(this)
                .build();
        CommandMeta staffChatPluginCommandMeta = commandManager.metaBuilder("staffchatplugin")
                .aliases("scpl")
                .plugin(this)
                .build();
        DiscordBotManager discordBotManager = new DiscordBotManager(proxyServer, logger, config);
        BrigadierCommand staffChatCommand = StaffChatCommand.createBrigadierCommand(proxyServer, logger, discordBotManager, config);
        BrigadierCommand staffChatPluginCommand = StaffChatPluginCommand.createBrigadierCommand(proxyServer, logger, config, discordBotManager, new YamlDocument[]{config});
        commandManager.register(staffChatCommandMeta, staffChatCommand);
        commandManager.register(staffChatPluginCommandMeta, staffChatPluginCommand);
        logger.info("Successfully initialized!");
    }
}
