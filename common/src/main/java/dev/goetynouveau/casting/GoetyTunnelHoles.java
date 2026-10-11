package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.blocks.entities.HoleBlockEntity;
import com.Polarice3.Goety.common.magic.spells.void_spells.TunnelSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/** Mark only addon holes. Their native children keep the same loaded-world boundary. */
public final class GoetyTunnelHoles {
    public static final String MARKER="goety_nouveau:tunnel_hole";
    private GoetyTunnelHoles() {}
    public static boolean loaded(Level world, BlockPos pos) {
        return world.isInWorldBounds(pos) && world.hasChunkAt(pos);
    }
    public static boolean marked(HoleBlockEntity hole) {
        return hole.getPersistentData().getBoolean(MARKER);
    }
    public static void mark(Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof HoleBlockEntity hole) {
            hole.getPersistentData().putBoolean(MARKER,true);
            hole.setChanged();
        }
    }
    public static boolean child(Level world, BlockPos pos, Direction face, int count, boolean staff, int lifespan) {
        if (!loaded(world,pos)) return false;
        boolean created=TunnelSpell.createHole(world,pos,face,count,staff,lifespan);
        if (created) mark(world,pos);
        return created;
    }
}
