package net.smileycorp.hordes.common.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.HuskEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.hordes.config.ZombiePlayersConfig;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;


public class HuskPlayerEntity extends HuskEntity implements PlayerZombie<HuskPlayerEntity> {
	
	protected static final DataParameter<Optional<UUID>> PLAYER = EntityDataManager.defineId(DrownedPlayerEntity.class, DataSerializers.OPTIONAL_UUID);
	protected static final DataParameter<Boolean> SHOW_CAPE = EntityDataManager.defineId(DrownedPlayerEntity.class, DataSerializers.BOOLEAN);
	
	protected NonNullList<ItemStack> playerItems = NonNullList.create();

	public double xCloakO;
	public double yCloakO;
	public double zCloakO;
	public double xCloak;
	public double yCloak;
	public double zCloak;

	public HuskPlayerEntity(EntityType<? extends HuskPlayerEntity> type, World level) {
		super(type, level);
	}

	public HuskPlayerEntity(World level) {
		this(HordesEntities.HUSK_PLAYER.get() ,level);
	}

	public HuskPlayerEntity(PlayerEntity player) {
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
	public void setPlayer(PlayerEntity player) {
		if (player == null) return;
		for (EquipmentSlotType slot : EquipmentSlotType.values()) setItemSlot(slot, player.getItemBySlot(slot));
		setPlayer(player.getGameProfile());
	}
	
	@Override
	public void setPlayer(String username) {
		GameProfile profile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(username);
		if (profile != null) setPlayer(profile);
	}
	
	@Override
	public void setPlayer(UUID uuid) {
		GameProfile profile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
		if (profile != null) setPlayer(profile);
	}
	
	@Override
	public void setPlayer(GameProfile profile) {
		if (profile == null) return;
		if (profile.getName() == null) setCustomName(new StringTextComponent(profile.getName()));
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
			item.remove(false);
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
		ZombieEntity zombie = convertTo(HordesEntities.ZOMBIE_PLAYER.get(), true);
		if (zombie != null) {
			zombie.handleAttributes(zombie.level.getCurrentDifficultyAt(zombie.blockPosition()).getSpecialMultiplier());
			zombie.setCanBreakDoors(zombie.supportsBreakDoorGoal() && this.canBreakDoors());
			ForgeEventFactory.onLivingConvert(this, zombie);
			if (zombie instanceof PlayerZombie) ((PlayerZombie) zombie).copyFrom(this);
		}
		if (!this.isSilent()) {
			level.levelEvent(null, 1040, this.blockPosition(), 0);
		}
	}

	@Override
	public boolean fireImmune() {
		return ZombiePlayersConfig.zombiePlayersFireImmune.get();
	}
	
	@Override
	public void addAdditionalSaveData(CompoundNBT compound) {
		super.addAdditionalSaveData(compound);
		Optional<UUID> optional = entityData.get(PLAYER);
		if (optional.isPresent()) {
			compound.putUUID("player", optional.get());
		}
		ItemStackHelper.saveAllItems(compound, playerItems);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("player")) {
			entityData.set(PLAYER, Optional.of(compound.getUUID("player")));
		}
		NonNullList<ItemStack> read = NonNullList.<ItemStack>withSize(compound.getList("Items", 10).size(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(compound, read);
		playerItems = read;
	}
	
	@Override
	public ITextComponent getDisplayName() {
		TranslationTextComponent textcomponentstring = new TranslationTextComponent(ScorePlayerTeam.formatNameForTeam(getTeam(),
				new StringTextComponent("entity.hordes.HuskPlayer.chat")).getString(), ScorePlayerTeam.formatNameForTeam(getTeam(), getName()));
		textcomponentstring.getStyle().withHoverEvent(this.createHoverEvent());
		textcomponentstring.getStyle().withInsertion(this.getEncodeId());
		return textcomponentstring;
	}
	
	@Override
	public void copyFrom(PlayerZombie entity) {
		setPlayer((UUID)entity.getPlayerUUID().get());
		setInventory(entity.getInventory());
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			ItemStack stack = entity.asEntity().getItemBySlot(slot);
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
