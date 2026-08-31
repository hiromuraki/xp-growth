package com.hiromuraki.xpgrowth;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

public final class XPGrowthAttachments {
    // 持久化：随实体 NBT 自动存取；Codec.FLOAT 决定序列化格式
    @SuppressWarnings("null")
    public static final AttachmentType<Float> SAVED_HEALTH = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(XPGrowth.MOD_ID, "saved_health"),
            Codec.FLOAT);

    public static void register() {
    }

    private XPGrowthAttachments() {
    }
}
