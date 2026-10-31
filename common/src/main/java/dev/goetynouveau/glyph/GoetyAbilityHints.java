package dev.goetynouveau.glyph;
import dev.arsconflux.api.glyph.*;
import dev.goetynouveau.api.EffectFocus;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
final class GoetyAbilityHints {
    static void effect(ResourceLocation id,EffectFocus focus) {
        Set<ResourceLocation> tags=switch(focus) {
            case SOUL_HEAL -> Set.of(AbilityTags.HEALING,AbilityTags.SELF,AbilityTags.ALLY);
            case IRON_HIDE,CHILL_HIDE,BULWARK,LEAF_SHELL -> Set.of(AbilityTags.DEFENSE,AbilityTags.SELF,AbilityTags.ALLY);
            case FIRE_BREATH,FROST_BREATH,SWARM,CORRUPTED_BEAM,WATER_JET,PRISMA_BEAM -> Set.of(AbilityTags.ATTACK,AbilityTags.CHANNELING,AbilityTags.SELF,AbilityTags.ALLY,AbilityTags.POSITION);
            case HAUNTED_SKULL,WATCHING,SNARING,BLASTING,BLAZING,GHASTLY,GUARDIAN,ZOMBIE,SKELETON,VEX,LEAPING,HUNTING,MAULING,REAPER,WRAITH,PHANTOM,SLIMY,CARRION,WHISPER,FROSTBORN,SPRITE,VANGUARD,BLACKGUARD,ILLUSION -> Set.of(AbilityTags.SUMMONING,AbilityTags.SELF,AbilityTags.POSITION,AbilityTags.ENEMY);
            case WEAKENING,ENTANGLING,LAUNCH,BONE_PULL -> Set.of(AbilityTags.CONTROL,AbilityTags.ENEMY,AbilityTags.POSITION);
            case ICE_CHUNK,FROST_NOVA,LIGHTNING,UPDRAFT,SHOCKWAVE,FANGS,IGNITE,FEAST,DISCHARGE,EARTH_FIST,GHOST_FIRE,LEECHING,ERUPTION,BIO_MINE,TEETH,THUNDERBOLT,HAIL,MONSOON,FIRE_BLAST,SONIC_BOOM,FLAME_STRIKE,VOID_RIFT,WIND_BLAST,SPIKES,THORN_TRAP,CORROSION -> Set.of(AbilityTags.ATTACK,AbilityTags.ENEMY,AbilityTags.POSITION);
            case CRAFTING,ENDER_CHEST -> Set.of(AbilityTags.UTILITY,AbilityTags.SELF,AbilityTags.PLAYER_ONLY);
            default -> Set.of();
        };
        if(!tags.isEmpty())GlyphAbilities.declare(id,tags);
        switch(focus) {
            case SOUL_HEAL -> GlyphConditions.declare(id,(caster,target)->target.isAlive()&&target.getHealth()<target.getMaxHealth()-.5f);
            case IRON_HIDE -> GlyphConditions.declare(id,(caster,target)->!target.hasEffect(dev.goetynouveau.platform.NativeEffectHooks.ironHide(1,0).getEffect()));
            case CHILL_HIDE -> GlyphConditions.declare(id,(caster,target)->!target.hasEffect(dev.goetynouveau.platform.NativeEffectHooks.chillHide(1,0).getEffect()));
            case BULWARK -> GlyphConditions.declare(id,(caster,target)->com.Polarice3.Goety.utils.MiscCapHelper.getShields(target)<=0);
            case BLAZING,GHASTLY,BLACKGUARD,LEAPING -> GlyphConditions.declare(id,(caster,target)->dev.goetynouveau.casting.GoetyServantCasting.canSummon(focus,caster));
            default -> {}
        }
    }
    private GoetyAbilityHints() {}
}
