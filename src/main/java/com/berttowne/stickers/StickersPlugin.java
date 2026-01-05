package com.berttowne.stickers;

import com.berttowne.stickers.util.injection.*;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class StickersPlugin extends JavaPlugin implements InjectionRoot {

    @Inject private Injector injector;

    @Override
    public void onLoad() {
        AppInjector.registerInjectionRoot(this);
        AppInjector.registerRootModule(new InjectionModule(this));
    }

    @Override
    public void onEnable() {
        AppInjector.boot();

        this.saveDefaultConfig();

        // Boot Services and register Listeners
        GuiceServiceLoader.load(Service.class, getClassLoader()).forEach(Service::onLoad);
        GuiceServiceLoader.load(Listener.class, getClassLoader()).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        GuiceServiceLoader.load(Service.class, getClassLoader()).forEach(Service::onEnable);
    }

    @Override
    public void onDisable() {
        GuiceServiceLoader.load(Service.class, getClassLoader()).forEach(Service::onDisable);
    }

    public Injector getInjector() {
        return injector;
    }

}