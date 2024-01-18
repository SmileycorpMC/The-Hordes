package net.smileycorp.hordes.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.mixinutils.CustomTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieVillager.class)
public abstract class MixinZombieVillager extends Zombie {

	public MixinZombieVillager(Level level) {
		super(null, level);
	}

	@Inject(at=@At("HEAD"), method = "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", cancellable = true)
	public void interact(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
		if (CommonConfigHandler.zombieVillagersCanBeCured.get() &!((CustomTexture)this).hasCustomTexture()) return;
		callback.setReturnValue(super.mobInteract(player, hand));
		callback.cancel();
	}

}
