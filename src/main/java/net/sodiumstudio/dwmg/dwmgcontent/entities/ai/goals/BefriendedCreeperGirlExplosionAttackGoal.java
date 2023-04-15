package net.sodiumstudio.dwmg.dwmgcontent.entities.ai.goals;

import net.minecraft.world.entity.LivingEntity;
import net.sodiumstudio.dwmg.befriendmobs.entity.IBefriendedMob;
import net.sodiumstudio.dwmg.befriendmobs.entity.ai.goal.preset.BefriendedMeleeAttackGoal;
import net.sodiumstudio.dwmg.dwmgcontent.entities.hmag.EntityBefriendedCreeperGirl;

public class BefriendedCreeperGirlExplosionAttackGoal extends BefriendedMeleeAttackGoal
{

	EntityBefriendedCreeperGirl creeper;

	@Override
	public boolean canUse()
	{
		if (!creeper.canAutoBlowEnemy)
			return false;
		if (creeper.getSwellDir() > 0)
			return false;
		if (creeper.blowEnemyCooldown > 0)
			return false;
		if (!creeper.hasEnoughAmmoToExplode())
			return false;
		if (creeper.getTarget() == null)
			return false;
		if (creeper.getOwner() != null && creeper.explodeSafeDistance * creeper.explodeSafeDistance > creeper.getTarget().distanceToSqr(creeper.getOwner()))
			return false;
		return super.canUse();
	}
	
	public BefriendedCreeperGirlExplosionAttackGoal(IBefriendedMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen)
	{
		super(mob, speedModifier, followingTargetEvenIfNotSeen);
		this.creeper = (EntityBefriendedCreeperGirl)mob;
	}

	@Override
	protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
		if (distToEnemySqr < 9.0d && creeper.blowEnemyCooldown == 0)
		{
			this.resetAttackCooldown();
			creeper.setSwellDir(1);
		}
	}
	
}
