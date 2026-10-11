package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.magic.spells.geomancy.BurrowingSpell;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.api.EffectFocus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import java.util.*;

/** Native mining state is owned by the actual player executor, with one writer at a time. */
public final class GoetyBurrowingCasting {
    private static final Map<Player,Object> LEASES=new IdentityHashMap<>();
    private GoetyBurrowingCasting() {}
    private static boolean nativeMining(Player player){return nativeWand(player) && dev.goetynouveau.api.FocusSpells.read(IWand.getFocus(player.getUseItem())).filter(spell->spell instanceof BurrowingSpell).isPresent();}
    private static boolean nativeWand(Player player){return player.isUsingItem() && player.getUseItem().getItem() instanceof IWand;}
    public static boolean apply(ServerLevel world,CastContext frame,SpellStats ars,ResourceCharge charge) {
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host) || nativeWand(host) || LEASES.containsKey(host)
                || SEHelper.getMiningProgress(host)>0 || SEHelper.getMiningPos(host)!=null)return false;
        var spell=(BurrowingSpell)EffectFocus.BURROWING.spell();var staff=GoetyResources.staff(frame.caster()).copy();
        var stats=WandUtil.getStats(frame.caster(),spell);stats.setPotency(Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier()));
        stats.setBurning(stats.getBurning()+ars.getBuffCount(dev.goetynouveau.glyph.AugmentBurning.INSTANCE));
        int ticks=GoetyChannelCasting.boundedTicks(spell.shotsNumber(frame.caster(),staff),ars.getDurationMultiplier());if(ticks<=0)return false;
        var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell,null,stats);var lease=new Object();LEASES.put(host,lease);
        java.util.function.BooleanSupplier running=()->LEASES.get(host)==lease && !nativeWand(host);
        java.util.function.BooleanSupplier pulse=()->{
            if(!running.getAsBoolean())return false;
            return GoetyNativeGeometry.scoped(view,()->{
                int range=stats.getRange()+(WandUtil.enchantedFocus(frame.caster())?WandUtil.getRangeLevel(frame.caster()):0);
                var pos=spell.blockResult(world,host,range).getBlockPos();
                if(!world.isInWorldBounds(pos) || !world.hasChunkAt(pos))return false;
                var before=world.getBlockState(pos);int progress=SEHelper.getMiningProgress(host);var prior=SEHelper.getMiningPos(host);
                dev.goetynouveau.platform.NativeBreathHooks.result(spell,world,frame.caster(),staff,stats);
                return world.getBlockState(pos)!=before || progress!=SEHelper.getMiningProgress(host) || !Objects.equals(prior,SEHelper.getMiningPos(host));
            });
        };
        Runnable close=()->{if(LEASES.remove(host,lease) && !nativeMining(host))BurrowingSpell.resetMiningProgress(world,host);};
        try {
            boolean result=GoetyChannelCasting.start(EffectFocus.BURROWING,world,frame,host,spell,stats,charge,
                    new GoetyChannelCasting.Program(ticks,1,pulse,()->{},spell.CastingSound(frame.caster()),spell.loopSound(frame.caster()),running,close));
            if(!result)close.run();return result;
        }catch(RuntimeException error){close.run();throw error;}
    }
}
