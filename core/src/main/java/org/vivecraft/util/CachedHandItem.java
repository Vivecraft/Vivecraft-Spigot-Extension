package org.vivecraft.util;

import org.vivecraft.api.data.VRBodyPart;

public class CachedHandItem {
    public final VRBodyPart bodyPart;
    public final Object mainItem;

    public CachedHandItem(VRBodyPart bodyPart, Object mainItem) {
        this.bodyPart = bodyPart;
        this.mainItem = mainItem;
    }
}
