package com.berttowne.stickers.stickers;

import com.berttowne.stickers.StickersPlugin;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.triumphteam.gui.guis.Gui;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class StickerCommands {

    public static final LiteralCommandNode<CommandSourceStack> STICKERS_COMMAND = Commands.literal("stickers")
            .requires(sender -> sender.getExecutor() instanceof Player)
            .executes(ctx -> {
                // Show help message
                CommandSourceStack source = ctx.getSource();
                Player player = (Player) source.getExecutor();
                StickerService stickerService = StickersPlugin.getPlugin(StickersPlugin.class).getInjector().getInstance(StickerService.class);

                player.sendMessage(stickerService.showStickersChat(player));

                // TODO: Merge resource pack font into default mc font
                /*Component stickers = stickerService.showStickers(player);
                Component title = Component.text()
                        .append(StickerService.BLANK_INVENTORY)
                        .append(stickers)
                        .append(Component.text("Available Stickers:").color(NamedTextColor.GRAY)).build();

                player.sendMessage("\n\n\n");
                player.sendMessage(title);

                Gui stickerGui = Gui.gui()
                        .title(title)
                        .rows(6)
                        .create();

                stickerGui.open(player);*/

                return Command.SINGLE_SUCCESS;
            })
            .build();

    public static final LiteralCommandNode<CommandSourceStack> PLACE_STICKER_COMMAND = Commands.literal("placesticker")
            .then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        StickerService stickerService = StickersPlugin.getPlugin(StickersPlugin.class).getInjector().getInstance(StickerService.class);
                        Sticker sticker = stickerService.getSticker(StringArgumentType.getString(ctx, "name"));
                        CommandSourceStack source = ctx.getSource();
                        Player player = (Player) source.getExecutor();

                        if (sticker == null) {
                            player.sendRichMessage("<red>Sticker not found!");
                            return Command.SINGLE_SUCCESS;
                        }

                        stickerService.placeSticker(player, sticker);

                        return Command.SINGLE_SUCCESS;
                    }))
            .build();

}