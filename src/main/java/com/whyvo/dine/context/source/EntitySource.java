package com.whyvo.dine.context.source;

import com.mojang.logging.LogUtils;
import com.whyvo.dine.config.Config;
import com.whyvo.dine.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.UUID;

public class EntitySource implements NbtSource {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Text TITLE = Text.translatable("dine.source.entity");

    private final UUID uuid;
    @Nullable
    private Entity cachedEntity;
    private NbtCompound cachedNbt;

    public EntitySource(UUID uuid) {
        this.uuid = uuid;
    }

    public EntitySource(@NotNull Entity entity) {
        this.uuid = entity.getUuid();
        this.cachedEntity = entity;
    }

    @Nullable
    private Entity getEntity() {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client == null) return null;
        return Util.getEntity(client, this.uuid);
    }

    private static NbtCompound finalGetNbt(@NotNull Entity entity) {
        NbtWriteView nbtWriteView;
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getErrorReporterContext(), LOGGER)) {
            nbtWriteView = NbtWriteView.create(logging, entity.getRegistryManager());
            entity.writeData(nbtWriteView);
        }
        return nbtWriteView.getNbt();
    }

    @Nullable
    @Override
    public NbtCompound getSource() {
        if(this.cachedEntity == null) return null;
        if(this.cachedNbt != null){
            return cachedNbt;
        }
        return finalGetNbt(this.cachedEntity);
    }

    @Override
    public void tick(boolean fromServer) {
        this.cachedEntity = getEntity();
        if(this.cachedEntity != null) {
            if(fromServer) {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if(player != null && (!Config.checkPermissionWhenFromServer || player.hasPermissionLevel(2))) {
                    player.networkHandler.getDataQueryHandler().queryEntityNbt(this.cachedEntity.getId(), nbt -> {
                        this.cachedNbt = nbt;
                    });
                }
            }
            else {
                this.cachedNbt = finalGetNbt(this.cachedEntity);
            }
        }
    }

    @Override
    public boolean defaultFromServer() {
        return Config.entityDefaultFromServer;
    }

    @Override
    public String getSourceName() {
        return "entity " + this.uuid;
    }

    @Override
    public String getTitle() {
        if(this.cachedEntity == null) return TITLE.getString();
        // noinspection ConstantConditions
        return TITLE.getString() +
                " (" +
                this.cachedEntity.getDisplayName().getString() +
                ")";
    }

    @Override
    public boolean isSame(@NotNull NbtSource o) {
        if(o instanceof EntitySource entitySource) {
            return this.uuid.equals(entitySource.uuid);
        }
        return false;
    }


}
