package net.sodiumstudio.dwmg.entities.ai.goals;

import java.util.EnumSet;

import com.github.mechalopa.hmag.world.entity.AbstractFlyingMonsterEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.sodiumstudio.befriendmobs.entity.befriended.IBefriendedMob;
import net.sodiumstudio.befriendmobs.entity.ai.BefriendedAIState;
import net.sodiumstudio.befriendmobs.entity.ai.goal.BefriendedGoal;
import net.sodiumstudio.befriendmobs.entity.ai.goal.BefriendedMoveGoal;
import net.sodiumstudio.befriendmobs.entity.ai.goal.BefriendedTargetGoal;
import net.sodiumstudio.nautils.LevelHelper;
import net.sodiumstudio.nautils.math.GeometryUtil;
import net.sodiumstudio.dwmg.befriendmobs.entity.ai.goal.preset.move.IBefriendedFollowOwner;
import net.sodiumstudio.dwmg.entities.capabilities.CFavorabilityHandler;

/* Ported from HMaG-AbstractFlyingMonsterEntity (Mechalopa)
 */
public interface HmagFlyingGoal
{
	
	public default AbstractFlyingMonsterEntity getFlying()
	{
		if (this instanceof BefriendedGoal g)
			return (AbstractFlyingMonsterEntity)(g.getMob());
		else if (this instanceof BefriendedTargetGoal g)
			return (AbstractFlyingMonsterEntity)(g.getMob());
		throw new UnsupportedOperationException("HmagFlyingGoal is only for BefriendedGoal and BefriendedTargetGoal.");
	}
	
	public static class ChargeAttackGoal extends BefriendedGoal implements HmagFlyingGoal
	{
		protected final double moveSpeed;
		protected final float attackRadius;
		protected final int chance;
		protected int attackTime;
		public double forceFollowDistance = 9d;

		public ChargeAttackGoal(IBefriendedMob mob)
		{
			this(mob, 0.3D, 2.0F);
		}

		public ChargeAttackGoal(IBefriendedMob mob, double moveSpeed, float maxAttackDistance)
		{
			this(mob, moveSpeed, maxAttackDistance, 4);
		}

		public ChargeAttackGoal(IBefriendedMob mob, double moveSpeed, float maxAttackDistance, int chance)
		{
			super(mob);
			this.moveSpeed = moveSpeed;
			this.attackRadius = maxAttackDistance;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
			this.allowAllStatesExceptWait();
			this.chance = chance;
		}
		
		@Override
		public boolean checkCanUse()
		{
			if (isDisabled())
				return false;
			if (mob.isOwnerPresent() && mob.getAIState() == BefriendedAIState.FOLLOW && mob.asMob().distanceToSqr(mob.getOwner()) > forceFollowDistance * forceFollowDistance)
				return false;
			if (getFlying().getTarget() != null 
					&& !getFlying().getMoveControl().hasWanted() 
					&& getFlying().getRandom().nextInt(this.chance) == 0
					)
			{
				return getFlying().distanceToSqr(getFlying().getTarget()) > this.attackRadius;
			}
			else
			{
				return false;
			}
		}

		@Override
		public boolean checkCanContinueToUse()
		{
			if (isDisabled())
				return false;
			return getFlying().getMoveControl().hasWanted() 
					&& getFlying().isCharging() 
					&& getFlying().getTarget() != null 
					&& getFlying().getTarget().isAlive();
		}

		@Override
		public void start()
		{
			LivingEntity livingentity = getFlying().getTarget();

			if (getFlying().hasLineOfSight(livingentity) || getFlying().getAttackPhase() != 0)
			{
				Vec3 vec3 = livingentity.position();
				getFlying().getMoveControl().setWantedPosition(vec3.x, vec3.y - 1.5D, vec3.z, this.moveSpeed);
				getFlying().setAttackPhase(2);
			}
		}

		@Override
		public void stop()
		{
			getFlying().setAttackPhase((getFlying().getTarget() != null && getFlying().getTarget().isAlive()) ? 1 : 0);
		}

