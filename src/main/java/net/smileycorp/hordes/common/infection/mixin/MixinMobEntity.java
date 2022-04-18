package net.smileycorp.hordes.common.infection.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.smileycorp.hordes.common.infection.HordesInfection;
import net.smileycorp.hordes.common.infection.InfectionRegister;
import net.smileycorp.hordes.common.infection.network.CureEntityMessage;
import net.smileycorp.hordes.common.infection.network.InfectionPacketHandler;

@Mixin(MobEntity.class)
public abstract class MixinMobEntity extends LivingEntity {

	public MixinMobEntity(Level level) {
		super(null, level);
	}

	@Inject(at=@At("HEAD"), method = "checkAndHandleImportantInteractions(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResultType;", cancellable = true)
	public void interact(Player player, Hand hand, CallbackInfoReturnable<ActionResultType> callback) {
		ItemStack stack = player.getItemInHand(hand);
		if (hasEffect(HordesInfection.INFECTED.get())) {
			if (InfectionRegister.isCure(stack)) {
				removeEffect(HordesInfection.INFECTED.get());
				if (!player.level.isClientSide) InfectionPacketHandler.NETWORK_INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(()->player.level.getChunkAt(getOnPos())), new CureEntityMessage(this));
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
				callback.setReturnValue(ActionResultType.sidedSuccess(player.level.isClientSide));
				callback.cancel();
			}
		}
	}

}
