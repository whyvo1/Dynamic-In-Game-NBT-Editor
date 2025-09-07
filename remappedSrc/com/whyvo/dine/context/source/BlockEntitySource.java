package com.whyvo.dine.context.source;

import com.whyvo.dine.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockEntitySource implements NbtSource {
    private static final Component TITLE = Component.translatable("dine.source.block_entity");

    private final BlockPos pos;
    private BlockEntity cachedBlockEntity;
    private CompoundTag cachedNbt;

    public BlockEntitySource(BlockPos pos) {
        this.pos = pos;
    }

    @Nullable
    private BlockEntity getBlockEntity() {
        Minecraft client = Minecraft.getInstance();
        if(client == null) return null;
        ClientLevel clientWorld = client.level;
        if(clientWorld == null) return null;
        return clientWorld.getBlockEntity(this.pos);
    }

    @Override
    public @Nullable CompoundTag getSource() {
        if(cachedBlockEntity == null) return null;
        if(this.cachedNbt != null){
            return cachedNbt;
        }
        return this.cachedBlockEntity.saveWithoutMetadata();
    }

    @Override
    public void tick(boolean fromServer) {
        this.cachedBlockEntity = getBlockEntity();
        if(this.cachedBlockEntity != null) {
            if(fromServer) {
                LocalPlayer player = Minecraft.getInstance().player;
                if(player != null && (!Config.checkPermissionWhenFromServer || player.hasPermissions(2))) {
                    player.connection.getDebugQueryHandler().queryBlockEntityTag(this.pos, nbt -> {
                        this.cachedNbt = nbt;
                    });
                }
            }
            else {
                this.cachedNbt = this.cachedBlockEntity.saveWithoutMetadata();
            }
        }
    }

    @Override
    public boolean defaultFromServer() {
        return Config.blockEntityDefaultFromServer;
    }

    private String getPosString() {
        return this.pos.getX() + " " + this.pos.getY() + " " + this.pos.getZ();
    }

    @Override
    public String getSourceName() {
        return "block " + getPosString();
    }

    @Override
    public String getTitle() {
        if(this.cachedBlockEntity == null) return TITLE.getString();
        return TITLE.getString() +
                " (" +
                this.cachedBlockEntity.getBlockState().getBlock().getName().getString() +
                ")";

    }

    @Override
    public boolean isSame(@NotNull NbtSource o) {
        if(o instanceof BlockEntitySource blockEntitySource) {
            return this.pos.equals(blockEntitySource.pos);
        }
        return false;
    }
}
