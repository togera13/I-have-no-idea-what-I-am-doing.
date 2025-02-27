package net.sodiumstudio.dwmg.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sodiumstudio.dwmg.entities.projectile.NecromancerMagicBulletEntity;
import net.sodiumstudio.dwmg.registries.DwmgItems;

public class ItemNecromancerWand extends Item implements IWithDuration
{

	public ItemNecromancerWand(Properties pProperties)
	{
		super(pProperties);
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand)
	{
		ItemStack stack = player.getItemInHand(usedHand);

		if (getDuration(stack) <= 0)
		{
			return InteractionResultHolder.fail(stack);
		}
		else
		{
			// Movement velocity vector
			Vec3 velocity = player.getLookAngle().scale(16d);
			Vec3 offset = player.getLookAngle().scale(0.2d);
			NecromancerMagicBulletEntity bullet = new NecromancerMagicBulletEntity(level, player, velocity.x, velocity.y, velocity.z);
			bullet.setPos(player.getX() + offset.x , player.getY() + 0.8d + offset.y, player.getZ() + offset.z);
			bullet.setDamage(0);
			if (player.getItemBySlot(EquipmentSlot.HEAD).is(DwmgItems.NECROMANCER_HAT.get()))
				bullet.hasNecromancerHat = true;
			level.addFreshEntity(bullet);
			consumeDuration(stack, 1);
			player.hurt(DamageSource.MAGIC, 2);
			player.getCooldowns().addCooldown(DwmgItems.NECROMANCER_WAND.get(), 50);
			return InteractionResultHolder.success(stack);
		}
	}

	// ===== IWithDuration interface 
	
	@Override
	public int getMaxDuration() {
		return 64;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag)
	{
		super.appendHoverText(stack, level, list, tooltipFlag);
		list.add(getDurationDescription(stack));
	}

	
}
