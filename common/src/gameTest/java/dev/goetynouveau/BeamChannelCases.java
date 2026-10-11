package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.projectiles.CorruptedBeam;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.casting.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

/** Real native beam damage/trace/lifecycle, through the actual glyph resource transaction. */
public final class BeamChannelCases {
    private static CorruptedBeam beam(GameTestHelper h) {
        var beams = RainStormCases.entities(h, CorruptedBeam.class);
        h.assertTrue(beams.size() == 1, "One real native beam per channel");
        return beams.get(0);
    }
    private static void finish(GameTestHelper h, ServerPlayer p) {
        p.discard();
        RainStormCases.clear(h, p);
    }

    public static void nativeGeometryDamageAndAlliance(GameTestHelper h, ServerPlayer p) {
        RainStormCases.prepare(h, p);
        var host = RainStormCases.host(h, p);
        host.setYRot(0); host.setXRot(0);
        var target = host.getTarget();
        target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);
        target.setHealth(200);
        var ally = EntityType.COW.create(h.getLevel());
        ally.setNoAi(true); ally.setNoGravity(true); ally.setPos(host.position().add(0, 0, 4));
        h.getLevel().addFreshEntity(ally);
        var board = h.getLevel().getScoreboard();
        board.addPlayerToTeam(ally.getScoreboardName(), board.getPlayersTeam(p.getScoreboardName()));
        var owner = EntityType.COW.create(h.getLevel());
        owner.setNoAi(true); owner.setNoGravity(true); owner.setPos(host.position().add(-10, 0, 0));
        h.getLevel().addFreshEntity(owner);
        var casterPosition = p.position();
        RainStormCases.cast(h, p, GlyphCatalog.CORRUPTED_BEAM, host, owner, true, AugmentAmplify.INSTANCE);
        var beam = beam(h);
        h.assertTrue(beam.getOwner() == host && beam.getOwnerId() == host.getId(), "Native spatial owner and client metadata identify the real allied executor");
        h.assertTrue(((ArsTriggeredEntity)beam).goetyNouveau$arsTriggered() && !beam.shouldBeSaved(), "Managed native beam has synced provenance and remains transient");
        h.assertTrue(beam.extraDamage > 0 && !beam.itemBase, "Native potency retained without a native held wand");
        float health = target.getHealth(), friendly = ally.getHealth();
        beam.tick();
        h.assertTrue(!beam.isRemoved() && target.getHealth() < health, "Native beam traces and deals actual damage from ally");
        h.assertTrue(target.getLastDamageSource().getEntity() == owner, "Explicit damage owner is distinct from spatial owner");
        h.assertTrue(ally.getHealth() == friendly && host.getHealth() == host.getMaxHealth(), "Native host and original-caster allies are protected");
        h.assertTrue(p.position().equals(casterPosition), "Caster was not relocated");

        // Live native facing, not the cast-time direction or a stale Ars hit.
        health = target.getHealth(); host.setYRot(90); beam.tick();
        h.assertTrue(target.getHealth() == health && Math.abs(beam.getYRot() - 90) < .01, "Changing executor facing changes beam direction");
        host.setYRot(0);
        var wall = BlockPos.containing(host.position()).offset(0, 0, 3);
        for (int x = -1; x <= 1; x++) for (int y = 0; y < 3; y++)
            h.getLevel().setBlock(wall.offset(x, y, 0), Blocks.STONE.defaultBlockState(), 2);
        beam.tick();
        h.assertTrue(target.getHealth() == health, "Native block collision prevents damage behind a wall");
        RainStormCases.cast(h, p, GlyphCatalog.CORRUPTED_BEAM, target, p, false);

