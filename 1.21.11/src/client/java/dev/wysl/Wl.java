package dev.wysl;

import com.mojang.blaze3d.platform.InputConstants;
import dev.wysl.ui.Hb;
import dev.wysl.ui.Panel;
import dev.wysl.ui.Ui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;

public class Wl implements ClientModInitializer {
    public static KeyMapping key;

    public void onInitializeClient() {
        KeyMapping k = key = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.wysslib.open", InputConstants.KEY_INSERT, KeyMapping.Category.register(Identifier.fromNamespaceAndPath("wysslib", "main"))));
        ClientTickEvents.END_CLIENT_TICK.register(m -> {
            while (k.consumeClick()) if (m.screen == null) m.setScreen(hub(null));
        });
    }

    public static Screen hub(Screen parent) {
        return new Panel(parent, new Hb(), "Wysss Library", key);
    }

    public static Screen screen(Screen parent, Ui ui, String title, KeyMapping closeKey) {
        return new Panel(parent, ui, title, closeKey);
    }
}
