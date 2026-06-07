package net.smileycorp.hordes.common.entities;

import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityHusk;
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
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.smileycorp.hordes.config.ZombiePlayersConfig;

import java.util.Collection;
import java.util.UUID;


public class EntityHuskPlayer extends EntityHusk implements PlayerZombie<EntityHuskPlayer> {

	public static final Type<EntityHuskPlayer> HUSK = new Type<>("husk", EntityHuskPlayer.class);

	protected static final DataParameter<Optional<UUID>> PLAYER = EntityDataManager.createKey(EntityHuskPlayer.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	protected static final DataParameter<Boolean> SHOW_CAPE = EntityDataManager.createKey(EntityHuskPlayer.class, DataSerializers.BOOLEAN);

	protected NonNullList<ItemStack> playerItems = NonNullList.<ItemStack>create();

	public double xCloakO;
	public double yCloakO;
	public double zCloakO;
	public double xCloak;
	public double yCloak;
	public double zCloak;

	public EntityHuskPlayer(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(PLAYER, Optional.absent());
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

	@Override
	public void setPlayer(String username) {
		setPlayer(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getGameProfileForUsername(username));
	}

	@Override
	public void setPlayer(UUID uuid) {
		setPlayer(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(uuid));
	}

	@Override
	public void setPlayer(GameProfile profile) {
		if (profile == null) return;
		if (profile.getName() != null) setCustomNameTag(profile.getName());
		dataManager.set(PLAYER, Optional.of(profile.getId()));
	}

	@Override
	public Optional<UUID> getPlayerUUID() {
		return dataManager.get(PLAYER);
	}

	@Override
	public void storeDrops(Collection<EntityItem> drops) {
		playerItems.clear();
		for (EntityItem item : drops) {
			ItemStack stack = item.getItem();
			item.setDead();
			if (stack != null) playerItems.add(stack.copy());
		}
	}

	@Override
	public void setInventory(NonNullList<ItemStack> list) {
		playerItems.clear();
		playerItems.addAll(list);
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return playerItems;
	}

	@Override
	protected void dropEquipment(boolean recentlyHit, int looting) {
		for (ItemStack stack : playerItems) if (!stack.isEmpty() && ! EnchantmentHelper.hasVanishingCurse(stack))
				entityDropItem(stack, 0f);
		playerItems.clear();
	}
	
	@Override
	public void onRemovedFromWorld() {
		if (world.getDifficulty() == EnumDifficulty.PEACEFUL  && canDespawn()) dropEquipment(false, 0);
		super.onRemovedFromWorld();
	}

	@Override
	public boolean isImmuneToFire() {
		return ZombiePlayersConfig.zombiePlayersFireImmune ? true : super.isImmuneToFire();
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (ZombiePlayersConfig.zombiePlayersOnlyHurtByPlayers &! (source.getTrueSource() instanceof EntityPlayer)) return false;
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		Optional<UUID> uuid = getPlayerUUID();
		if (uuid.isPresent()) compound.setString("player", uuid.toString());
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
		TextComponentTranslation textcomponentstring = new TextComponentTranslation(ScorePlayerTeam.formatPlayerName(getTeam(), "entity.hordes.husk_player.chat"), ScorePlayerTeam.formatPlayerName(getTeam(), getName()));
		textcomponentstring.getStyle().setHoverEvent(getHoverEvent());
		textcomponentstring.getStyle().setInsertion(getCachedUniqueIdString());
		return textcomponentstring;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		moveCloak();
	}
	
	@Override
	protected boolean canDespawn() {
		return playerItems.isEmpty() | !ZombiePlayersConfig.zombiePlayersDespawnPeaceful && super.canDespawn();
	}

	@Override
	public void setDisplayCape(boolean display) {
		dataManager.set(SHOW_CAPE, display);
	}

	@Override
	public boolean displayCape() {
		return dataManager.get(SHOW_CAPE);
	}

	@Override
	public double getXCloakO() {
		return xCloakO;
	}

	@Override
	public double getYCloakO() {
		return yCloakO;
	}

	@Override
	public double getZCloakO() {
		return zCloakO;
	}

	@Override
	public double getXCloak() {
		return xCloak;
	}

	@Override
	public double getYCloak() {
		return yCloak;
	}

	@Override
	public double getZCloak() {
		return zCloak;
	}

	@Override
	public void setXCloakO(double value) {
		xCloakO = value;
	}

	@Override
	public void setYCloakO(double value) {
		yCloakO = value;
	}

	@Override
	public void setZCloakO(double value) {
		zCloakO = value;
	}

	@Override
	public void setXCloak(double value) {
		xCloak = value;
	}

	@Override
	public void setYCloak(double value) {
		yCloak = value;
	}

	@Override
	public void setZCloak(double value) {
		zCloak = value;
	}

	@Override
	public Type<EntityHuskPlayer> getType() {
		return HUSK;
	}

}
