package net.smileycorp.hordes.mixin;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.mixinutils.VillageMerchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Villager.class)
public abstract class MixinVillager extends AbstractVillager implements VillageMerchant {
    
    public MixinVillager(EntityType<? extends AbstractVillager> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    
    @Shadow public abstract void setOffers(MerchantOffers pOffers);
    
    @Shadow public abstract GossipContainer getGossips();
    
    @Shadow public abstract void setGossips(Tag pGossip);
    
    @Shadow public abstract int getVillagerXp();
    
    @Shadow public abstract void setVillagerXp(int pVillagerXp);
    
    @Override
    public void setMerchantOffers(MerchantOffers offers) {
        setOffers(offers);
    }
    
    @Override
    public MerchantOffers getMerchantOffers() {
        return getOffers();
    }
    
    @Override
    public Tag getMerchantGossips() {
        return getGossips().store(NbtOps.INSTANCE);
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
