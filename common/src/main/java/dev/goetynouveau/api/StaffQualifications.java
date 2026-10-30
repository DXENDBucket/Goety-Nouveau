package dev.goetynouveau.api;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import dev.goetynouveau.casting.QualifiedStaffAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Function;

/** Additional NPC staff qualifications. Empty by default; queries must be side-effect-free. */
public final class StaffQualifications {
    public record Qualification(Set<SpellType> schools,Set<ResourceLocation> items,boolean allStaves) {
        public static final Qualification NONE=new Qualification(Set.of(),Set.of(),false);
        public static final Qualification ALL=new Qualification(EnumSet.allOf(SpellType.class),Set.of(),true);
        public Qualification { schools=Set.copyOf(schools);items=Set.copyOf(items); }
        public static Qualification schools(SpellType... schools) {
            return new Qualification(Set.of(schools),Set.of(),false);
        }
        public static Qualification items(ResourceLocation... items) {
            return new Qualification(Set.of(),Set.of(items),false);
        }
        public boolean empty(){return schools.isEmpty()&&items.isEmpty()&&!allStaves;}
        public boolean matches(Item item){
            return item instanceof IWand&&(allStaves||items.contains(BuiltInRegistries.ITEM.getKey(item)));
        }
        public boolean matches(SpellType school) {
            if(schools.contains(school)||allStaves)return true;
            // Explicit staff identities also carry their native school qualifications.
            for(var id:items)if(BuiltInRegistries.ITEM.get(id) instanceof IWand wand&&wand.getSpellTypes().contains(school))return true;
            return false;
        }
        private Qualification merge(Qualification other) {
            var mergedSchools=new HashSet<>(schools);mergedSchools.addAll(other.schools);
            var mergedItems=new HashSet<>(items);mergedItems.addAll(other.items);
            return new Qualification(mergedSchools,mergedItems,allStaves||other.allStaves);
        }
    }
    private record Provider(ResourceLocation id,Function<LivingEntity,Qualification> lookup) { }
    private static volatile List<Provider> providers=List.of();

    public static synchronized void register(ResourceLocation id,Function<LivingEntity,Qualification> lookup) {
        Objects.requireNonNull(id);Objects.requireNonNull(lookup);
        if(providers.stream().anyMatch(p->p.id().equals(id)))throw new IllegalArgumentException("Duplicate staff qualification provider: "+id);
        var next=new ArrayList<>(providers);next.add(new Provider(id,lookup));providers=List.copyOf(next);
    }

    /** Native hand contents are preserved. Qualified copies exist only as casting arguments. */
    public static ItemStack apply(LivingEntity caster,ItemStack nativeStaff) {
        if(caster==null||caster instanceof Player||providers.isEmpty())return nativeStaff;
        var qualification=Qualification.NONE;
        for(var provider:providers) {
            var extra=provider.lookup().apply(caster);
            if(extra!=null)qualification=qualification.merge(extra);
        }
        if(qualification.empty())return nativeStaff;
        if(qualification.equals(read(nativeStaff)))return nativeStaff;
        var copy=nativeStaff.isEmpty()
                ?new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("goety","dark_wand")))
                :nativeStaff.copy();
        if(!copy.isEmpty())((QualifiedStaffAccess)(Object)copy).goetyNouveau$qualification(qualification);
        return copy;
    }

    public static Qualification read(ItemStack staff) {
        return (Object)staff instanceof QualifiedStaffAccess access?access.goetyNouveau$qualification():null;
    }
    private StaffQualifications() { }
}
