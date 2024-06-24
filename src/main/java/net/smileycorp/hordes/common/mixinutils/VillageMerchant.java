package net.smileycorp.hordes.common.mixinutils;

import net.minecraft.nbt.Tag;
import net.minecraft.world.item.trading.MerchantOffers;

public interface VillageMerchant {
    
    void setMerchantOffers(MerchantOffers offers);
    
    MerchantOffers getMerchantOffers();
    
    Tag getMerchantGossips();
    
    void setMerchantGossips(Tag gossips);
    
    int getMerchantXp();
    
    void setMerchantXp(int xp);
    
}
