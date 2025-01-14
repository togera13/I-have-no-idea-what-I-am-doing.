package net.sodiumstudio.dwmg.entities.projectile;

import java.util.List;

import javax.annotation.Nonnull;

import com.github.mechalopa.hmag.world.entity.projectile.ModProjectileItemEntity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.sodiumstudio.befriendmobs.entity.befriended.IBefriendedMob;
import net.sodiumstudio.dwmg.registries.DwmgEntityTypes;
import net.sodiumstudio.nautils.EntityHelper;

public abstract class BefriendedAlrauneSeedEntity extends ModProjectileItemEntity
{
	
	IBefriendedMob mob;
	
	public BefriendedAlrauneSeedEntity(EntityType<? extends BefriendedAlrauneSeedEntity> type, Level level)
	{
		super(type, level);
	}

	public BefriendedAlrauneSeedEntity(EntityType<? extends BefriendedAlrauneSeedEntity> type, Level level, @Nonnull IBefriendedMob thrower)
	{
		super(type, thrower.asMob(), level);
		this.mob = thrower;
	}

	public BefriendedAlrauneSeedEntity(EntityType<? extends BefriendedAlrauneSeedEntity> type, Level level, double x, double y, double z)
	{
		super(type, x, y, z, level);
	}

	public BefriendedAlrauneSeedEntity(EntityType<? extends BefriendedAlrauneSeedEntity> type, PlayMessages.SpawnEntity spawnEntity, Level level)
	{
		this(type, level);
	}

	@Override
	protected Item getDefaultItem()
	{
		return Items.BEETROOT_SEEDS;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void handleEntityEvent(byte id)
	{
		if (id == 3)
		{
			for (int i = 0; i < 8; ++i)
			{
				this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), (this.random.nextFloat() - 0.5D) * 0.08D, (this.random.nextFloat() - 0.5D) * 0.08D, (this.random.nextFloat() - 0.5D) * 0.08D);
			}
		}
	}

	@Override
	protected void onHitServer(HitResult result)
	{
		AABB aabb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
		List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, aabb);

		if (!list.isEmpty())
		{
			for (LivingEntity livingEntity : list)
			{
				if (livingEntity.isAffectedByPotions() && canAffect(livingEntity))
				{
					double d0 = this.distanceToSqr(livingEntity);

					if (d0 < 16.0D)
					{
						double d1 = 1.0D - Math.sqrt(d0) / 4.0D;
						int i = getMaxEffectSeconds() * 20;
						i = (int)(d1 * i + 0.5D);
						if (i > 20)
						{
							applyEffects(livingEntity, i);
						}
					}
				}
			}
		}

		this.level.levelEvent(2002, this.blockPosition(), getPotionColor());
		this.level.broadcastEntityEvent(this, (byte)3);
		super.onHitServer(result);
	}

	protected abstract boolean canAffect(LivingEntity living);
	/*{
		return true;
	}*/
	
	protected int getMaxEffectSeconds()
	{
		return 15;
	}
	
	protected abstract void applyEffects(LivingEntity target, int ticks);
	/*{
		target.addEffect(new MobEffectInstance(MobEffects.POISON, ticks, 0));
		target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 0));
		target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 0));
	}*/
	
	protected abstract int getPotionColor();
	
	@Override
	protected void onHitEntity(EntityHitResult result)
	{
		super.onHitEntity(result);
		if (hasDamage())
			result.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), this.getDamage());
	}
	
	protected boolean hasDamage()
	{
		return true;
	}
	
	public static class PoisonSeed extends BefriendedAlrauneSeedEntity
	{

		public PoisonSeed(EntityType<? extends PoisonSeed> type, Level level)
		{
			super(type, level);
		}
		
		public PoisonSeed(Level level, IBefriendedMob thrower)
		{
			super(DwmgEntityTypes.ALRAUNE_POISON_SEED.get(), level, thrower);
		}

		public PoisonSeed(Level level, double x, double y, double z)
		{
			super(DwmgEntityTypes.ALRAUNE_POISON_SEED.get(), level, x, y, z);
		}

		public PoisonSeed(SpawnEntity spawnEntity, Level level)
		{
			super(DwmgEntityTypes.ALRAUNE_POISON_SEED.get(), level);
		}
		
		@Override
		protected boolean canAffect(LivingEntity living) {
			
			Player player = null;	// Related player
			if (living instanceof Player)
				player = (Player)living;
			else if (living instanceof TamableAnimal ta && ta.getOwnerUUID() != null)
			{
				// If has the same owner, block
				if (ta.getOwnerUUID().equals(mob.getOwnerUUID()))
					return false;
				if (ta.getOwner() instanceof Player)
					player = (Player)(ta.getOwner());
			}
			else if (living instanceof IBefriendedMob bm)
				return false;
			
			if (player == null)
				return true;
			else if (player.getUUID().equals(mob.getOwnerUUID()))
				return false;
			else if (mob.isOwnerPresent() && mob.getOwner().canHarmPlayer(player))
				return true;
			else return false;
			
		}

		@Override
		protected void applyEffects(LivingEntity target, int ticks) {
			target.addEffect(new MobEffectInstance(MobEffects.POISON, ticks, 0));
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 0));
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 0));
		}

		@Override
		protected int getPotionColor() {
			return MobEffects.POISON.getColor();
		}
	}
	
	public static class HealingSeed extends BefriendedAlrauneSeedEntity
	{
		
		public HealingSeed(EntityType<? extends HealingSeed> type, Level level)
		{
			super(type, level);
		}
		
		public HealingSeed(Level level, IBefriendedMob thrower)
		{
			super(DwmgEntityTypes.ALRAUNE_HEALING_SEED.get(), level, thrower);
		}

		public HealingSeed(Level level, double x, double y, double z)
		{
			super(DwmgEntityTypes.ALRAUNE_HEALING_SEED.get(), level, x, y, z);
		}

		public HealingSeed(SpawnEntity spawnEntity, Level level)
		{
			super(DwmgEntityTypes.ALRAUNE_HEALING_SEED.get(), level);
		}

		@Override
		protected boolean canAffect(LivingEntity living) {
			if (living.getUUID().equals(mob.getOwnerUUID()))
				return true;
			else if (living instanceof TamableAnimal ta 
					&& ta.getOwnerUUID() != null
					&& mob.getOwnerUUID().equals(ta.getOwnerUUID()))
				return true;
			else if (living instanceof IBefriendedMob bm
					&& bm.getOwnerUUID().equals(mob.getOwnerUUID()))
				return true;
			else return false;
		}

		public static boolean canAffect(IBefriendedMob shooter, LivingEntity living)
		{
			if (living.getUUID().equals(shooter.getOwnerUUID()))
				return true;
			else if (living instanceof TamableAnimal ta 
					&& ta.getOwnerUUID() != null
					&& shooter.getOwnerUUID().equals(ta.getOwnerUUID()))
				return true;
			else if (living instanceof IBefriendedMob bm
					&& bm.getOwnerUUID().equals(shooter.getOwnerUUID()))
				return true;
			else return false;
		}
		
		@Override
		protected int getMaxEffectSeconds()
		{
			return 5;
		}
		
		@Override
		protected void applyEffects(LivingEntity target, int ticks) {
			target.heal(1);
			EntityHelper.addEffectSafe(target, new MobEffectInstance(MobEffects.REGENERATION, ticks, 0));
		}

		@Override
		protected int getPotionColor() {
			return MobEffects.REGENERATION.getColor();
		}
		
		@Override
		protected boolean hasDamage()
		{
			return false;
		}
		
	}
}
