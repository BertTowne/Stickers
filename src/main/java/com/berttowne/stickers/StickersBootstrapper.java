package com.berttowne.stickers;

import com.berttowne.stickers.stickers.StickerCommands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class StickersBootstrapper implements PluginBootstrap {

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        // Register commands
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(StickerCommands.STICKERS_COMMAND);
            commands.registrar().register(StickerCommands.PLACE_STICKER_COMMAND);
        });
    }

}