		@Override
		public void tick()
		{
			AbstractFlyingMonsterEntity attacker = getFlying();
			LivingEntity livingentity = attacker.getTarget();

			this.attackTime = Math.max(this.attackTime - 1, 0);
			attacker.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
			double d0 = GeometryUtil.getBoxSurfaceDistSqr(attacker.getBoundingBox(), livingentity.getBoundingBox());
			double d1 = this.getAttackMaxSurfaceDistSqr(livingentity);

			if (d0 <= d1 && this.attackTime <= 0)
			{
				this.attackTime = 20;
				attacker.swing(InteractionHand.MAIN_HAND);
				attacker.doHurtTarget(livingentity);
				attacker.setAttackPhase(1);
			}
			else
			{
				if (attacker.hasLineOfSight(livingentity))
				{
					if (d0 < this.attackRadius + 15.0F)
					{
						Vec3 vec3 = livingentity.getEyePosition();
						attacker.getMoveControl().setWantedPosition(vec3.x, vec3.y - 0.75D, vec3.z, this.moveSpeed);
						/*
						Vec3 attackerPos = attacker.position();
						Vec3 targetPos = livingentity.position();
						Vec3 offset = attackerPos.subtract(targetPos);
						offset = new Vec3(offset.x, 0, offset.z);	// Project to xz plane;
						offset = offset.normalize().scale(livingentity.getBbWidth() / 2 + 0.2d);
						Vec3 actualPos = targetPos.add(offset);
						attacker.getMoveControl().setWantedPosition(actualPos.x, actualPos.y, actualPos.z, this.moveSpeed);*/
					}
				}
				else if (attacker.getRandom().nextInt(16) == 0)
				{
					attacker.setAttackPhase(0);
				}
			}
		}

		@Deprecated
		protected double getAttackReachSqr(LivingEntity attackTarget)
		{
			return getFlying().getBbWidth() * 2.0F * getFlying().getBbWidth() * 2.0F + attackTarget.getBbWidth();
		}
		
		protected double getAttackMaxSurfaceDistSqr(LivingEntity target)
		{
			return 0.25d;
		}
	}

	public static class MoveRandomGoal extends BefriendedMoveGoal implements HmagFlyingGoal
	{
		protected final double moveSpeed;
		protected final int chance;
		protected final int width;
		protected final int height;
		protected int heightLimit = -1;
		
		public MoveRandomGoal(IBefriendedMob mob)
		{
			this(mob, 0.25D);
		}

		public MoveRandomGoal(IBefriendedMob mob, double moveSpeed)
		{
			this(mob, moveSpeed, 6, 3, 2);
		}

		public MoveRandomGoal(IBefriendedMob mob, double moveSpeed, int chance, int width, int height)
		{
			super(mob);
			this.moveSpeed = moveSpeed;
			this.chance = chance;
			this.width = width;
			this.height = height;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
			//this.allowAllStatesExceptWait();
			this.allowState(BefriendedAIState.WANDER);
		}

		public MoveRandomGoal heightLimit(int value)
		{
			heightLimit = value;
			return this;
		}
		
		@Override
		public boolean checkCanUse()
		{
			if (isDisabled())
				return false;		
			return !getFlying().getMoveControl().hasWanted() && getFlying().getRandom().nextInt(this.chance) == 0;
		}

		@Override
		public boolean checkCanContinueToUse()
		{
			return false;
		}

