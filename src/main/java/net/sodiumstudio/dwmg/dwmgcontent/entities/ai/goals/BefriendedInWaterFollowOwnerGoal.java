package net.sodiumstudio.dwmg.dwmgcontent.entities.ai.goals;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.sodiumstudio.dwmg.befriendmobs.entity.IBefriendedMob;
import net.sodiumstudio.dwmg.befriendmobs.entity.ai.goal.preset.move.BefriendedFollowOwnerGoal;

/* Adjusted from vanilla FollowOwnerGoal for TameableAnimal */
public class BefriendedInWaterFollowOwnerGoal extends BefriendedFollowOwnerGoal {

	public static final int TELEPORT_WHEN_DISTANCE_IS = 12;
	protected static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
	protected static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
	protected static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
	protected final LevelReader level;
	protected final double speedModifier;
	protected final PathNavigation navigation;
	protected int timeToRecalcPath;
	protected final float stopDistance;
	protected final float startDistance;
	protected float oldWaterCost;
	protected final boolean canFly;

	public BefriendedInWaterFollowOwnerGoal(@Nonnull IBefriendedMob inMob, double pSpeedModifier, float pStartDistance,
			float pStopDistance) {
		super(inMob, pSpeedModifier, pStartDistance, pStartDistance, false);
		mob = inMob;
		this.level = mob.asMob().level;
		this.speedModifier = pSpeedModifier;
		this.navigation = getPathfinder().getNavigation();
		this.startDistance = pStartDistance;
		this.stopDistance = pStopDistance;
		this.canFly = false;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));

		allowState(FOLLOW);
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state
	 * necessary for execution in this handler as well.
	 */
	@Override
	public boolean canUse() {
		if (isDisabled())
			return false;
		if (!mob.asMob().isInWater() && !mob.getOwner().isInWater())
			return false;
		return super.canUse();
		}
	

}
