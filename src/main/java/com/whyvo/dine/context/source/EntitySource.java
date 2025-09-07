package com.whyvo.dine.context.source;

import com.whyvo.dine.config.Config;
import com.whyvo.dine.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EntitySource implements NbtSource {
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

    @Nullable
    @Override
    public NbtCompound getSource() {
        if(cachedEntity == null) return null;
        if(this.cachedNbt != null){
            return cachedNbt;
        }
        return this.cachedEntity.writeNbt(new NbtCompound());
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
                this.cachedNbt = this.cachedEntity.writeNbt(new NbtCompound());
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
