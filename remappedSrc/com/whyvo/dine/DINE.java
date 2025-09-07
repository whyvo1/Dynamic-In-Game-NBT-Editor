package com.whyvo.dine;

import com.mojang.blaze3d.platform.InputConstants;
import com.whyvo.dine.command.DineCommand;
import com.whyvo.dine.config.Config;
import com.whyvo.dine.context.BScreenManager;
import com.whyvo.dine.screen.DINEScreen;
import com.whyvo.dine.util.Util;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DINE implements ClientModInitializer {
	public static final String MOD_ID = "dine";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final BScreenManager SCREENS = new BScreenManager();

	private static boolean shouldOpenBine;

	public static void setShouldOpenBine() {
		shouldOpenBine = true;
	}

	public static final String BINE_CATEGORY = "key.category.dine";

	private static final KeyMapping OPEN_BINE = new KeyMapping(
			"key.dine.open_dine",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			BINE_CATEGORY
	);

	private static final KeyMapping OPEN_TARGET = new KeyMapping(
			"key.dine.open_target",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			BINE_CATEGORY
	);

	@Override
	public void onInitializeClient() {
//		KeyBindingHelper.registerKeyBinding(OPEN_ITEM);
//		KeyBindingHelper.registerKeyBinding(OPEN_BLOCK);
//		KeyBindingHelper.registerKeyBinding(OPEN_ENTITY);
		KeyBindingHelper.registerKeyBinding(OPEN_TARGET);
		KeyBindingHelper.registerKeyBinding(OPEN_BINE);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				DineCommand.register(dispatcher)
		);

		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			if(shouldOpenBine) {
				if(client.screen == null) {
					client.setScreen(new DINEScreen());
				}
				shouldOpenBine = false;
			}
		});

		ClientTickEvents.START_CLIENT_TICK.register((client) -> {
			if (OPEN_BINE.consumeClick()) {
				if (client.screen == null) {
					Util.tryOpenBine(client);
				}
			}
			if (OPEN_TARGET.consumeClick()) {
				if (client.screen == null) {
					Util.tryOpenTarget(client);
				}
			}
		});

//		ClientLifecycleEvents.CLIENT_STARTED.register(client -> Config.refreshProperties());

		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			private static final ResourceLocation RELOAD_ID = ResourceLocation.tryBuild(DINE.MOD_ID, "reload");

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				Config.refreshProperties();
			}

			@Override
			public ResourceLocation getFabricId() {
				return RELOAD_ID;
			}
		});

		LOGGER.info("DINE is loaded!");
	}

}