package com.whyvo.dine.context.source;

import com.whyvo.dine.config.Config;
import com.whyvo.dine.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class EntitySource implements NbtSource {
    private static final Component TITLE = Component.translatable("dine.source.entity");

    private final UUID uuid;
    @Nullable
    private Entity cachedEntity;
    private CompoundTag cachedNbt;

    public EntitySource(UUID uuid) {
        this.uuid = uuid;
    }

    public EntitySource(@NotNull Entity entity) {
        this.uuid = entity.getUUID();
        this.cachedEntity = entity;
    }

    @Nullable
    private Entity getEntity() {
        Minecraft client = Minecraft.getInstance();
        return Util.getEntity(client, this.uuid);
    }

    @Nullable
    @Override
    public CompoundTag getSource() {
        if(cachedEntity == null) return null;
        if(this.cachedNbt != null){
            return cachedNbt;
        }
        return this.cachedEntity.saveWithoutId(new CompoundTag());
    }

    @Override
    public void tick(boolean fromServer) {
        this.cachedEntity = getEntity();
        if(this.cachedEntity != null) {
            if(fromServer) {
                LocalPlayer player = Minecraft.getInstance().player;
                if(player != null && (!Config.checkPermissionWhenFromServer || player.hasPermissions(2))) {
                    player.connection.getDebugQueryHandler().queryEntityTag(this.cachedEntity.getId(), nbt -> {
                        this.cachedNbt = nbt;
                    });
                }
            }
            else {
                this.cachedNbt = this.cachedEntity.saveWithoutId(new CompoundTag());
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
