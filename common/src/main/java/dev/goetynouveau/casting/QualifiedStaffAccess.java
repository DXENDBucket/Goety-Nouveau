package dev.goetynouveau.casting;

import dev.goetynouveau.api.StaffQualifications.Qualification;

/** Ephemeral casting metadata; not an inventory item, component or saved grant. */
public interface QualifiedStaffAccess {
    Qualification goetyNouveau$qualification();
    void goetyNouveau$qualification(Qualification qualification);
}
