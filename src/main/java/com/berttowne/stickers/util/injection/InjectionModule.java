package com.berttowne.stickers.util.injection;

import com.berttowne.stickers.StickersPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.io.IOException;

public class InjectionModule extends AbstractModule {

    private final StickersPlugin plugin;

    public InjectionModule(StickersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        AppInjector.getServices(Module.class).forEach(this::install);

        bind(StickersPlugin.class).toInstance(this.plugin);
        bind(Server.class).toInstance(Bukkit.getServer());
        bind(Gson.class).toInstance(new GsonBuilder()
                .registerTypeAdapter(Component.class, new TypeAdapter<Component>() {
                    // Automatically serialize/deserialize Components as MiniMessage Strings for ease of use

                    @Override
                    public void write(JsonWriter out, Component value) throws IOException {
                        out.value(MiniMessage.miniMessage().serialize(value));
                    }

                    @Override
                    public Component read(JsonReader in) throws IOException {
                        return MiniMessage.miniMessage().deserialize(in.nextString());
                    }
                })
                .setPrettyPrinting()
                .create());
    }

}