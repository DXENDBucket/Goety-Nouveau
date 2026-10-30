package dev.goetynouveau.api;

import dev.arsconflux.api.resource.ResourceAccount;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.*;
import java.util.function.Function;

/** Optional non-player accounts. Player soul storage, totems and loss events remain native. */
public final class SoulEnergyAccounts {
    private record Provider(ResourceLocation id,int priority,Function<LivingEntity,ResourceAccount> lookup) { }
    private static volatile List<Provider> providers=List.of();
    public static synchronized void register(ResourceLocation id,int priority,Function<LivingEntity,ResourceAccount> lookup) {
        Objects.requireNonNull(id);Objects.requireNonNull(lookup);
        if(providers.stream().anyMatch(p->p.id().equals(id)))throw new IllegalArgumentException("Duplicate soul-energy provider: "+id);
        var next=new ArrayList<>(providers);next.add(new Provider(id,priority,lookup));
        next.sort(Comparator.comparingInt(Provider::priority).reversed().thenComparing(p->p.id().toString()));
        providers=List.copyOf(next);
    }
    /** Null means unsupported, never free. Queries must not withdraw energy or initialize balances. */
    public static ResourceAccount find(LivingEntity actor) {
        if(actor==null||actor instanceof Player||actor.level().isClientSide)return null;
        for(var provider:providers) {
            var account=provider.lookup().apply(actor);
            if(account!=null)return account;
        }
        return null;
    }
    private SoulEnergyAccounts() { }
}
