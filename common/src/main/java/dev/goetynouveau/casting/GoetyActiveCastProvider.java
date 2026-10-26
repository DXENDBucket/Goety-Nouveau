package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.api.magic.IChargingSpell;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import dev.arsconflux.api.casting.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetynouveau.glyph.GoetyBoltGlyph;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.api.FocusSpells;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Native Goety preparations and focus cooldowns, without initiating the original staff use loop. */
public final class GoetyActiveCastProvider implements ActiveCastProvider {
    public static void register() { ActiveCasts.register(CarrierRegistry.id("goety_nouveau:active_casting"), new GoetyActiveCastProvider()); }
    private static ResourceLocation focus(com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart part) {
        if(part instanceof dev.goetynouveau.glyph.NativeFocusGlyph nativeFocus)return nativeFocus.focusId();
        if(part instanceof GoetyEffectGlyph effect)return effect.focus().focusId();
        return part instanceof GoetyBoltGlyph glyph ? glyph.focus().focusId() : null;
    }
    private static List<ResourceLocation> focuses(Spell recipe) {
        var result = new ArrayList<ResourceLocation>();
        for (var part : ArsSpellAccess.parts(recipe)) { var id = focus(part); if (id != null) result.add(id); }
        return result;
    }
    @Override public boolean supports(Spell spell) { return !focuses(spell).isEmpty(); }
    @Override public boolean chantingEnabled(ActiveCastRequest request) { return GoetyConfig.chantingEnabled(); }
    /** Only native unlimited-fire projectile payloads skip preparation/cooldown.
     * EverCharge is also used by beams, rain and utility channels; those keep their own timing. */
    public static boolean rapidFire(ISpell spell) {
        if (!(spell instanceof IChargingSpell charging) || !charging.everCharge()) return false;
        return Arrays.stream(dev.goetynouveau.api.BoltFocus.values())
                .anyMatch(focus -> focus.spell().getClass() == spell.getClass());
    }
    public static int preparationTicks(ISpell spell, net.minecraft.world.entity.LivingEntity caster, ItemStack staff) {
        if (rapidFire(spell)) return 0;
        // Its real target-dependent charge is displayed and completed after the Ars hit.
        if(spell instanceof com.Polarice3.Goety.common.magic.spells.abyss.PrismaBeamSpell)return 0;
        if(spell instanceof com.Polarice3.Goety.common.magic.spells.void_spells.VoidShockSpell)return 10;
        if(spell instanceof com.Polarice3.Goety.common.magic.spells.geomancy.QuakingSpell)return Math.max(0,spell.defaultCastDuration());
        if(spell instanceof com.Polarice3.Goety.common.magic.spells.utility.IlluminateSpell)return Math.max(0,com.Polarice3.Goety.config.SpellConfig.IlluminateChargeUp.get());
        // Staff Scatter emits during its cast window; that window is the channel, not an extra warmup.
        if(spell instanceof com.Polarice3.Goety.common.magic.spells.geomancy.ScatterSpell scatter && scatter.rightStaff(staff))return 0;
        // 72000 is an item-use lifetime. Only castUp is the preparation of a charging spell.
        return Math.max(0, spell instanceof IChargingSpell charging ? charging.castUp(caster, staff) : spell.castDuration(caster, staff));
    }
    @Override public List<Integer> chantTicks(ActiveCastRequest request) {
        var result = new ArrayList<Integer>();
        var staff = GoetyResources.staff(request.caster());
        for (var part : ArsSpellAccess.parts(request.spell())) {
            var id=focus(part);
            if(id!=null && !(part instanceof dev.goetynouveau.glyph.NativeFocusGlyph)) FocusSpells.read(id).ifPresent(spell -> result.add(preparationTicks(spell, request.caster(), staff)));
        }
        return List.copyOf(result);
    }
    private boolean cooldowns(ActiveCastRequest request) {
        return GoetyConfig.cooldownsEnabled() && dev.goetynouveau.platform.ActiveCastingPlatform.realPlayer(request.caster())
                && (!((ServerPlayer)request.caster()).isCreative() || GoetyConfig.creativeCooldown());
    }
    @Override public boolean allowed(ActiveCastRequest request) { return rejection(request)==null; }
    @Override public dev.arsconflux.api.casting.CastRejection rejection(ActiveCastRequest request) {
        if(!cooldowns(request)) return null;
        var player=(ServerPlayer)request.caster();
        dev.arsconflux.api.casting.CastRejection selected=null;
        for(var id:new LinkedHashSet<>(focuses(request.spell()))) {
            if(FocusSpells.read(id).map(GoetyActiveCastProvider::rapidFire).orElse(false)) continue;
            var focus=new ItemStack(BuiltInRegistries.ITEM.get(id));
            if(!focus.isEmpty() && SEHelper.isOnCooldown(player,focus)) {
                long ticks=GoetyFocusCooldowns.remaining(player,focus.getItem());
                if(selected==null || ticks>selected.remainingTicks()) selected=dev.arsconflux.api.casting.CastRejection.cooldown(focus.getHoverName(),ticks);
            }
        }
        return selected;
    }
    @Override public void released(ActiveCastRequest request, List<Spell> recipes) {
        if (!cooldowns(request)) return;
        var player = (ServerPlayer)request.caster();
        var distinct = new LinkedHashSet<ResourceLocation>();
        for (var recipe : recipes) distinct.addAll(focuses(recipe));
        for (var id : distinct) FocusSpells.read(id).ifPresent(spell -> {
            if(dev.goetynouveau.compat.NativeFocusIndex.all().stream().anyMatch(g->g.focusId().equals(id)))return;
            if (rapidFire(spell)) return;
            // Native grapples cool down on retrieval/end, not on initial launch.
            if(spell instanceof com.Polarice3.Goety.common.magic.spells.wild.GrappleSpell)return;
            int ticks = Math.max(0, spell.spellCooldown(player));
            GoetyFocusCooldowns.atLeast(player, BuiltInRegistries.ITEM.get(id), ticks);
        });
    }
    @Override public void chantProgress(ActiveCastRequest request, UUID token, int duration, int remaining) {
        if (request.caster() instanceof ServerPlayer player && !player.hasDisconnected())
            dev.goetynouveau.platform.ActiveCastingPlatform.chant(player, new dev.goetynouveau.network.ChantStatePayload(token, duration, remaining));
    }
}
