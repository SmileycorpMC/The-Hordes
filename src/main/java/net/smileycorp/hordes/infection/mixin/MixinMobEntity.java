package net.smileycorp.hordes.infection.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.InfectionRegister;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MixinMobEntity extends LivingEntity {

	public MixinMobEntity(World world) {
		super(null, world);
	}

	@Inject(at=@At("HEAD"), method = "interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Z", cancellable = true)
	public void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<Boolean> callback) {
		ItemStack stack = player.getItemInHand(hand);
		if (hasEffect(HordesInfection.INFECTED.get())) {
			if (InfectionRegister.isCure(stack)) {
				removeEffect(HordesInfection.INFECTED.get());
				if (!player.level.isClientSide) InfectionPacketHandler.NETWORK_INSTANCE.sendToAllTracking(new CureEntityMessage(this), this);
				if (!player.isCreative()) {
					ItemStack container = stack.getItem().getContainerItem(stack);
					if (stack.isDamageableItem() && player instanceof ServerPlayerEntity) {
						stack.hurt(1, player.level.random, (ServerPlayerEntity) player);
					} else {
						stack.shrink(1);
					}
					if (stack.isEmpty() && !container.isEmpty()) {
						player.setItemInHand(hand, container);
					}
				}
				callback.setReturnValue(true);
				callback.cancel();
			}
		}
	}

}
