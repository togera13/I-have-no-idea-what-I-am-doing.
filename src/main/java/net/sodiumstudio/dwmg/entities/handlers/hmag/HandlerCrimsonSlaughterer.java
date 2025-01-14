package net.sodiumstudio.dwmg.entities.handlers.hmag;

import java.util.HashSet;
import java.util.UUID;

import com.github.mechalopa.hmag.world.entity.CrimsonSlaughtererEntity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.sodiumstudio.befriendmobs.entity.befriending.BefriendableAddHatredReason;
import net.sodiumstudio.befriendmobs.entity.befriending.handlerpreset.HandlerItemGivingProgress;
import net.sodiumstudio.dwmg.registries.DwmgTags;
import net.sodiumstudio.nautils.EntityHelper;
import net.sodiumstudio.nautils.TagHelper;
import net.sodiumstudio.nautils.math.RandomSelection;
import net.sodiumstudio.nautils.math.RndUtil;

public class HandlerCrimsonSlaughterer extends HandlerItemGivingProgress
{
	protected static final UUID WARPED_BLOCK_KNOCKBACK_UUID = UUID.fromString("e934d764-7e28-4dc7-a652-a156ac4ce44d");
	protected static final AttributeModifier WARPED_BLOCK_KNOCKBACK = new AttributeModifier(WARPED_BLOCK_KNOCKBACK_UUID, "warped_block_knockback",
			2.0d, AttributeModifier.Operation.ADDITION);
	
	@Override
	protected double getProcValueToAdd(ItemStack item, Player player, Mob mob, double oldProc) {
		if (item.is(Items.CRIMSON_ROOTS))
			return RndUtil.rndRangedDouble(0.02d, 0.04d);
		else if (item.is(Items.WEEPING_VINES))
			return RndUtil.rndRangedDouble(0.02d, 0.04d);
		else if (item.is(Items.NETHER_WART))
			return RndUtil.rndRangedDouble(0.03d, 0.06d);
		else if (item.is(Items.CRIMSON_FUNGUS))
			return RndUtil.rndRangedDouble(0.03d, 0.06d);
		else if (item.is(Items.SHROOMLIGHT))
			return RndUtil.rndRangedDouble(0.04d, 0.08d);
		else if (item.is(Items.GILDED_BLACKSTONE))
			return RndUtil.rndRangedDouble(0.04d, 0.08d);
		else if (item.is(Items.GOLDEN_APPLE))
			return RndUtil.rndRangedDouble(0.08d, 0.16d);
		else if (TagHelper.hasTag(item, "forge:ingots/netherite") || item.is(Items.NETHERITE_INGOT))
			return RndUtil.rndRangedDouble(0.16d, 0.32d);
		else if (item.is(Items.NETHER_STAR))
			return RandomSelection.createDouble(0.50d).add(1.01d, 0.20d).getDouble();
		else return 0;
			
	}

	@Override
	public boolean isItemAcceptable(ItemStack item) {
		if (item.is(Items.CRIMSON_ROOTS)
			|| item.is(Items.WEEPING_VINES)
			|| item.is(Items.NETHER_WART)
			|| item.is(Items.SHROOMLIGHT)
			|| item.is(Items.CRIMSON_FUNGUS)
			|| item.is(Items.GILDED_BLACKSTONE)
			|| item.is(Items.GOLDEN_APPLE)
			|| TagHelper.hasTag(item, "forge:ingots/netherite") || item.is(Items.NETHERITE_INGOT)
			|| item.is(Items.NETHER_STAR))
			return true;
		else return false;
	}

	@Override
	public boolean additionalConditions(Player player, Mob mob) {
		return isOnWarpedBlock(mob) && satisfiesShroomlightCondition(mob);
	}

	@Override
	public int getItemGivingCooldownTicks() {
		return 10 * 20;
	}

	@Override
	public int getHatredDurationTicks(BefriendableAddHatredReason reason)
	{
		switch (reason)
		{
		case ATTACKED:
			return 300 * 20;
		default:
			return 0;				
		}
	}
	
	@Override
	public HashSet<BefriendableAddHatredReason> getAddHatredReasons() {
		HashSet<BefriendableAddHatredReason> set = new HashSet<BefriendableAddHatredReason>();
		set.add(BefriendableAddHatredReason.ATTACKED);
		return set;
	}

	@Override
	public void serverTick(Mob mob)
	{
		super.serverTick(mob);
		if (mob instanceof CrimsonSlaughtererEntity cs)
		{
			if (isOnWarpedBlock(mob))
			{
				EntityHelper.addEffectSafe(mob, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10));
				if (!mob.getAttribute(Attributes.ATTACK_KNOCKBACK).hasModifier(WARPED_BLOCK_KNOCKBACK))
					mob.getAttribute(Attributes.ATTACK_KNOCKBACK).addTransientModifier(WARPED_BLOCK_KNOCKBACK);
			}
			else
			{
				mob.getAttribute(Attributes.ATTACK_KNOCKBACK).removeModifier(WARPED_BLOCK_KNOCKBACK);
			}
		}
		
		if (!satisfiesShroomlightCondition(mob) && this.isInProcess(mob))
		{
			this.forAllPlayersInProcess(mob, player -> {
				this.addProgressValue(mob, player, -0.005);	// 0.1 per second
				if (this.getProgressValue(mob, player) <= 0)
				{
					interrupt(player, mob, false);
				}
			});
			EntityHelper.sendSmokeParticlesToLivingDefault(mob);
		}
	}
	
	public boolean isOnWarpedBlock(Mob mob)
	{
		return mob.level.getBlockState(mob.blockPosition().below()).is(DwmgTags.AFFECTS_CRIMSON_SLAUGHTERER);
	}
	
	public boolean satisfiesShroomlightCondition(Mob mob)
	{
		return mob.level.getBlockStates(EntityHelper.getNeighboringArea(mob, 6, 3)).filter(bs -> bs.is(Blocks.SHROOMLIGHT))
				.toList().size() >= 16;
	}

	
}
