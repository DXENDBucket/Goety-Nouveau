package dev.goetynouveau.casting;

import com.Polarice3.Goety.utils.SEHelper;
import dev.goetynouveau.platform.ActiveCastingPlatform;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

/** Use native focus records. A result-specific cooldown must survive the base release callback. */
public final class GoetyFocusCooldowns {
    private GoetyFocusCooldowns() {}
    public static boolean enabled(LivingEntity caster) {
        return GoetyConfig.cooldownsEnabled() && dev.arsconflux.api.casting.ActiveCasts.cooldownsEnabled(caster) && ActiveCastingPlatform.realPlayer(caster)
                && (!((ServerPlayer)caster).isCreative() || GoetyConfig.creativeCooldown());
    }
    public static int remaining(ServerPlayer player, Item focus) {
        // Public native serialization exposes remaining ticks without a reflection/mixin dependency.
        var records = new ListTag();
        SEHelper.getFocusCoolDown(player).save(records);
        for (int i = 0; i < records.size(); ++i) {
            var record = records.getCompound(i);
            if (record.getInt("Item") == Item.getId(focus)) return Math.max(0, record.getInt("Time"));
        }
        return 0;
    }
    public static void atLeast(LivingEntity caster, Item focus, int ticks) {
        if (ticks > 0 && enabled(caster) && remaining((ServerPlayer)caster, focus) < ticks)
            SEHelper.addCooldown((ServerPlayer)caster, focus, ticks);
    }
}
