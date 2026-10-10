package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.events.spell.GoetyEventFactory;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.TotemFinder;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.resource.*;
import dev.goetynouveau.api.FocusSpells;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.platform.GoetyConfig;
import dev.arsconflux.api.projectile.CarrierRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Uses native storage, never a parallel soul-energy capability or a synthetic player. */
public final class GoetyResources {
    private GoetyResources() {}
    public static ISpell spell() {
        return FocusSpells.read(CarrierRegistry.id("goety:soul_bolt_focus")).orElseThrow();
    }
    public static ItemStack staff(LivingEntity caster) { return caster == null ? ItemStack.EMPTY : WandUtil.findWand(caster); }
    public static ResourceCharge quote(SpellContext context) {
        return quote(context, spell());
    }
    public static ResourceCharge quote(SpellContext context, ISpell spell) {
        return quote(context, spell, java.util.List.of());
    }
    public static ResourceCharge quote(SpellContext context, ISpell spell,
            java.util.List<com.hollingsworth.arsnouveau.api.spell.AbstractAugment> augments) {
        var caster = CastContext.of(context, null, null).caster();
        int cost = caster == null ? spell.defaultSoulCost() : spell.soulCost(caster, staff(caster));
        long burning = augments.stream().filter(a -> a == dev.goetynouveau.glyph.AugmentBurning.INSTANCE).count();
        cost = (int)Math.min(Integer.MAX_VALUE, (long)Math.max(0, cost) + burning * GoetyConfig.BURNING_COST.get());
        if (GoetyConfig.RESOURCE_SOURCE.get() == ResourceSource.ARS_MANA)
            return new ResourceCharge(new ArsManaAccount(context.getCaster()), Math.max(0, cost));
        var account = new SoulAccount(caster instanceof Player player ? player : null);
        if (caster instanceof Player player) cost = (int)(cost * SEHelper.soulDiscount(player));
        return new ResourceCharge(account, Math.max(0, cost));
    }
    /** Quotes must be side-effect-free. The native loss event runs once at the actual trigger,
     * before reserving its final price; custom event handlers can therefore change affordability. */
    public static ResourceCharge activation(ResourceCharge quote) {
        if (quote.account().free()) return quote;
        if (quote.account() instanceof SoulAccount souls && souls.player != null)
            return new ResourceCharge(souls, Math.max(0, GoetyEventFactory.onSoulEnergyLoss(souls.player, quote.amount())));
        return quote;
    }
    private static final class SoulAccount implements ResourceAccount {
        private final Player player;
        private final boolean active;
        private final ItemStack totem;
        private SoulAccount(Player player) {
            this.player = player;
            active = player != null && SEHelper.getSEActive(player);
            totem = player != null && !active ? TotemFinder.FindTotem(player) : ItemStack.EMPTY;
        }
        public Object reservationKey() {
            return ResourcePoolKey.of("goety:soul_energy", active ? player : totem);
        }
        public boolean free() { return player != null && player.isCreative(); }
        public boolean canSpend(int amount) {
            if (player == null || player.level().isClientSide || active != SEHelper.getSEActive(player)) return false;
            if (active) return SEHelper.getSESouls(player) >= amount;
            return !totem.isEmpty() && TotemFinder.FindTotem(player) == totem && ITotem.currentSouls(totem) >= amount;
        }
        public void spend(int amount) {
            // Already discounted and event-adjusted. Calling decreaseSouls would apply both twice.
            if (active) { SEHelper.decreaseSESouls(player, amount); SEHelper.sendSEUpdatePacket(player); }
            else ITotem.decreaseSouls(totem, amount);
        }
    }
}
