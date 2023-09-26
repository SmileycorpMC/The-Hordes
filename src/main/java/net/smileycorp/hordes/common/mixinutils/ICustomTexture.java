package net.smileycorp.hordes.common.mixinutils;

import net.minecraft.resources.ResourceLocation;

public interface ICustomTexture {

   boolean hasCustomTexture();

   ResourceLocation getTexture();

   void setTexture(ResourceLocation texture);

}
