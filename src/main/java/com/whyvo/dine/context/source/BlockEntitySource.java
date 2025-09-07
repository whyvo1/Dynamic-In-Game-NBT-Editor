package com.whyvo.dine.context.source;

import com.whyvo.dine.config.Config;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockEntitySource implements NbtSource {
    private static final Text TITLE = Text.translatable("dine.source.block_entity");

    private final BlockPos pos;
    private BlockEntity cachedBlockEntity;
    private NbtCompound cachedNbt;

    public BlockEntitySource(BlockPos pos) {
        this.pos = pos;
    }

    @Nullable
    private BlockEntity getBlockEntity() {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client == null) return null;
        ClientWorld clientWorld = client.world;
        if(clientWorld == null) return null;
        return clientWorld.getBlockEntity(this.pos);
    }

    @Override
    public @Nullable NbtCompound getSource() {
        if(cachedBlockEntity == null) return null;

        World world = MinecraftClient.getInstance().world;
        if(this.cachedNbt != null || world == null){
            return cachedNbt;
        }
        return this.cachedBlockEntity.createNbt(world.getRegistryManager());
    }

    @Override
    public void tick(boolean fromServer) {
        this.cachedBlockEntity = getBlockEntity();
        if(this.cachedBlockEntity != null) {
            if(fromServer) {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if(player != null && (!Config.checkPermissionWhenFromServer || player.hasPermissionLevel(2))) {
                    player.networkHandler.getDataQueryHandler().queryBlockNbt(this.pos, nbt -> {
                        this.cachedNbt = nbt;
                    });
                }
            }
            else {
                World world = MinecraftClient.getInstance().world;
                if(world == null){
                    this.cachedNbt = null;
                    return;
                }
                this.cachedNbt = this.cachedBlockEntity.createNbt(world.getRegistryManager());
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
                this.cachedBlockEntity.getCachedState().getBlock().getName().getString() +
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
