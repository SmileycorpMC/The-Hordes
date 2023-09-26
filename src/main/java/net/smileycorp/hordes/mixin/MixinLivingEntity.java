package net.smileycorp.hordes.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.mixinutils.IChatName;
import net.smileycorp.hordes.common.mixinutils.ICustomTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements ICustomTexture {

    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.STRING);

    public MixinLivingEntity(EntityType<?> p_19870_, Level p_19871_) {
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
    public void addAdditionalSaveData(CompoundTag tag, CallbackInfo callback) {
        if (hasCustomTexture()) tag.putString("texture", entityData.get(TEXTURE));
        if (((IChatName)this).hasChatName()) tag.putString("chat_name", ((IChatName)this).getChatName());
    }

    @Inject(at=@At("HEAD"), method = "readAdditionalSaveData")
    public void readAdditionalSaveData(CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("texture")) {
            String texture = tag.getString("texture");
            if (ResourceLocation.isValidResourceLocation(texture)) entityData.set(TEXTURE, texture);
        }
        if (tag.contains("chat_name")) ((IChatName)this).setChatName(tag.getString("chat_name"));
    }

}
