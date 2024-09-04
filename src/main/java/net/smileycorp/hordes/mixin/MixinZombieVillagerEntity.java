package net.smileycorp.hordes.mixin;

import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.mixinutils.CustomTexture;
import net.smileycorp.hordes.config.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieVillagerEntity.class)
public abstract class MixinZombieVillagerEntity extends ZombieEntity {

	public MixinZombieVillagerEntity(World level) {
		super(null, level);
	}
	
	@Inject(at=@At("HEAD"), method = "mobInteract", cancellable = true)
	public void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResultType> callback) {
		if (CommonConfigHandler.zombieVillagersCanBeCured.get() &!((CustomTexture)this).hasCustomTexture()) return;
		callback.setReturnValue(super.mobInteract(player, hand));
	}

}
