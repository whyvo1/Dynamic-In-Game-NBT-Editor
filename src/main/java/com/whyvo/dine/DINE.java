package com.whyvo.dine;

import com.mojang.blaze3d.platform.InputConstants;
import com.whyvo.dine.command.DineCommand;
import com.whyvo.dine.config.Config;
import com.whyvo.dine.context.BScreenManager;
import com.whyvo.dine.screen.DINEScreen;
import com.whyvo.dine.util.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(DINE.MOD_ID)
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

    public DINE(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::onRegisterKeyMappings);
        modEventBus.addListener(this::onRegisterReload);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onTick);
    }

    public DINE(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegisterKeyMappings);
        modEventBus.addListener(this::onRegisterReload);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onTick);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_BINE.get());
        event.register(OPEN_TARGET.get());
    }

    private void onRegisterCommands(RegisterClientCommandsEvent event) {
        DineCommand.register(event.getDispatcher());
    }

    private void onTick(TickEvent.ClientTickEvent event) {
        Minecraft client = Minecraft.getInstance();
        if(event.phase == TickEvent.Phase.START) {
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
        else if (event.phase == TickEvent.Phase.END) {
            if(shouldOpenBine) {
                if(client.screen == null) {
                    client.setScreen(new DINEScreen());
                }
                shouldOpenBine = false;
            }
        }
    }

    private void onRegisterReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ResourceManagerReloadListener() {
            private static final String NAME = "dine:reload";

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                Config.refreshProperties();
            }

            @Override
            public String getName() {
                return NAME;
            }
        });
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
//            event.enqueueWork(Config::refreshProperties);

            LOGGER.info("DINE is loaded!");
        }
    }
}
