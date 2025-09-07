package com.whyvo.dine.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ClientPosArgument {
    private final WorldCoordinate x;
    private final WorldCoordinate y;
    private final WorldCoordinate z;

    public ClientPosArgument(WorldCoordinate x, WorldCoordinate y, WorldCoordinate z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private Vec3 toAbsolutePos(FabricClientCommandSource source) {
        Vec3 vec3d = source.getPosition();
        return new Vec3(this.x.get(vec3d.x), this.y.get(vec3d.y), this.z.get(vec3d.z));
    }

    public BlockPos toAbsoluteBlockPos(FabricClientCommandSource source) {
        return BlockPos.containing(this.toAbsolutePos(source));
    }

    public static ClientPosArgument parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        WorldCoordinate coordinateArgument = WorldCoordinate.parseInt(reader);
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            WorldCoordinate coordinateArgument2 = WorldCoordinate.parseInt(reader);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                WorldCoordinate coordinateArgument3 = WorldCoordinate.parseInt(reader);
                return new ClientPosArgument(coordinateArgument, coordinateArgument2, coordinateArgument3);
            } else {
                reader.setCursor(i);
                throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
            }
        } else {
            reader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
        }
    }
}
