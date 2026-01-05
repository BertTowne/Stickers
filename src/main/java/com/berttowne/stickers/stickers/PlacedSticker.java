package com.berttowne.stickers.stickers;

import com.berttowne.stickers.StickersPlugin;
import com.berttowne.stickers.util.Scheduler;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.HologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlacedSticker {

    private final StickerService stickerService = StickersPlugin.getPlugin(StickersPlugin.class).getInjector()
            .getInstance(StickerService.class);
    private final HologramManager hologramManager = FancyHologramsPlugin.get().getHologramManager();

    private final Sticker sticker;
    private final Hologram hologram;
    private final long placedAt;
    private final Player placedBy;

    private int currentFrame = 0;

    public PlacedSticker(@NotNull Sticker sticker, @NotNull Player placedBy, @NotNull Block block, BlockFace blockFace, long stickerCooldown) {
        this.sticker = sticker;
        this.placedAt = System.currentTimeMillis();
        this.placedBy = placedBy;

        Location location = block.getRelative(blockFace).getLocation();
        TextHologramData hologramData = new TextHologramData(placedBy.getName(), location);
        hologramData.setBillboard(Display.Billboard.FIXED);
        hologramData.setBackground(Color.fromARGB(0));
        hologramData.setTextShadow(false);
        hologramData.setSeeThrough(false);
        hologramData.setPersistent(false);
        hologramData.setText(List.of(
                "<yellow>Placed by:",
                "<white>" + placedBy.getName(),
                MiniMessage.miniMessage().serialize(sticker.getFrame(0))
        ));

        this.hologram = hologramManager.create(hologramData);
        hologramManager.addHologram(hologram);

        adjustRotation(blockFace);
        startFrameTask(sticker.getRefreshRate());
        scheduleDeletion(stickerCooldown);
    }

    private void adjustRotation(@NotNull BlockFace blockFace) {
        HologramData data = hologram.getData();

        RayTraceResult rayTraceResult = placedBy.rayTraceBlocks(5);
        if (rayTraceResult == null) return;

        Location hitLocation = rayTraceResult.getHitPosition().toLocation(placedBy.getWorld());
        data.setLocation(hitLocation);
        
        switch (blockFace) {
            case UP -> data.setLocation(data.getLocation().setRotation(getYaw(), -180).add(0, 0.02, 0));
            case DOWN -> data.setLocation(data.getLocation().setRotation(getYaw(), 180).subtract(0, 0.02, 0));
            case NORTH -> data.setLocation(data.getLocation().setRotation(180, 0).subtract(0, 0, 0.02));
            case EAST -> data.setLocation(data.getLocation().setRotation(-90, 0).add(0.02, 0, 0));
            case SOUTH -> data.setLocation(data.getLocation().setRotation(0, 0).add(0, 0, 0.02));
            case WEST -> data.setLocation(data.getLocation().setRotation(90, 0).subtract(0.02, 0, 0));
            default -> data.setLocation(data.getLocation().setRotation(0, 0));
        }

        hologram.forceUpdate();
        hologram.queueUpdate();
    }

    private void startFrameTask(long refreshRate) {
        if (sticker.getFrames().size() <= 1) return; // Skip for static stickers

        TextHologramData data = (TextHologramData) hologram.getData();

        Scheduler.repeatUntil(() -> {
            currentFrame = (currentFrame + 1) % sticker.getFrames().size();
            data.setText(List.of(data.getText().get(0), data.getText().get(1),
                    MiniMessage.miniMessage().serialize(sticker.getFrame(currentFrame))));

            hologram.forceUpdate();
            hologram.queueUpdate();
        }, refreshRate, refreshRate, () -> placedAt + stickerService.getStickerCooldown() <= System.currentTimeMillis());
    }

    private void scheduleDeletion(long stickerCooldown) {
        Scheduler.later(this::delete, stickerCooldown / 50);
    }

    private float getYaw() {
        return (placedBy.getEyeLocation().getYaw() + 180) % 360;
    }

    public void delete() {
        hologramManager.removeHologram(hologram);
    }

    public Sticker getSticker() {
        return sticker;
    }

    public Player getPlacedBy() {
        return placedBy;
    }

    public long getPlacedAt() {
        return placedAt;
    }

}