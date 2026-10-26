package dev.goetynouveau.casting;
import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.WandUtil;
import dev.goetynouveau.glyph.NativeFocusGlyph;
import dev.arsconflux.api.context.CastContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.util.function.Supplier;

/** Per-cast copies preserve actual staff identity/components without editing the equipped item. */
public final class NativeFocusScope {
    public static final class State {
        public final NativeFocusGlyph glyph;
        public final LivingEntity caster;
        public final ItemStack wand,focus;
        public ISpell spell;
        public SpellStat stats;
        public int shots;
        public State(NativeFocusGlyph glyph,LivingEntity caster){
            this.glyph=glyph;this.caster=caster;this.spell=glyph.spell();
            var equipped=WandUtil.findWand(caster);
            wand=equipped.isEmpty()?new ItemStack(BuiltInRegistries.ITEM.get(dev.arsconflux.api.projectile.CarrierRegistry.id("goety:dark_wand"))):equipped.copy();
            var existing=equipped.isEmpty()?ItemStack.EMPTY:IWand.getFocus(equipped);
            focus=!existing.isEmpty()&&existing.is(glyph.source().getItem())?existing.copy():glyph.source();
            if(!wand.isEmpty()){
                var contents=SoulUsingItemHandler.get(wand);contents.extractItem();contents.insertItem(focus.copy());
            }
        }
    }
    private static final ThreadLocal<State> CURRENT=new ThreadLocal<>();
    private NativeFocusScope(){}
    public static SpellStat stats(State state){var s=state.stats;return new SpellStat(s.getPotency(),s.getDuration(),s.getRange(),s.getRadius(),s.getBurning(),s.getVelocity());}
    public static State current(LivingEntity caster){var state=CURRENT.get();return state!=null&&state.caster==caster?state:null;}
    public static <T>T scoped(State state,Supplier<T> action){
        var before=CURRENT.get();CURRENT.set(state);
        try{return action.get();}finally{if(before==null)CURRENT.remove();else CURRENT.set(before);}
    }
    public static <T>T geometry(State state,CastContext frame,Supplier<T> action){
        return scoped(state,()->state.spell instanceof com.Polarice3.Goety.common.magic.Spell spell
                ?GoetyNativeGeometry.scoped(new GoetyNativeGeometry.View(frame,state.caster,spell),action):action.get());
    }
}
