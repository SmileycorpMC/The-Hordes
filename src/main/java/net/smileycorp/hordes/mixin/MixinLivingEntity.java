package net.smileycorp.hordes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.mixinutils.ChatName;
import net.smileycorp.hordes.common.mixinutils.CustomTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements CustomTexture {

    private static final DataParameter<String> TEXTURE = EntityDataManager.defineId(LivingEntity.class, DataSerializers.STRING);

    public MixinLivingEntity(EntityType<?> p_19870_, World p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Override
    public void setTexture(ResourceLocation texture) {
        entityData.set(TEXTURE, texture.toString());
    }

    @Override
    public ResourceLocation getTexture() {
        return new ResourceLocation(entityData.get(TEXTURE));
    }

    @Override
    public boolean hasCustomTexture() {
        return !entityData.get(TEXTURE).isEmpty();
    }

    @Inject(at=@At("HEAD"), method = "defineSynchedData")
    public void defineSynchedData(CallbackInfo callback){
        entityData.define(TEXTURE, "");
    }

    @Inject(at=@At("HEAD"), method = "addAdditionalSaveData")
    public void addAdditionalSaveData(CompoundNBT tag, CallbackInfo callback) {
        if (hasCustomTexture()) tag.putString("texture", entityData.get(TEXTURE));
        if (((ChatName)this).hasChatName()) tag.putString("chat_name", ((ChatName)this).getChatName());
    }

    @Inject(at=@At("HEAD"), method = "readAdditionalSaveData")
    public void readAdditionalSaveData(CompoundNBT tag, CallbackInfo callback) {
        if (tag.contains("texture")) {
            String texture = tag.getString("texture");
            if (ResourceLocation.isValidResourceLocation(texture)) entityData.set(TEXTURE, texture);
        }
        if (tag.contains("chat_name")) ((ChatName)this).setChatName(tag.getString("chat_name"));
    }

}
