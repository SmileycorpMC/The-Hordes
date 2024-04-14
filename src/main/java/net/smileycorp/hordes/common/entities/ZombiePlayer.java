package net.smileycorp.hordes.common.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.hordes.config.ZombiePlayersConfig;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;


public class ZombiePlayer extends Zombie implements PlayerZombie<ZombiePlayer> {

	protected static final EntityDataAccessor<Optional<UUID>> PLAYER = SynchedEntityData.defineId(ZombiePlayer.class, EntityDataSerializers.OPTIONAL_UUID);
	protected static final EntityDataAccessor<Boolean> SHOW_CAPE = SynchedEntityData.defineId(ZombiePlayer.class, EntityDataSerializers.BOOLEAN);

	protected NonNullList<ItemStack> playerItems = NonNullList.<ItemStack>create();

	public double xCloakO;
	public double yCloakO;
	public double zCloakO;
	public double xCloak;
	public double yCloak;
	public double zCloak;

	public ZombiePlayer(EntityType<? extends ZombiePlayer> type, Level level) {
		super(type, level);
	}

	public ZombiePlayer(Level level) {
		this(HordesEntities.ZOMBIE_PLAYER.get() ,level);
	}

	public ZombiePlayer(Player player) {
		this(player.level);
		setPlayer(player);
	}

	@Override
	protected void defineSynchedData(){
		super.defineSynchedData();
		entityData.define(PLAYER, Optional.empty());
		entityData.define(SHOW_CAPE, true);
	}

	@Override
	public void setPlayer(Player player) {
		if (player == null) return;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			ItemStack stack = slot.getType() == EquipmentSlot.Type.ARMOR ? player.getInventory().armor.get(slot.getIndex()) :
				slot == EquipmentSlot.MAINHAND ? player.getMainHandItem() : player.getOffhandItem();
			setItemSlot(slot, stack);
		}
		setPlayer(player.getGameProfile());
	}

	@Override
	public void setPlayer(String username) {
		Optional<GameProfile> optional = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(username);
		if (optional.isPresent()) setPlayer(optional.get());
	}

	@Override
	public void setPlayer(UUID uuid) {
		Optional<GameProfile> optional = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
		if (optional.isPresent()) setPlayer(optional.get());
	}

	@Override
	public void setPlayer(GameProfile profile) {
		if (profile == null) return;
		if (profile.getName() != null) setCustomName(Component.literal(profile.getName()));
		entityData.set(PLAYER, Optional.of(profile.getId()));
	}

	@Override
	public Optional<UUID> getPlayerUUID() {
		return entityData.get(PLAYER);
	}

	@Override
	public void storeDrops(Collection<ItemEntity> list) {
		playerItems.clear();
		for (ItemEntity item : list) {
			ItemStack stack = item.getItem();
			item.remove(RemovalReason.DISCARDED);
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
	protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn){
		for (ItemStack stack : playerItems) {
			if (!stack.isEmpty() && ! EnchantmentHelper.hasVanishingCurse(stack)) {
				spawnAtLocation(stack, 0f);
			}
		}
	}

	@Override
	protected void doUnderWaterConversion() {
		if (ZombiePlayersConfig.drownedPlayers.get()) {
			Zombie drowned = convertTo(HordesEntities.DROWNED_PLAYER.get(), true);
			if (drowned != null) {
				drowned.handleAttributes(drowned.level.getCurrentDifficultyAt(drowned.blockPosition()).getSpecialMultiplier());
				drowned.setCanBreakDoors(drowned.supportsBreakDoorGoal() && this.canBreakDoors());
				ForgeEventFactory.onLivingConvert(this, drowned);
				if (drowned instanceof PlayerZombie) ((PlayerZombie) drowned).copyFrom(this);
			}
			if (!this.isSilent()) {
				level.levelEvent(null, 1040, this.blockPosition(), 0);
			}
		}
	}

	@Override
	public boolean isSunSensitive() {
		return ZombiePlayersConfig.zombiePlayersBurn.get();
	}

	@Override
	public boolean fireImmune() {
		return ZombiePlayersConfig.zombiePlayersFireImmune.get();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		Optional<UUID> optional = entityData.get(PLAYER);
		if (optional.isPresent()) {
			compound.putUUID("player", optional.get());
		}
		ContainerHelper.saveAllItems(compound, playerItems);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("player")) {
			entityData.set(PLAYER, Optional.of(compound.getUUID("player")));
		}
		NonNullList<ItemStack> read = NonNullList.<ItemStack>withSize(compound.getList("Items", 10).size(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, read);
		playerItems = read;
	}

	@Override
	public MutableComponent getDisplayName() {
		MutableComponent textcomponentstring = PlayerTeam.formatNameForTeam(getTeam(),
				Component.translatable("entity.hordes.ZombiePlayer.chat", getCustomName()));
		textcomponentstring.getStyle().withHoverEvent(this.createHoverEvent());
		textcomponentstring.getStyle().withInsertion(this.getEncodeId());
		return textcomponentstring;
	}

	@Override
	public void copyFrom(PlayerZombie entity) {
		Optional<UUID> optional = entity.getPlayerUUID();
		if(optional.isPresent()) setPlayer(optional.get());
		setInventory(entity.getInventory());
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			ItemStack stack = ((Mob) entity).getItemBySlot(slot);
			setItemSlot(slot, stack);
		}
		entityData.set(SHOW_CAPE, entity.displayCape());
	}

	@Override
	public void tick() {
		super.tick();
		moveCloak(this);
	}

	@Override
	public void checkDespawn() {
		if (level.getDifficulty() == Difficulty.PEACEFUL) super.checkDespawn();
	}

	@Override
	public void setDisplayCape(boolean display) {
		entityData.set(SHOW_CAPE, display);
	}

	@Override
	public boolean displayCape() {
		return entityData.get(SHOW_CAPE);
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

}
