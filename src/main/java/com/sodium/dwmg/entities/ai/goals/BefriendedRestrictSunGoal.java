package com.sodium.dwmg.entities.ai.goals;

import com.sodium.dwmg.entities.IBefriendedMob;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;

// Adjusted from vanilla RestrictSunGoal
public class BefriendedRestrictSunGoal extends BefriendedGoal {

	// If true, the mob will restrict sun although having a helmet.
	public boolean ignoreHelmet = false;

	public BefriendedRestrictSunGoal(IBefriendedMob pMob) {
		mob = pMob;
		allowAllStates();
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state
	 * necessary for execution in this method as well.
	 */
	public boolean canUse() {
		return !isDisabled() && getPathfinder().level.isDay() && 
				ignoreHelmet ? true	: getPathfinder().getItemBySlot(EquipmentSlot.HEAD).isEmpty() && 
						GoalUtils.hasGroundPathNavigation(getPathfinder());
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		((GroundPathNavigation) getPathfinder().getNavigation()).setAvoidSun(true);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by
	 * another one
	 */
	public void stop() {
		if (GoalUtils.hasGroundPathNavigation(getPathfinder())) {
			((GroundPathNavigation) getPathfinder().getNavigation()).setAvoidSun(false);
		}

	}

}
