package dev.goetynouveau.api;

import net.minecraft.world.phys.Vec3;

/** Native equipment chooses launch geometry; Ars supplies the reference origin and direction. */
public record ProjectileLaunch(Vec3 origin, Vec3 direction) {}
