package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import dev.arsconflux.api.casting.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.*;
import dev.goetynouveau.api.*;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

/** Small real-world fixtures for the new native ability families, not the historical suite. */
public final class AbilityCases {
    private static Vec3 scene(GameTestHelper h) {
        var origin=h.absoluteVec(new Vec3(4,0,4));return new Vec3(origin.x,h.getLevel().getMaxBuildHeight()-48,origin.z);
    }
    private static void prepare(GameTestHelper h,ServerPlayer p) {
        var pos=BlockPos.containing(scene(h));
        for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)for(int y=-1;y<=7;y++)
            h.getLevel().setBlock(pos.offset(x,y,z),y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        p.setPos(scene(h).add(-7,0,0));p.setGameMode(GameType.SURVIVAL);
        if(h.getLevel().getEntity(p.getUUID())!=p)h.getLevel().addNewPlayer(p);
        SEHelper.setSEActive(p,true);SEHelper.setSESouls(p,100000);
    }
    private static LivingEntity cow(GameTestHelper h,Vec3 pos) {
        var cow=EntityType.COW.create(h.getLevel());cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(500);cow.setHealth(500);cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(pos);h.getLevel().addFreshEntity(cow);return cow;
    }
    private static SpellResolver resolver(GameTestHelper h,ServerPlayer p,Spell spell) {
        return new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));
    }
    private static BlockHitResult block(Vec3 pos) {return new BlockHitResult(pos,Direction.UP,BlockPos.containing(pos),false);}
    private static void effect(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult hit) {
        var resolver=resolver(h,p,new Spell(MethodTouch.INSTANCE,glyph));resolver.spellContext.nextPart();
        int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(resolver.spellContext,glyph.focus().spell()).amount();
        resolver.onResolveEffect(h.getLevel(),hit);
        h.assertTrue(SEHelper.getSESouls(p)==before-(p.isCreative()?0:fee),"Native successful ability pays once: "+glyph.focus()+" before="+before+" after="+SEHelper.getSESouls(p)+" fee="+fee+" alive="+p.isAlive());
    }
    private static <T extends Entity> List<T> entities(GameTestHelper h,Class<T> type) {
        return h.getLevel().getEntitiesOfClass(type,new AABB(scene(h),scene(h)).inflate(22));
    }
    private static void clear(GameTestHelper h,ServerPlayer p) {
        for(var entity:h.getLevel().getEntities(p,new AABB(scene(h),scene(h)).inflate(22)))entity.discard();ExecutionSessions.tick();
    }
    public static void nativeBrewsAndEffects(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h,p);var center=scene(h);var pos=BlockPos.containing(center);
            effect(h,p,GlyphCatalog.LEAF_SHELL,block(center.add(0,3,0)));
            int leaves=0;for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)for(int y=0;y<=7;y++)
                if(h.getLevel().getBlockState(pos.offset(x,y,z)).is(ModBlocks.HARDENED_LEAVES.get()))leaves++;
            h.assertTrue(leaves>20 && h.getLevel().getBlockState(pos.above(3)).isAir(),"Native leaf shell is a hollow block sphere, not an absorption buff");
            prepare(h,p);var target=cow(h,center);
            effect(h,p,GlyphCatalog.THORN_TRAP,new EntityHitResult(target));
            long thorns=Direction.Plane.HORIZONTAL.stream().filter(d->h.getLevel().getBlockState(pos.relative(d)).is(ModBlocks.MAGIC_THORN.get())).count();
            h.assertTrue(thorns==4,"Native thorn trap surrounds the selected entity with four columns");
            prepare(h,p);target.setPos(center.add(2,0,0));var dropped=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),center.x+2,center.y,center.z+1,new ItemStack(Items.STONE));h.getLevel().addFreshEntity(dropped);
            effect(h,p,GlyphCatalog.TRANSPOSE,block(center));
            h.assertTrue(target.position().distanceTo(new Vec3(pos.getX(),pos.getY()+1,pos.getZ()))<.01 && dropped.position().distanceTo(target.position())<.01,"Native transpose gathers both living and item entities");
            target.setPos(center.add(2,0,0));target.setDeltaMovement(Vec3.ZERO);
            effect(h,p,GlyphCatalog.BONE_PULL,block(center));
            h.assertTrue(target.getDeltaMovement().x<0,"Native hammer pull accelerates toward the selected point");
            target.setPos(center);effect(h,p,GlyphCatalog.BERSERK_CLOUD,new EntityHitResult(target));
            h.assertTrue(target.hasEffect(MobEffects.DAMAGE_BOOST) && target.hasEffect(MobEffects.MOVEMENT_SPEED) && target.hasEffect(MobEffects.CONFUSION),"Complete native fungus splash keeps all three effects");
            h.assertTrue(entities(h,BerserkFungus.class).isEmpty(),"Instant native fungus splash leaves no projectile behind");
            prepare(h,p);target.setPos(center.add(0,0,1));var armor=new ItemStack(Items.IRON_CHESTPLATE);target.setItemSlot(EquipmentSlot.CHEST,armor);h.getLevel().setBlock(pos,Blocks.STONE.defaultBlockState(),2);
            effect(h,p,GlyphCatalog.CORROSION,block(center));
            var worn=target.getItemBySlot(EquipmentSlot.CHEST);
            h.assertTrue(h.getLevel().getBlockState(pos).isAir() && (worn.isEmpty() || worn.getDamageValue()>=50),"Native corrosion retains block destruction and equipment wear, including breakage");
            prepare(h,p);target.setPos(center);effect(h,p,GlyphCatalog.SNARELING_TRAP,new EntityHitResult(target));
            var trap=entities(h,SnarelingGoop.class).get(0);
            for(int i=0;i<8;i++)trap.tick();
            h.assertTrue(trap.getOwner()==p && target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"Native trap binds and slows the selected victim");
            for(int i=0;i<110;i++)if(!trap.isRemoved())trap.tick();
            h.assertTrue(trap.isRemoved(),"Native trap expires with its burrow lifecycle");
            for(var ability:AbilitySpells.Ability.values()) {
                var spell=AbilitySpells.read(ability.glyph).orElseThrow();
                var part=GlyphCatalog.ALL.stream().filter(g->g.getRegistryName().getPath().equals("glyph_"+ability.glyph)).findFirst().orElseThrow();
                h.assertTrue((part instanceof GoetyEffectGlyph e?e.focus().spell():((dev.goetynouveau.glyph.GoetyBoltGlyph)part).focus().spell())==spell,"Actual glyph resolves its own ability metadata: "+ability);
                h.assertTrue(spell.defaultSoulCost()>0,"Every ability has a native resource reference");
                if(ability.brew!=null)h.assertTrue(spell.defaultSoulCost()==ability.brewEffect().getSoulCost(),"Brew charge reads native configurable soul cost");
                h.assertTrue(GoetyActiveCastProvider.preparationTicks(spell,p,ItemStack.EMPTY)>0,"Ability participates in shared active chanting");
            }
            var provider=new GoetyActiveCastProvider();var request=new ActiveCastRequest(new Spell(MethodTouch.INSTANCE,GlyphCatalog.LEAF_SHELL),p,ActiveCastSource.CASTER_TOOL);
            provider.released(request,List.of(request.spell()));
            var glyph=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(GlyphCatalog.LEAF_SHELL.getRegistryName()));
            h.assertTrue(SEHelper.isOnCooldown(p,glyph) && !provider.allowed(request),"Non-focus ability records native cooldown on its real glyph item");
            p.setGameMode(GameType.CREATIVE);h.assertTrue(provider.allowed(request),"Creative bypasses ability cooldowns by default");
            prepare(h,p);SEHelper.setSESouls(p,0);var cast=resolver(h,p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.LEAF_SHELL));cast.spellContext.nextPart();cast.onResolveEffect(h.getLevel(),block(center.add(0,3,0)));
            h.assertTrue(h.getLevel().getBlockState(pos.above(6)).isAir(),"Unfunded brew cannot create shell blocks");
            h.succeed();
        }finally{clear(h,p);p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);}
    }
    public static void nativeProjectilesAndRepeatedPayment(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h,p);
            for(var focus:List.of(BoltFocus.SNARELING_SHOT,BoltFocus.SNAP_FUNGUS,BoltFocus.BLAST_FUNGUS,BoltFocus.BERSERK_FUNGUS))for(boolean orbit:List.of(false,true)) {
                clear(h,p);SEHelper.setSESouls(p,100000);var marker=new Marker();
                var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.projectile(focus),marker));cast.spellContext.nextPart();
                EntityProjectileSpell carrier=orbit?new EntityOrbitProjectile(h.getLevel(),cast,p):new EntityProjectileSpell(h.getLevel(),cast);carrier.setPos(scene(h).add(0,2,0));carrier.setDeltaMovement(new Vec3(0,0,.75));
                int fee=GoetyResources.quote(cast.spellContext,focus.spell()).amount();
                h.assertTrue(ProjectileDispatch.convert(carrier),"Core converts new native payload "+focus);
                var shot=entities(h,Projectile.class).get(0);var start=shot.position();
                h.assertTrue(shot.getOwner()==p && shot instanceof ManagedBolt,"Native ownership and carrier state retained");
                h.assertTrue(Math.abs(shot.getDeltaMovement().length()-(orbit?.75:focus==BoltFocus.SNARELING_SHOT?.95:.75))<.001,"Original native projectile launch speed; orbit inherits the Ars trajectory");
                shot.tick();h.assertTrue(orbit?shot.position().distanceTo(start)<.0001:shot.position().distanceTo(start)>.1,"Native flight moves once; special carrier never moves twice");
                var target=cow(h,scene(h).add(0,0,2));shot.setPos(target.position());
                if(orbit) dev.arsconflux.api.glyph.ArsSpellAccess.resolver(carrier).onResolveEffect(h.getLevel(),new EntityHitResult(target));
                else GoetyNativeProjectileCasting.nativeImpact(shot,new EntityHitResult(target));
                h.assertTrue(marker.hits==1 && SEHelper.getSESouls(p)==100000-fee,"Full native impact continues Ars and pays exactly once: "+focus+" orbit="+orbit+" hits="+marker.hits+" souls="+SEHelper.getSESouls(p)+" fee="+fee+" removed="+shot.isRemoved());
                if(focus==BoltFocus.SNARELING_SHOT)h.assertTrue(!entities(h,SnarelingGoop.class).isEmpty(),"Native snare shot retains its trap aftermath");
                if(focus==BoltFocus.BERSERK_FUNGUS)h.assertTrue(target.hasEffect(MobEffects.DAMAGE_BOOST),"Native thrown fungus splash survives replacement");
                if(orbit){SEHelper.setSESouls(p,0);dev.arsconflux.api.glyph.ArsSpellAccess.resolver(carrier).onResolveEffect(h.getLevel(),new EntityHitResult(target));h.assertTrue(marker.hits==1 && shot.isRemoved(),"Unfunded repeated native impact stops without another continuation");}
            }
            h.succeed();
        }finally{clear(h,p);p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);}
    }
    private static final class Marker extends AbstractEffect {
        int hits;Marker(){super(CarrierRegistry.id("goety_nouveau:glyph_ability_test_marker"),"Marker");}
        public int getDefaultManaCost(){return 0;}public int getCastingCost(){return 0;}
        public SpellTier defaultTier(){return SpellTier.ONE;}public SpellTier getConfigTier(){return SpellTier.ONE;}
        public boolean isEnabled(){return true;}public Set<AbstractAugment> getCompatibleAugments(){return Set.of();}
        public void onResolve(HitResult hit,Level world,LivingEntity caster,SpellStats stats,SpellContext context,SpellResolver resolver){hits++;}
    }
}
