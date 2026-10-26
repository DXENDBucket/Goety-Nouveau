package dev.goetynouveau.glyph;

import com.Polarice3.Goety.api.items.magic.IFocus;
import com.Polarice3.Goety.api.magic.ISpell;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.items.Glyph;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.glyph.TriggerPaidGlyph;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.compat.NativeFocusIndex;
import dev.goetynouveau.casting.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
import java.util.*;

public final class NativeFocusGlyph extends AbstractEffect implements TriggerPaidGlyph {
    private final ResourceLocation focus;
    private final Item source;
    public NativeFocusGlyph(ResourceLocation focus,Item source) {
        super(NativeFocusIndex.id(focus),focus.toString());this.focus=focus;this.source=source;
        compatibleAugments.addAll(Set.of(AugmentAmplify.INSTANCE,AugmentDampen.INSTANCE));
        spellSchools.add(SpellSchools.CONJURATION);SpellSchools.CONJURATION.addSpellPart(this);
    }
    public ResourceLocation focusId(){return focus;}
    public ItemStack source(){return source.getDefaultInstance();}
    public ISpell spell(){return ((IFocus)source).getSpell();}
    @Override public int getDefaultManaCost(){return 0;}
    @Override public int getCastingCost(){return 0;}
    @Override public SpellTier defaultTier(){return SpellTier.ONE;}
    @Override protected Set<AbstractAugment> getCompatibleAugments(){return Set.of();}
    @Override public boolean isEnabled(){return super.isEnabled()&&(focus==null||dev.goetynouveau.platform.GoetyConfig.nativeSelfEnabled(focus));}
    @Override public String getLocalizationKey(){return source==null?super.getLocalizationKey():source.getDescriptionId();}
    @Override public Component getBookDescLang(){return Component.translatable("goety_nouveau.native_self.description");}
    @Override public Glyph getGlyph(){
        if(glyphItem==null)glyphItem=dev.goetynouveau.platform.NativeFocusPlatform.glyphItem(this);
        return glyphItem;
    }
    @Override public ResourceCharge firstCharge(SpellContext context,List<AbstractAugment> augments){return NativeFocusCasts.quote(this,context,augments);}
    @Override public void onResolve(HitResult hit,Level world,LivingEntity ignored,SpellStats stats,SpellContext context,SpellResolver resolver){
        var frame=CastContext.of(context,hit,null);
        if(isEnabled()&&hit instanceof EntityHitResult entity&&entity.getEntity()==frame.caster()) {
            dev.arsconflux.api.context.CastContexts.bind(context,frame);
            dev.arsconflux.api.context.CastContexts.scoped(frame,()->NativeFocusCasts.start(this,frame,stats));
        }
    }
}
