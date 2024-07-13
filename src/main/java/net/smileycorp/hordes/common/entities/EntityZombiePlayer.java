package net.smileycorp.hordes.common.entities;

import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.smileycorp.hordes.config.ZombiePlayersConfig;

import java.util.List;
import java.util.UUID;


public class EntityZombiePlayer extends EntityZombie {

	protected static final DataParameter<Optional<UUID>> PLAYER_UUID = EntityDataManager.createKey(EntityZombiePlayer.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	protected static final DataParameter<Boolean> SHOW_CAPE = EntityDataManager.createKey(EntityZombiePlayer.class, DataSerializers.BOOLEAN);

	protected NonNullList<ItemStack> playerItems = NonNullList.<ItemStack>create();
	
	public double xCloakO;
	public double yCloakO;
	public double zCloakO;
	public double xCloak;
	public double yCloak;
	public double zCloak;

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
		dataManager.register(SHOW_CAPE, true);
	}

	public void setPlayer(EntityPlayer player) {
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			ItemStack stack = slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR ? player.inventory.armorInventory.get(slot.getIndex()) :
				slot == EntityEquipmentSlot.MAINHAND ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
			setItemStackToSlot(slot, stack);
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
		UUID uuid = profile.getId();
		setCustomNameTag(profile.getName());
		dataManager.set(PLAYER_UUID, Optional.of(uuid));
	}

	public UUID getPlayerUUID() {
		return dataManager.get(PLAYER_UUID).get();
	}

	public void setInventory(List<EntityItem> list) {
		playerItems.clear();
		for (EntityItem item : list) {
			ItemStack stack = item.getItem();
			item.setDead();
			if (stack != null) playerItems.add(stack.copy());
		}
	}

	@Override
	protected void dropEquipment(boolean recentlyHit, int looting) {
		for (ItemStack stack : playerItems) if (!stack.isEmpty() && ! EnchantmentHelper.hasVanishingCurse(stack))
				entityDropItem(stack, 0f);
	}
	
	@Override
	public void onRemovedFromWorld() {
		dropEquipment(false, 0);
		super.onRemovedFromWorld();
	}

	@Override
	public boolean isImmuneToFire() {
		return ZombiePlayersConfig.zombiePlayersFireImmune ? true : super.isImmuneToFire();
	}

	@Override
	public boolean shouldBurnInDay() {
		return ZombiePlayersConfig.zombiePlayersBurn;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (ZombiePlayersConfig.zombiePlayersOnlyHurtByPlayers &! (source.getTrueSource() instanceof EntityPlayer)) return false;
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		UUID uuid = getPlayerUUID();
		if (uuid != null) compound.setString("player", uuid.toString());
		ItemStackHelper.saveAllItems(compound, playerItems);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("player")) setPlayer(UUID.fromString(compound.getString("player")));
		NonNullList<ItemStack> read = NonNullList.<ItemStack>withSize(compound.getTagList("Items", 10).tagCount(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(compound, read);
		playerItems = read;
	}

	@Override
	public ITextComponent getDisplayName() {
		despawnEntity();
		TextComponentTranslation textcomponentstring = new TextComponentTranslation(ScorePlayerTeam.formatPlayerName(getTeam(), "entity.hordes.ZombiePlayer.chat"), ScorePlayerTeam.formatPlayerName(getTeam(), getName()));
		textcomponentstring.getStyle().setHoverEvent(getHoverEvent());
		textcomponentstring.getStyle().setInsertion(getCachedUniqueIdString());
		return textcomponentstring;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		xCloakO = xCloak;
		yCloakO = yCloak;
		zCloakO = zCloak;
		double d0 = posX - xCloak;
		double d1 = posY - yCloak;
		double d2 = posZ - zCloak;
		if (d0 > 10.0D) {
			xCloak = posX;
			xCloakO = xCloak;
		}
		if (d2 > 10.0D) {
			zCloak = posZ;
			zCloakO = zCloak;
		}
		if (d1 > 10.0D) {
			yCloak = posY;
			yCloakO = yCloak;
		}
		if (d0 < -10.0D) {
			xCloak = posX;
			xCloakO = xCloak;
		}
		if (d2 < -10.0D) {
			zCloak = posZ;
			zCloakO = zCloak;
		}
		if (d1 < -10.0D) {
			yCloak = posY;
			yCloakO = yCloak;
		}
		xCloak += (d0 * 0.25D);
		yCloak += (d1 * 0.25D);
		zCloak += (d2 * 0.25D);
	}
	
	@Override
	protected boolean canDespawn() {
		return playerItems.isEmpty() |! ZombiePlayersConfig.zombiePlayersDespawnPeaceful ? super.canDespawn() : false;
	}
	
	public void setDisplayCape(boolean display) {
		dataManager.set(SHOW_CAPE, display);
	}
	
	public boolean displayCape() {
		return dataManager.get(SHOW_CAPE);
	}
	
}
