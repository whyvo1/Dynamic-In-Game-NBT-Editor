package com.whyvo.dine;

import com.mojang.blaze3d.platform.InputConstants;
import com.whyvo.dine.command.DineCommand;
import com.whyvo.dine.config.Config;
import com.whyvo.dine.context.BScreenManager;
import com.whyvo.dine.screen.DINEScreen;
import com.whyvo.dine.util.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.LoggerFactory;

@Mod(value = DINE.MOD_ID, dist = Dist.CLIENT)
public class DINE {
    public static final String MOD_ID = "dine";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final BScreenManager SCREENS = new BScreenManager();

    public static final String BINE_CATEGORY = "key.category.dine";

    private static boolean shouldOpenBine;

    public static void setShouldOpenBine() {
        shouldOpenBine = true;
    }

    private static final Lazy<KeyMapping> OPEN_BINE = Lazy.of(() -> new KeyMapping(
            "key.dine.open_dine",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            BINE_CATEGORY
    ));

    private static final Lazy<KeyMapping> OPEN_TARGET = Lazy.of(() -> new KeyMapping(
            "key.dine.open_target",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            BINE_CATEGORY
    ));

    public DINE(IEventBus modEventBus) {

        modEventBus.addListener(this::onRegisterKeyMappings);
        modEventBus.addListener(this::onRegisterReload);
        modEventBus.addListener(this::onClientSetup);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onPreTick);
        NeoForge.EVENT_BUS.addListener(this::onPostTick);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_BINE.get());
        event.register(OPEN_TARGET.get());
    }

    private void onRegisterCommands(RegisterClientCommandsEvent event) {
        DineCommand.register(event.getDispatcher());
    }

    private void onPreTick(ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();
        if (OPEN_BINE.get().consumeClick()) {
            if (client.screen == null) {
                Util.tryOpenBine(client);
            }
        }
        if (OPEN_TARGET.get().consumeClick()) {
            if (client.screen == null) {
                Util.tryOpenTarget(client);
            }
        }
    }

    private void onPostTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if(shouldOpenBine) {
            if(client.screen == null) {
                client.setScreen(new DINEScreen());
            }
            shouldOpenBine = false;
        }
    }

    private void onRegisterReload(AddClientReloadListenersEvent event) {
        event.addListener(ResourceLocation.fromNamespaceAndPath(MOD_ID, "reload"), new ResourceManagerReloadListener() {
            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                Config.refreshProperties();
            }
        });
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("DINE is loaded!");
    }
}
