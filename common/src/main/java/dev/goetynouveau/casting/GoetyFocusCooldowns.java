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
    private static final ThreadLocal<Boolean> FINAL_WRITE = new ThreadLocal<>();
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
        int adjusted = dev.arsconflux.api.casting.CastingTiming.cooldownTicks(caster, ticks);
        if (adjusted > 0 && enabled(caster) && remaining((ServerPlayer)caster, focus) < adjusted) {
            var previous = FINAL_WRITE.get();
            FINAL_WRITE.set(true);
            try { SEHelper.addCooldown((ServerPlayer)caster, focus, adjusted); }
            finally { if (previous == null) FINAL_WRITE.remove(); else FINAL_WRITE.set(previous); }
        }
    }
    /** Custom native focus callbacks may write directly; only opt-in native-focus scopes participate. */
    public static int nativeWrite(net.minecraft.world.entity.player.Player player, int ticks) {
        return !Boolean.TRUE.equals(FINAL_WRITE.get()) && NativeFocusScope.current(player) != null
                ? dev.arsconflux.api.casting.CastingTiming.cooldownTicks(player, ticks) : ticks;
    }
}
