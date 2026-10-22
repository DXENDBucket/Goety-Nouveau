package dev.goetynouveau.platform;

/** Goety changed Flying's enchanted-focus rule alongside the current spell-stat update. */
public final class NativeStatHooks {
    private NativeStatHooks() {}
    public static final boolean ADDITIVE_FLYING = net.neoforged.fml.ModList.get()
            .getModContainerById("goety").orElseThrow().getModInfo().getVersion()
            .compareTo(new org.apache.maven.artifact.versioning.DefaultArtifactVersion("3.2.1")) >= 0;
}
