package com.berttowne.stickers.util.injection;

public interface InjectionRoot {

    // Attempt to auto handle injection for plugins.
    @SuppressWarnings("unused")
    default void onLoad() {
        AppInjector.registerInjectionRoot(this);
    }

}