        // Native Goety entities still require a native active wand.
        var ordinary = new CorruptedBeam(ModEntityType.CORRUPTED_BEAM.get(), h.getLevel(), p);
        ordinary.setItemBase(true); h.getLevel().addFreshEntity(ordinary); ordinary.tick();
        h.assertTrue(ordinary.isRemoved() && !((ArsTriggeredEntity)ordinary).goetyNouveau$arsTriggered(), "Unmanaged native wand cancellation remains intact");
        finish(h, p); h.succeed();
    }

    public static void paymentAllegianceAndCleanup(GameTestHelper h, ServerPlayer p) {
        RainStormCases.prepare(h, p);
        var host = RainStormCases.host(h, p); host.setYRot(90); host.setXRot(0);
        int fee = RainStormCases.cast(h, p, GlyphCatalog.CORRUPTED_BEAM, host, p, true);
        var beam = beam(h);
        SEHelper.setSESouls(p, fee * 2);
        h.runAfterDelay(22, () -> {
            h.assertTrue(SEHelper.getSESouls(p) == fee, "Native per-second beam upkeep pays the original caster");
            h.assertTrue(RainStormCases.entities(h, CorruptedBeam.class).size() == 1 && !beam.isRemoved(), "Channel maintains its original native entity without accumulating copies");
            h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());
            beam.tick(); ExecutionSessions.tick();
            h.assertTrue(beam.isRemoved() && !ExecutionSessions.isCasting(p), "Lost alliance removes both beam and channel");

            RainStormCases.clear(h, p);
            var next = RainStormCases.host(h, p); next.setYRot(90); next.setXRot(0);
            SEHelper.setSESouls(p, 100000);
            RainStormCases.cast(h, p, GlyphCatalog.CORRUPTED_BEAM, next, p, true);
            var empty = beam(h); SEHelper.setSESouls(p, 0);
            empty.tick(); ExecutionSessions.tick();
            h.assertTrue(empty.isRemoved() && !ExecutionSessions.isCasting(p), "Empty account cannot retain a native damage entity");

            RainStormCases.cast(h, p, GlyphCatalog.CORRUPTED_BEAM, next, p, false);
            h.assertTrue(RainStormCases.entities(h, CorruptedBeam.class).isEmpty(), "Unfunded trigger creates no beam");
            next.setYRot(0); next.setXRot(0);
            SEHelper.setSESouls(p, fee);
            float health = next.getTarget().getHealth();
            RainStormCases.cast(h, p, GlyphCatalog.CORRUPTED_BEAM, next, p, true);
            h.assertTrue(next.getTarget().getHealth() < health && SEHelper.getSESouls(p) == 0,
                    "Exactly one fee resolves its first actual damage before empty-account stopping");
            var last = beam(h); last.tick(); ExecutionSessions.tick();
            h.assertTrue(last.isRemoved(), "Final funded activation does not leave a free damage entity");
            finish(h, p); h.succeed();
        });
    }

    public static void finiteCreativeAndOrphanCleanup(GameTestHelper h, ServerPlayer p) {
        RainStormCases.prepare(h, p);
        var host = RainStormCases.host(h, p); host.setYRot(90); host.setXRot(0);
        p.setGameMode(GameType.CREATIVE); SEHelper.setSESouls(p, 0);
        RainStormCases.cast(h, p, GlyphCatalog.CORRUPTED_BEAM, host, p, true);
        var beam = beam(h);
        var saved = new CompoundTag(); beam.saveWithoutId(saved);
        var orphan = new CorruptedBeam(ModEntityType.CORRUPTED_BEAM.get(), h.getLevel(), host);
        orphan.load(saved); h.getLevel().addFreshEntity(orphan); orphan.tick();
        h.assertTrue(orphan.isRemoved(), "Restored provenance without a live transient session cannot create a permanent beam");
        var spell = (Spell)GlyphCatalog.CORRUPTED_BEAM.focus().spell();
        int nativeTicks = ((com.Polarice3.Goety.api.magic.IChargingSpell)spell).shotsNumber(p, net.minecraft.world.item.ItemStack.EMPTY);
        int ticks = GoetyChannelCasting.boundedTicks(nativeTicks, 0);
        h.assertTrue(ticks > 0 && ticks < 550, "Fixture uses native finite duration or the configured finite Ars duration");
        h.runAfterDelay(ticks + 2, () -> {
            h.assertTrue(beam.isRemoved() && !ExecutionSessions.isCasting(p) && SEHelper.getSESouls(p) == 0,
                    "Creative beam stays free and naturally removes entity plus channel");
            RainStormCases.cast(h, p, GlyphCatalog.CORRUPTED_BEAM, host, p, true);
            var removed = beam(h); host.discard(); ExecutionSessions.tick();
            h.assertTrue(removed.isRemoved() && !ExecutionSessions.isCasting(p), "Executor removal cleans up the entity");
            var next = RainStormCases.host(h, p); next.setYRot(90); next.setXRot(0);
            RainStormCases.cast(h, p, GlyphCatalog.CORRUPTED_BEAM, next, p, true);
            var abandoned = beam(h); p.discard(); abandoned.tick(); ExecutionSessions.tick();
            h.assertTrue(abandoned.isRemoved() && !ExecutionSessions.isCasting(p), "Original caster removal stops beam before its next native damage tick");
            finish(h, p); h.succeed();
        });
    }
}
