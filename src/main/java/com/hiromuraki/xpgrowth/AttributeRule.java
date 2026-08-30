package com.hiromuraki.xpgrowth;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;

public record AttributeRule(
        String key,
        Holder<Attribute> attribute,
        int startLevel,
        int maxLevel,
        int step,
        double stepBonus,
        boolean enabled) {

    public AttributeRule {
        if (step < 1) {
            throw new IllegalArgumentException("step must be >= 1");
        }
        if (startLevel < 0) {
            throw new IllegalArgumentException("startLevel must be >= 0");
        }
        if (maxLevel < 0) {
            throw new IllegalArgumentException("maxLevel must be >= 0");
        }
        if (maxLevel < startLevel) {
            throw new IllegalArgumentException("maxLevel must be >= startLevel");
        }
    }

    public Identifier getModifierId() {
        return Identifier.fromNamespaceAndPath(XPGrowth.MOD_ID, key);
    }
}
