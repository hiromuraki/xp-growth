package com.hiromuraki.xprogue;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeRule(String key, Holder<Attribute> attribute, double perLevel, boolean enabled) {

    public Identifier modifierId() {
        return Identifier.fromNamespaceAndPath(XPRogue.MOD_ID, key);
    }

    public AttributeModifier modifier(double level) {
        return new AttributeModifier(modifierId(), perLevel * level, AttributeModifier.Operation.ADD_VALUE);
    }
}