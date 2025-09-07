package com.whyvo.dine;

import com.whyvo.dine.command.DineCommand;
import com.whyvo.dine.config.Config;
import com.whyvo.dine.context.BScreenManager;
import com.whyvo.dine.screen.DINEScreen;
import com.whyvo.dine.util.Compat1215;
import com.whyvo.dine.util.Util;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
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

	private static final KeyBinding OPEN_BINE = new KeyBinding(
			"key.dine.open_dine",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			BINE_CATEGORY
	);

	private static final KeyBinding OPEN_TARGET = new KeyBinding(
			"key.dine.open_target",
			InputUtil.Type.KEYSYM,
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
				if(client.currentScreen == null) {
					client.setScreen(new DINEScreen());
				}
				shouldOpenBine = false;
			}
		});

		ClientTickEvents.START_CLIENT_TICK.register((client) -> {
			if (OPEN_BINE.wasPressed()) {
				if (client.currentScreen == null) {
					Util.tryOpenBine(client);
				}
			}
			if (OPEN_TARGET.wasPressed()) {
				if (client.currentScreen == null) {
					Util.tryOpenTarget(client);
				}
			}
		});


		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			private static final Identifier RELOAD_ID = Identifier.of(DINE.MOD_ID, "reload");

			@Override
			public void reload(ResourceManager resourceManager) {
				Config.refreshProperties();
			}

			@Override
			public Identifier getFabricId() {
				return RELOAD_ID;
			}
		});

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> Compat1215.init());

		LOGGER.info("DINE is loaded!");
	}

}