		@Override
		public void start()
		{
			if (!(getFlying().getTarget() != null && getFlying().getTarget().isAlive()))
			{
				getFlying().setAttackPhase(0);
			}
		}

		
		protected BlockPos getWantedPosition()
		{
			BlockPos blockpos = getFlying().blockPosition();
			BlockPos blockpos1 = blockpos.offset(
					getFlying().getRandom().nextInt(this.width * 2 + 1) - this.width,
					getFlying().getRandom().nextInt(this.height * 2 + 1) - this.height,
					getFlying().getRandom().nextInt(this.width * 2 + 1) - this.width);
			if (heightLimit <= 0)
				return blockpos1;
			// No height limit if it's above the void
			else if (LevelHelper.getHeightToGround(blockpos1, getFlying()) == -1)
				return blockpos1;
			else if (LevelHelper.getHeightToGround(blockpos1, getFlying()) > heightLimit)
			{
				// If it's already too high, fly to the height limit first
				int it = 32;
				while (LevelHelper.getHeightToGround(blockpos1, getFlying()) > heightLimit)
				{
					blockpos1 = blockpos1.below();
					it--;
					if (it <= 0)
						break;
				}
				// Case when it didn't find a position, try flying down
				if (it <= 0)
				{
					blockpos1 = new BlockPos(blockpos);
					while (LevelHelper.getHeightToGround(blockpos1, getFlying()) > heightLimit)
						blockpos1 = blockpos1.below();
				}
				return blockpos1;
			}
			else
			{
				int it = 32;	// To avoid potential infinite loop 				
				while (LevelHelper.getHeightToGround(blockpos1, getFlying()) > heightLimit)
				{
					// Search until an acceptable
					blockpos1 = blockpos.offset(
							getFlying().getRandom().nextInt(this.width * 2 + 1) - this.width,
							getFlying().getRandom().nextInt(this.height * 2 + 1) - this.height,
							getFlying().getRandom().nextInt(this.width * 2 + 1) - this.width);
					it--;
					if (it <= 0)
						break;
				}
				// If failed, find below to get an acceptable position
				while (LevelHelper.getHeightToGround(blockpos1, getFlying()) > heightLimit)
					blockpos1 = blockpos1.below();
				return blockpos1;
			}

		}
		
		@Override
		public void tick()
		{
			AbstractFlyingMonsterEntity flyingentity = getFlying();
			BlockPos blockpos = flyingentity.getBoundOrigin();

			if (blockpos == null)
			{
				blockpos = flyingentity.blockPosition();
			}

			for (int i = 0; i < 6; ++i)
			{
				BlockPos blockpos1 = getWantedPosition();
				
				if (shouldAvoidSun.test(mob) && LevelHelper.isUnderSun(blockpos1, mob.asMob()))
					continue;
				
				if (flyingentity.level.isEmptyBlock(blockpos1))
				{
					flyingentity.getMoveControl().setWantedPosition(blockpos1.getX() + 0.5D, blockpos1.getY() + 0.5D, blockpos1.getZ() + 0.5D, this.moveSpeed);

					if (flyingentity.getTarget() == null)
					{
						flyingentity.getLookControl().setLookAt(blockpos1.getX() + 0.5D, blockpos1.getY() + 0.5D, blockpos1.getZ() + 0.5D, 180.0F, 20.0F);
					}

					break;
				}
			}
		}	
	}
	
	public static class FollowOwnerGoal extends MoveRandomGoal implements HmagFlyingGoal, IBefriendedFollowOwner
	{
		public double teleportDistance = 12d;
		public double noFollowOnCombatDistance = 6d;
		public double minStartDistance = 4d;
		
		public FollowOwnerGoal(IBefriendedMob mob, double moveSpeed, int width, int height)
		{
			super(mob, moveSpeed, 1, width, height);
			this.disallowAllStates();
			this.allowState(BefriendedAIState.FOLLOW);
		}

		public FollowOwnerGoal(IBefriendedMob mob, double moveSpeed)
		{
			this(mob, moveSpeed, 3, 2);
		}

		public FollowOwnerGoal(IBefriendedMob mob)
		{
			this(mob, 0.25D);
		}
		
		@Override
		public boolean checkCanUse()
		{
			if (getFlying().getMoveControl().hasWanted())
				return false;
			if (!mob.isOwnerPresent())
				return false;
			if (mob.asMob().getTarget() != null && mob.asMob().distanceToSqr(mob.getOwner()) < noFollowOnCombatDistance * noFollowOnCombatDistance)
				return false;
			if (mob.asMob().distanceToSqr(mob.getOwner()) < minStartDistance * minStartDistance)
				return false;
			if (CFavorabilityHandler.isLowFavorability(mob.asMob()))
				return false;
			else return true;
		}
		
		@Override
		public void tick() {
			if (!mob.isOwnerPresent())
				return;	// Prevent potential nullptr crash
			goToOwnerPreset(moveSpeed);
		}	
		
		@Override
		public void moveToOwner(double param)
		{
			if (!goal().getMob().isOwnerPresent())
				return;
			Mob mob = goal().getMob().asMob();
			Player owner = goal().getMob().getOwner();
			Vec3 pos = owner.getEyePosition();
			mob.getMoveControl().setWantedPosition(pos.x, pos.y, pos.z, param);
		}
	}
	

	
}
