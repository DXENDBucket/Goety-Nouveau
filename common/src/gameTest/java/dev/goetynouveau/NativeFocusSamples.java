package dev.goetynouveau;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.api.magic.*;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
public final class NativeFocusSamples {
    public static class Sample extends Spell {
        public int defaultSoulCost(){return 7;}
        public int defaultCastDuration(){return 4;}
        public int defaultSpellCooldown(){return 20;}
        public SpellType getSpellType(){return SpellType.FROST;}
        @SuppressWarnings("rawtypes") public java.util.List acceptedEnchantments(){return java.util.List.of();}
        public boolean conditionsMet(ServerLevel world,LivingEntity caster){return !caster.getPersistentData().getBoolean("fixture_reject");}
        private void count(LivingEntity caster,String key){caster.getPersistentData().putInt(key,caster.getPersistentData().getInt(key)+1);}
        public void startSpell(ServerLevel world,LivingEntity caster,ItemStack wand,SpellStat stats){
            count(caster,"fixture_start");caster.getPersistentData().putBoolean("fixture_frost",typeStaff(wand,SpellType.FROST));
            caster.getPersistentData().putBoolean("fixture_virtual",WandUtil.findWand(caster)==wand&&WandUtil.findFocus(caster).getItem() instanceof com.Polarice3.Goety.api.items.magic.IFocus);
            caster.getPersistentData().putInt("fixture_potency",WandUtil.getStats(caster,this).getPotency());
        }
        public void useSpell(ServerLevel world,LivingEntity caster,ItemStack wand,int elapsed,SpellStat stats){count(caster,"fixture_use");stats.increasePotency(100);}
        public void SpellResult(ServerLevel world,LivingEntity caster,ItemStack wand,SpellStat stats){count(caster,"fixture_result");caster.getPersistentData().putInt("fixture_result_potency",stats.getPotency());caster.getPersistentData().putInt("fixture_shots",WandUtil.getShots(caster));}
        public void stopSpell(ServerLevel world,LivingEntity caster,ItemStack wand,ItemStack focus,int elapsed,SpellStat stats){count(caster,"fixture_stop");}
    }
    public static class Charge extends Sample implements IChargingSpell {
        public int defaultCastDuration(){return 72000;}
        public int castUp(LivingEntity caster,ItemStack wand){return 2;}
        public int Cooldown(){return 1;}
        public int shotsNumber(){return 3;}
        public int shotsNumber(LivingEntity caster,ItemStack wand){return shotsNumber();}
    }
    public static final class Ever extends Charge {
        public boolean everCharge(){return true;}
        public int shotsNumber(){return 0;}
    }
    private NativeFocusSamples(){}
}
