package net.sodiumstudio.dwmg.entities.hmag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import com.github.mechalopa.hmag.registry.ModItems;
import com.github.mechalopa.hmag.world.entity.SlimeGirlEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sodiumstudio.befriendmobs.entity.befriended.BefriendedHelper;
import net.sodiumstudio.befriendmobs.entity.capability.HealingItemTable;
import net.sodiumstudio.befriendmobs.entity.ai.goal.preset.BefriendedMeleeAttackGoal;
import net.sodiumstudio.befriendmobs.entity.ai.goal.preset.move.BefriendedFollowOwnerGoal;
import net.sodiumstudio.befriendmobs.entity.ai.goal.preset.move.BefriendedWaterAvoidingRandomStrollGoal;
import net.sodiumstudio.befriendmobs.entity.ai.goal.preset.target.BefriendedHurtByTargetGoal;
import net.sodiumstudio.befriendmobs.inventory.BefriendedInventory;
import net.sodiumstudio.befriendmobs.inventory.BefriendedInventoryMenu;
import net.sodiumstudio.befriendmobs.item.baublesystem.BaubleHandler;
import net.sodiumstudio.nautils.math.LinearColor;
import net.sodiumstudio.dwmg.Dwmg;
import net.sodiumstudio.dwmg.befriendmobs.entity.ai.goal.preset.move.BefriendedLeapAtOwnerGoal;
import net.sodiumstudio.dwmg.befriendmobs.entity.ai.goal.preset.move.BefriendedLeapAtTargetGoal;
import net.sodiumstudio.dwmg.entities.IDwmgBefriendedMob;
import net.sodiumstudio.dwmg.entities.ai.goals.target.DwmgBefriendedOwnerHurtByTargetGoal;
import net.sodiumstudio.dwmg.entities.ai.goals.target.DwmgBefriendedOwnerHurtTargetGoal;
import net.sodiumstudio.dwmg.entities.ai.goals.target.DwmgNearestHostileToOwnerTargetGoal;
import net.sodiumstudio.dwmg.entities.ai.goals.target.DwmgNearestHostileToSelfTargetGoal;
import net.sodiumstudio.dwmg.entities.item.baublesystem.DwmgBaubleSlotGetter;
import net.sodiumstudio.dwmg.inventory.InventoryMenuFourBaubles;
import net.sodiumstudio.dwmg.inventory.InventoryMenuSlimeGirl;
import net.sodiumstudio.dwmg.item.MagicalGelColorUtils;
import net.sodiumstudio.dwmg.registries.DwmgBaubleHandlers;
import net.sodiumstudio.dwmg.registries.DwmgHealingItems;
import net.sodiumstudio.dwmg.registries.DwmgItems;
import net.sodiumstudio.dwmg.sounds.DwmgSoundPresets;
import net.sodiumstudio.dwmg.util.DwmgEntityHelper;
import net.sodiumstudio.nautils.ReflectHelper;
import net.sodiumstudio.nautils.annotation.DontCallManually;

public class HmagSlimeGirlEntity extends SlimeGirlEntity implements IDwmgBefriendedMob {

	/* Data sync */

	protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID = SynchedEntityData
			.defineId(HmagSlimeGirlEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	protected static final EntityDataAccessor<Integer> DATA_AISTATE = SynchedEntityData
			.defineId(HmagSlimeGirlEntity.class, EntityDataSerializers.INT);
	protected static final EntityDataAccessor<LinearColor> DATA_COLOR = SynchedEntityData
			.defineId(HmagSlimeGirlEntity.class, LinearColor.ENTITY_DATA_SERIALIZER);
	
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(DATA_OWNERUUID, Optional.empty());
		entityData.define(DATA_AISTATE, 0);
		entityData.define(DATA_COLOR, LinearColor.fromNormalized(0.5d, 0.5d, 0.5d));
	}
	
	@Override
	public EntityDataAccessor<Optional<UUID>> getOwnerUUIDAccessor() {
		return DATA_OWNERUUID;
	}

	@Override
	public EntityDataAccessor<Integer> getAIStateData() {
		return DATA_AISTATE;
	}

	/* Initialization */

