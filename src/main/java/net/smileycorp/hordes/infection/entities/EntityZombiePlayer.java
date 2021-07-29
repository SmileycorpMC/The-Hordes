package net.smileycorp.hordes.infection.entities;

import java.util.UUID;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;


public class EntityZombiePlayer extends EntityZombie {
	
	protected static final DataParameter<Optional<UUID>> PLAYER_UUID = EntityDataManager.createKey(EntityZombiePlayer.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	
	protected NonNullList<ItemStack> playerItems = NonNullList.<ItemStack>create();
	protected UUID uuid;
	
	public EntityZombiePlayer(World world) {
		super(world);
	}
	
	public EntityZombiePlayer(EntityPlayer player) {
		this(player.world);
		setPlayer(player);
	}
	

	@Override
	protected void entityInit() {
        super.entityInit();
        dataManager.register(PLAYER_UUID, Optional.of(UUID.fromString("1512ce82-00e5-441a-9774-f46d9b7badfb")));
    }
	
	public void setPlayer(EntityPlayer player) {
		InventoryPlayer inv  = player.inventory;
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			ItemStack stack = slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR ? inv.armorItemInSlot(slot.getIndex()) :
				slot == EntityEquipmentSlot.MAINHAND ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
			setItemStackToSlot(slot, stack);
		}
		for (ItemStack stack : inv.mainInventory) {
			playerItems.add(stack.copy());
		}
		for (ItemStack stack : inv.armorInventory) {
			playerItems.add(stack.copy());
		}
		for (ItemStack stack : inv.offHandInventory) {
			playerItems.add(stack.copy());
		}
		setPlayer(player.getGameProfile());
	}
	
	public void setPlayer(String username) {
		setPlayer(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getGameProfileForUsername(username));
	}
	
	public void setPlayer(UUID uuid) {
		setPlayer(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(uuid));
	}
	
	public void setPlayer(GameProfile profile) {
		this.uuid=profile.getId();
		this.setCustomNameTag(profile.getName());
		dataManager.set(PLAYER_UUID, Optional.of(uuid));
	}

	public UUID getPlayerUUID() {
		return dataManager.get(PLAYER_UUID).get();
	}
	
	@Override
	protected void dropEquipment(boolean recentlyHit, int looting) {
		for (ItemStack stack : playerItems) {
            if (!stack.isEmpty() && ! EnchantmentHelper.hasVanishingCurse(stack)) {
                entityDropItem(stack, 0f);
            }
        }
    }
	
	@Override
	public boolean shouldBurnInDay() {
		return false;
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (uuid != null) {
        	compound.setString("player", uuid.toString());
        }
        ItemStackHelper.saveAllItems(compound, playerItems);
    }
	
    @Override
	public void readEntityFromNBT(NBTTagCompound compound) {
       super.readEntityFromNBT(compound);
       if (compound.hasKey("player")) {
    	   uuid = UUID.fromString(compound.getString("player"));
       }
       NonNullList<ItemStack> read = NonNullList.<ItemStack>withSize(compound.getTagList("Items", 10).tagCount(), ItemStack.EMPTY);
       ItemStackHelper.loadAllItems(compound, read);
       playerItems = read;
    }
	
	@Override
	public ITextComponent getDisplayName() {
        TextComponentTranslation textcomponentstring = new TextComponentTranslation(ScorePlayerTeam.formatPlayerName(getTeam(), "entity.hordes.ZombiePlayer.chat"), ScorePlayerTeam.formatPlayerName(getTeam(), getName()));
        textcomponentstring.getStyle().setHoverEvent(this.getHoverEvent());
        textcomponentstring.getStyle().setInsertion(this.getCachedUniqueIdString());
        return textcomponentstring;
    }
	

}
