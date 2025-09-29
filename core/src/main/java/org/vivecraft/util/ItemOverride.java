package org.vivecraft.util;

import org.vivecraft.api.data.VRBodyPart;

public class ItemOverride {
    public final Object original;
    public final Object override;
    public final VRBodyPart overridePart;

    public ItemOverride(VRBodyPart overridePart, Object original, Object override) {
        this.overridePart = overridePart;
        this.original = original;
        this.override = override;
    }
}
