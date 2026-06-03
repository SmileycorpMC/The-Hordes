package net.smileycorp.hordes.common.mixinutils;

import net.minecraft.util.ResourceLocation;

public interface CustomTexture {

   boolean hasCustomTexture();

   ResourceLocation getTexture();

   void setTexture(ResourceLocation texture);

}
