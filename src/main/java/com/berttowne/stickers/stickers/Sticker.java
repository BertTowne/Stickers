package com.berttowne.stickers.stickers;

import net.kyori.adventure.text.Component;

import java.util.List;

public class Sticker {

    private String name;
    private List<Component> description;
    private List<Component> frames;
    private int refreshRate; // in ticks
    private String permission;

    public Sticker(String name, List<Component> description, List<Component> frames, int refreshRate, String permission) {
        this.name = name;
        this.description = description;
        this.frames = frames;
        this.refreshRate = refreshRate;
        this.permission = permission;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Component> getDescription() {
        return description;
    }

    public void setDescription(List<Component> description) {
        this.description = description;
    }

    public List<Component> getFrames() {
        return frames;
    }

    public Component getFrame(int index) {
        if (frames.isEmpty()) throw new IllegalStateException("Sticker has no frames!");
        if (index >= frames.size()) throw new IndexOutOfBoundsException("Frame index out of bounds: " + index);

        return frames.get(index);
    }

    public void setFrames(List<Component> frames) {
        this.frames = frames;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

}