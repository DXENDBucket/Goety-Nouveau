package dev.goetynouveau.casting;

import dev.arsconflux.api.context.CastContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;

/** Persistent source identity supplements the native damage owner; it does not replace native AI. */
public final class GoetyAreaProvenance {
    private static final String SOURCE="goety_nouveau:area_source", OWNER="goety_nouveau:area_damage_owner";
    private GoetyAreaProvenance() {}
    public static void mark(Entity entity,CastContext frame){
        entity.getPersistentData().putUUID(SOURCE,frame.caster().getUUID());
        if(frame.damageOwner()!=null)entity.getPersistentData().putUUID(OWNER,frame.damageOwner().getUUID());
    }
    public static boolean marked(Entity entity){return entity.getPersistentData().hasUUID(SOURCE);}
    public static LivingEntity source(Entity entity,LivingEntity fallback){
        return marked(entity) && entity.level() instanceof ServerLevel world
                && world.getEntity(entity.getPersistentData().getUUID(SOURCE)) instanceof LivingEntity caster?caster:fallback;
    }
    public static boolean friendly(Entity area,Entity candidate){
        var caster=source(area,null);return caster!=null && (candidate==caster || dev.arsconflux.api.targeting.Allies.areAllies(caster,candidate));
    }
    public static LivingEntity damageOwner(Entity area,LivingEntity fallback){
        return area.getPersistentData().hasUUID(OWNER) && area.level() instanceof ServerLevel world
                && world.getEntity(area.getPersistentData().getUUID(OWNER)) instanceof LivingEntity owner?owner:fallback;
    }
    public static void copy(Entity from,Entity to){
        if(marked(from))to.getPersistentData().putUUID(SOURCE,from.getPersistentData().getUUID(SOURCE));
        if(from.getPersistentData().hasUUID(OWNER))to.getPersistentData().putUUID(OWNER,from.getPersistentData().getUUID(OWNER));
    }
}
