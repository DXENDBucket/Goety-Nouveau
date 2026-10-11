package dev.goetynouveau.casting;

import dev.arsconflux.api.context.CastContext;
import java.util.function.BooleanSupplier;

/** Server-only execution roles. Native owner metadata continues to identify the spatial host. */
public interface ManagedBeam {
    void goetyNouveau$bindBeam(CastContext context, BooleanSupplier active);
    CastContext goetyNouveau$beamContext();
}