	public HmagSlimeGirlEntity(EntityType<? extends HmagSlimeGirlEntity> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
		this.xpReward = 0;
		Arrays.fill(this.armorDropChances, 0);
		Arrays.fill(this.handDropChances, 0);
	}

	@Deprecated
	public static Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 60.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.19D)
				.add(Attributes.ATTACK_DAMAGE, 7.0D)
				.add(Attributes.ARMOR, 8.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
	}

	@Override
	public void onInit(UUID playerUUID, Mob from)
	{
		if (from instanceof SlimeGirlEntity sg)
		{
			this.setColorLinear(MagicalGelColorUtils.getSlimeColor(sg));
		}
	}
	
	/* AI */

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new FloatGoal(this));
		goalSelector.addGoal(2, new SGLeapAtTargetGoal(this));
		goalSelector.addGoal(3, new BefriendedMeleeAttackGoal(this, 1.0d, true));
		goalSelector.addGoal(4, new SGLeapAtOwnerGoal(this));
		goalSelector.addGoal(5, new BefriendedFollowOwnerGoal(this, 1.0d, 5.0f, 2.0f, false));
		goalSelector.addGoal(6, new BefriendedWaterAvoidingRandomStrollGoal(this, 1.0d));
		goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new DwmgBefriendedOwnerHurtByTargetGoal(this));
		targetSelector.addGoal(2, new BefriendedHurtByTargetGoal(this));
		targetSelector.addGoal(3, new DwmgBefriendedOwnerHurtTargetGoal(this));
		targetSelector.addGoal(5, new DwmgNearestHostileToSelfTargetGoal(this));
		targetSelector.addGoal(6, new DwmgNearestHostileToOwnerTargetGoal(this));
	}
	
	/* Interaction */

	// Map items that can heal the mob and healing values here.
	// Leave it empty if you don't need healing features.
	@Override
	public HealingItemTable getHealingItems()
	{
		return DwmgHealingItems.SLIME;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand)
	{
		if (player.getUUID().equals(getOwnerUUID())) {
			// For normal interaction
			if (!player.isShiftKeyDown())
			{
				if (!player.level.isClientSide()) 
				{
					/* Put checks before healing item check */
					/* if (....)
					 {
					 	....
					 }
					else */if (this.tryApplyHealingItems(player.getItemInHand(hand)) != InteractionResult.PASS)
						return InteractionResult.sidedSuccess(player.level.isClientSide);
					// The function above returns PASS when the items are not correct. So when not PASS it should stop here
					else if (hand == InteractionHand.MAIN_HAND
							&& DwmgEntityHelper.isOnEitherHand(player, DwmgItems.COMMANDING_WAND.get()))
					{
						switchAIState();
					}
					// Here it's main hand but no interaction. Return pass to enable off hand interaction.
					else return InteractionResult.PASS;
				}
				// Interacted
				return InteractionResult.sidedSuccess(player.level.isClientSide);
			}
			// For interaction with shift key down
			else
			{
				// Open inventory and GUI
				if (hand == InteractionHand.MAIN_HAND && DwmgEntityHelper.isOnEitherHand(player, DwmgItems.COMMANDING_WAND.get()))
				{
					BefriendedHelper.openBefriendedInventory(player, this);
					return InteractionResult.sidedSuccess(player.level.isClientSide);
				}
			}
		} 
		// Always pass when not owning this mob
		return InteractionResult.PASS;
	}
	
	/* Inventory */

	// This enables mob armor and hand items by default.
	// If not needed, use BefriendedInventory class instead.
	protected BefriendedInventory additionalInventory = new BefriendedInventory(getInventorySize(), this);

	@Override
	public BefriendedInventory getAdditionalInventory()
	{
		return additionalInventory;
	}
	
	@Override
	public int getInventorySize()
	{
		return 4;
	}

	@Override
	public void updateFromInventory() {
		if (!this.level.isClientSide) {
		}
	}

	@Override
	public void setInventoryFromMob()
	{
		if (!this.level.isClientSide) {
		}
		return;
	}

	@Override
	public BefriendedInventoryMenu makeMenu(int containerId, Inventory playerInventory, Container container) {
		return new InventoryMenuSlimeGirl(containerId, playerInventory, container, this);
	}

	/* Save and Load */
	
	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		BefriendedHelper.addBefriendedCommonSaveData(this, nbt);
		nbt.putDouble("sg_color_r", this.getColorLinear().r);
		nbt.putDouble("sg_color_g", this.getColorLinear().g);
		nbt.putDouble("sg_color_b", this.getColorLinear().b);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		BefriendedHelper.readBefriendedCommonSaveData(this, nbt);
		this.setColorLinear(LinearColor.fromNormalized(nbt.getDouble("sg_color_r"), nbt.getDouble("sg_color_g"), nbt.getDouble("sg_color_b")));
		setInit();
	}

	@Override
	public HashMap<String, ItemStack> getBaubleSlots() {
		return DwmgBaubleSlotGetter.NO_EQUIPMENT_FOUR_BAUBLES.get(this);
	}

	@Override
	public BaubleHandler getBaubleHandler() {
		return DwmgBaubleHandlers.GENERAL;
	}
	
	// Sound
	
	protected SoundEvent getJumpSound()
	{
		return SoundEvents.SLIME_JUMP;
	}
	
	// Color related
		
	public LinearColor getColorLinear()
	{
		return this.getEntityData().get(DATA_COLOR);
	}
	
	public void setColorLinear(LinearColor color)
	{
		this.getEntityData().set(DATA_COLOR, color);
	}
	
	/**
	 * Invoked on staining with magical gel bottle
	 */
	public void stain(LinearColor gelColor)
	{
		this.setColorLinear(LinearColor.lerp(getColorLinear(), gelColor, 0.2d));
	}
	
	/**
	 * This is for renderer ONLY! On server use {@code getColorLinear} instead.
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	@DontCallManually
	public float[] getColor()
	{
		LinearColor color = getColorLinear();
		float[] res = {(float) color.r, (float) color.g, (float) color.b};
		return res;
	}
	
	// Sounds
	
	@Override
	protected SoundEvent getAmbientSound()
	{
		return DwmgSoundPresets.generalAmbient(super.getAmbientSound());
	}
	
	// Misc
	
	// Indicates which mod this mob belongs to
	@Override
	public String getModId() {
		return Dwmg.MOD_ID;
	}
	
	@Override
	public void remove(RemovalReason reason)
	{
	      this.setRemoved(reason);
	      this.invalidateCaps();
	}

	
	// ==================================================================== //
	// ========================= General Settings ========================= //
	// Generally these can be copy-pasted to other IBefriendedMob classes //

	@Override
	public boolean isPersistenceRequired() {
		return true;
	}

	@Override
	public boolean isPreventingPlayerRest(Player pPlayer) {
		return false;
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return false;
	}

	// ========================= General Settings end ========================= //
	// ======================================================================== //

	protected static class SGLeapAtTargetGoal extends BefriendedLeapAtTargetGoal
	{
		protected final HmagSlimeGirlEntity sg;
		
		public SGLeapAtTargetGoal(HmagSlimeGirlEntity mob)
		{
			super(mob, 0.25F, 0.3F, 6.0F, 6);
			this.sg = mob;
		}
		
		@Override
		public void start()
		{
			super.start();
			this.sg.playSound(sg.getJumpSound(), 1.0F, ((this.sg.getRandom().nextFloat() - this.sg.getRandom().nextFloat()) * 0.2F + 1.0F) * 0.8F);
		}
	}
	
	protected static class SGLeapAtOwnerGoal extends BefriendedLeapAtOwnerGoal
	{
		protected final HmagSlimeGirlEntity sg;
		
		public SGLeapAtOwnerGoal(HmagSlimeGirlEntity mob)
		{
			super(mob, 0.25F, 0.3F, 6.0F, 6);
			this.sg = mob;
		}
		
		@Override
		public void start()
		{
			super.start();
			this.sg.playSound(sg.getJumpSound(), 1.0F, ((this.sg.getRandom().nextFloat() - this.sg.getRandom().nextFloat()) * 0.2F + 1.0F) * 0.8F);
		}
	}
}
