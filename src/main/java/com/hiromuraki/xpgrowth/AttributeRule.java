package com.hiromuraki.xpgrowth;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;

public record AttributeRule(String key, Holder<Attribute> attribute, double maxBonus, boolean enabled) {

    public Identifier getModifierId() {
        return Identifier.fromNamespaceAndPath(XPGrowth.MOD_ID, key);
    }
}