package com.berttowne.stickers.stickers;

import com.berttowne.stickers.StickersPlugin;
import com.berttowne.stickers.util.Scheduler;
import com.berttowne.stickers.util.TimeFormatter;
import com.berttowne.stickers.util.injection.Service;
import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Singleton
@AutoService({Service.class, Listener.class})
public class StickerService implements Service, Listener {

    // CUSTOM CHARS
    public static final Component BLANK_INVENTORY = Component.text('\uEff1').font(Key.key("minecraft", "default")).color(NamedTextColor.WHITE);
    public static final Component STICKER_LEGUNDO = Component.text('\uEff1').font(Key.key("minecraft", "default")).color(NamedTextColor.WHITE);
    public static final Component STICKER_SMILE_1 = Component.text('\uEff2').font(Key.key("minecraft", "default")).color(NamedTextColor.WHITE);
    public static final Component STICKER_SMILE_2 = Component.text('\uEff3').font(Key.key("minecraft", "default")).color(NamedTextColor.WHITE);
    public static final Component STICKER_SMILE_3 = Component.text('\uEff4').font(Key.key("minecraft", "default")).color(NamedTextColor.WHITE);

    @Inject private Gson gson;
    @Inject private StickersPlugin plugin;

    private final List<Sticker> stickers = Lists.newArrayList();
    private final List<PlacedSticker> placedStickers = Lists.newArrayList();

    private File stickersFile;
    private long stickerCooldown;

    @Override
    public void onEnable() {
        this.stickersFile = new File(plugin.getDataFolder(), "stickers.json");
        this.stickerCooldown = plugin.getConfig().getLong("sticker-cooldown", 30000);

        loadStickers();

        // TODO: Sticker GUI and resource pack
        // TODO: Remove after testing
        stickers.add(new Sticker(
                "Emojis",
                List.of(MiniMessage.miniMessage().deserialize("<gray>Source: https://github.com/iamBijoyKar/emojis-pixel-art/tree/master")),
                List.of(STICKER_SMILE_1, STICKER_SMILE_2, STICKER_SMILE_3),
                20,
                "stickers.use.test"
        ));

        stickers.add(new Sticker(
                "Legundo",
                List.of(MiniMessage.miniMessage().deserialize("<gray>Sticker used for testing.")),
                List.of(STICKER_LEGUNDO),
                0,
                "stickers.use.legundo"
        ));
    }

    @Override
    public void onDisable() {
        saveStickers();

        placedStickers.forEach(PlacedSticker::delete);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove any active stickers a player may have
        placedStickers.removeIf(placedSticker -> {
            if (!placedSticker.getPlacedBy().equals(event.getPlayer())) return false;

            placedSticker.delete();
            return true;

        });
    }

    public void loadStickers() {
        if (!stickersFile.exists()) {
            plugin.getLogger().warning("Stickers file not found! Creating default stickers.json...");
            saveStickers();
            return;
        }

        try (FileReader reader = new FileReader(stickersFile)) {
            Sticker[] loadedStickers = gson.fromJson(reader, Sticker[].class);

            stickers.clear();
            stickers.addAll(List.of(loadedStickers));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveStickers() {
        try (FileWriter writer = new FileWriter(stickersFile)) {
            gson.toJson(stickers, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public Sticker getSticker(String name) {
        return stickers.stream()
                .filter(sticker -> sticker.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public long getStickerCooldown() {
        return stickerCooldown;
    }

    public Component showStickers(Player player) {
        if (stickers.isEmpty()) throw new IllegalStateException("No stickers found!");

        TextComponent.Builder builder = Component.text();

        for (Sticker sticker : stickers) {
            if (!player.hasPermission(sticker.getPermission())) continue;

            builder.append(sticker.getFrame(0));
        }

        return builder.build();
    }

    public Component showStickersChat(Player player) {
        if (stickers.isEmpty()) throw new IllegalStateException("No stickers found!");

        TextComponent.Builder builder = Component.text().append(Component.text("Available Stickers:\n"));

        for (Sticker sticker : stickers) {
            if (!player.hasPermission(sticker.getPermission())) continue;

            builder.append(sticker.getFrame(0).hoverEvent(
                    Component.text()
                            .append(Component.text(sticker.getName() + "\n").color(NamedTextColor.YELLOW))
                            .append(sticker.getDescription())
                            .append(MiniMessage.miniMessage().deserialize("\n\n<white>Click<gray> to place this sticker at target location"))
                            .build()
            ).clickEvent(ClickEvent.runCommand("/placesticker " + sticker.getName())));
        }

        return builder.build();
    }

    public void placeSticker(Player player, Sticker sticker) {
        if (sticker == null) {
            player.sendRichMessage("<red>Sticker not found!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1f);
            return;
        }

        if (!player.hasPermission(sticker.getPermission())) {
            player.sendRichMessage("<red>You do not have permission to place this sticker!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1f);
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null) {
            player.sendRichMessage("<red>You must be looking at a surface!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1f);
            return;
        }

        BlockFace targetFace = player.getTargetBlockFace(5);
        if (targetFace == null) {
            player.sendRichMessage("<red>You must be looking at a surface!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1f);
            return;
        }

        NamespacedKey stickerKey = new NamespacedKey(plugin, "last_placed");
        if (player.getPersistentDataContainer().has(stickerKey)) {
            long lastPlaced = player.getPersistentDataContainer().get(stickerKey, PersistentDataType.LONG);

            if (lastPlaced + stickerCooldown > System.currentTimeMillis()) {
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>You cannot place another sticker for <white>" +
                        TimeFormatter.formatTimeDifference((lastPlaced + stickerCooldown) - System.currentTimeMillis(), false)));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1f);
                return;
            }
        }

        PlacedSticker placedSticker = new PlacedSticker(sticker, player, targetBlock, targetFace, stickerCooldown);
        placedStickers.add(placedSticker);

        Scheduler.later(placedSticker::delete, stickerCooldown / 50);

        targetBlock.getWorld().playEffect(targetBlock.getLocation(), Effect.STEP_SOUND, Material.SLIME_BLOCK);

        player.getPersistentDataContainer().set(stickerKey, PersistentDataType.LONG, System.currentTimeMillis());
    }

}