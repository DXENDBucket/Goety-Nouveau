package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.IceSpear;
import com.Polarice3.Goety.common.entities.projectiles.PoisonQuill;
import dev.goetynouveau.casting.NativePiercingHistory;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;

@Mixin(value = {PoisonQuill.class, IceSpear.class}, remap = false)
public abstract class NativePiercingHistoryMixin implements NativePiercingHistory {
    @Accessor("piercingIgnoreEntityIds") protected abstract IntOpenHashSet goetyNouveau$piercingIds();
    @Accessor("piercedAndKilledEntities") protected abstract List<Entity> goetyNouveau$piercedAndKilled();

    // The native private reset shares a vanilla name and is SRG-renamed in Forge
    // releases. These Goety-owned fields keep their names on both loaders.
    @Override public void goetyNouveau$resetPiercing() {
        var killed = goetyNouveau$piercedAndKilled();
        if (killed != null) killed.clear();
        var ignored = goetyNouveau$piercingIds();
        if (ignored != null) ignored.clear();
    }
}
