package net.smileycorp.hordes.mixin;

import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.mixinutils.CustomTexture;
import net.smileycorp.hordes.common.mixinutils.VillageMerchant;
import net.smileycorp.hordes.config.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ZombieVillager.class)
public abstract class MixinZombieVillager extends Zombie implements VillageMerchant {
	
	@Shadow public abstract void setTradeOffers(MerchantOffers pTradeOffers);
	
	@Shadow @Nullable private MerchantOffers tradeOffers;
	
	@Shadow @Nullable private Tag gossips;
	
	@Shadow public abstract void setGossips(Tag pGossips);
	
	@Shadow public abstract int getVillagerXp();
	
	@Shadow public abstract void setVillagerXp(int pVillagerXp);
	
	public MixinZombieVillager(Level level) {
		super(null, level);
	}

	@Inject(at=@At("HEAD"), method = "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", cancellable = true)
	public void interact(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
		if (CommonConfigHandler.zombieVillagersCanBeCured.get() &!((CustomTexture)this).hasCustomTexture()) return;
		callback.setReturnValue(super.mobInteract(player, hand));
	}
	
	@Override
	public void setMerchantOffers(MerchantOffers offers) {
		setTradeOffers(offers);
	}
	
	@Override
	public MerchantOffers getMerchantOffers() {
		return tradeOffers;
	}
	
	@Override
	public Tag getMerchantGossips() {
		return gossips;
	}
	
	@Override
	public void setMerchantGossips(Tag gossips) {
		setGossips(gossips);
	}
	
	@Override
	public int getMerchantXp() {
		return getVillagerXp();
	}
	
	@Override
	public void setMerchantXp(int xp) {
		setVillagerXp(xp);
	}

}
