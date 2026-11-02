package dev.goetynouveau.casting;

import net.minecraft.world.entity.Entity;

/** The native bomb explosion credits its owner directly, so retain its source just for this synchronous call. */
public final class GoetyBossDamageScope {
    public static final String CHILDREN="goety_nouveau:bomb_children_created";
    public static final String BONUS="goety_nouveau:boss_damage_bonus";
    private static final ThreadLocal<Entity> CURRENT=new ThreadLocal<>();
    private GoetyBossDamageScope() {}
    public static Entity current(){return CURRENT.get();}
    public static void during(Entity entity,Runnable action) {
        var old=CURRENT.get();CURRENT.set(entity);
        try{action.run();}finally{if(old==null)CURRENT.remove();else CURRENT.set(old);}
    }
}
