package net.sodiumstudio.dwmg.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.sodiumstudio.befriendmobs.entity.befriended.IBefriendedMob;
import net.sodiumstudio.befriendmobs.entity.capability.HealingItemTable;
import net.sodiumstudio.befriendmobs.entity.capability.wrapper.IAttributeMonitor;
import net.sodiumstudio.befriendmobs.item.MobRespawnerItem;
import net.sodiumstudio.befriendmobs.item.baublesystem.IBaubleEquipable;
import net.sodiumstudio.befriendmobs.item.capability.wrapper.IItemStackMonitor;
import net.sodiumstudio.nautils.Wrapped;
import net.sodiumstudio.nautils.annotation.DontCallManually;
import net.sodiumstudio.nautils.annotation.DontOverride;
import net.sodiumstudio.befriendmobs.entity.capability.HealingItemTable;
import net.sodiumstudio.nautils.object.ItemOrKey;
import net.sodiumstudio.dwmg.entities.capabilities.CFavorabilityHandler;
import net.sodiumstudio.dwmg.entities.capabilities.CLevelHandler;
import net.sodiumstudio.dwmg.entities.item.baublesystem.DwmgBaubleItem;
import net.sodiumstudio.dwmg.registries.DwmgCapabilities;
import net.sodiumstudio.dwmg.registries.DwmgItems;

public interface IDwmgBefriendedMob extends IBefriendedMob, IBaubleEquipable, IAttributeMonitor, IItemStackMonitor
{
	
	@DontOverride
	public default CFavorabilityHandler getFavorability()
	{
		Wrapped<CFavorabilityHandler> cap = new Wrapped<CFavorabilityHandler>(null);
		asMob().getCapability(DwmgCapabilities.CAP_FAVORABILITY_HANDLER).ifPresent((c) -> 
		{
			cap.set(c);
		});
		if (cap.get() == null)
			throw new IllegalStateException("Missing CFavorabilityHandler capability");
		return cap.get();
	}
	
	@DontOverride
	public default CLevelHandler getLevelHandler()
	{
		Wrapped<CLevelHandler> cap = new Wrapped<CLevelHandler>(null);
		asMob().getCapability(DwmgCapabilities.CAP_LEVEL_HANDLER).ifPresent((c) -> 
		{
			cap.set(c);
		});
		if (cap.get() == null)
			throw new IllegalStateException("Missing CLevelHandler capability");
		return cap.get();
	}

	@DontOverride
	@DontCallManually
	public default void touchEntity(Entity other)
	{
		MinecraftForge.EVENT_BUS.post(new OverlapEntityEvent(this, other));
	}
	
	@Override
	public default double getAnchoredStrollRadius()  
	{
		return 16.0d;
	}
	
	/**
	 * Fired EVERY TICK when a befriended mob overlaps an entity.
	 */
	public static class OverlapEntityEvent extends Event
	{
		public final IBefriendedMob thisMob;
		public final Entity touchedEntity;
		public OverlapEntityEvent(IBefriendedMob thisMob, Entity touchedEntity)
		{
			this.thisMob = thisMob;
			this.touchedEntity = touchedEntity;
		}
	}

	/* Bauble related */
	public default boolean hasDwmgBauble(String typeName)
	{
		for (ItemStack stack: this.getBaubleSlots().values())
		{
			if (!stack.isEmpty() && stack.getItem() instanceof DwmgBaubleItem bauble && bauble.is(typeName))
				return true;
		}
		return false;
	}
	
	public default boolean hasDwmgBaubleWithLevel(String typeName, int level)
	{
		for (ItemStack stack: this.getBaubleSlots().values())
		{
			if (!stack.isEmpty() && stack.getItem() instanceof DwmgBaubleItem bauble && bauble.is(typeName, level))
				return true;
		}
		return false;
	}
	
	public default boolean hasDwmgBaubleWithMinLevel(String typeName, int minLevel)
	{
		for (ItemStack stack: this.getBaubleSlots().values())
		{
			if (!stack.isEmpty() 
				&& stack.getItem() instanceof DwmgBaubleItem bauble 
				&& bauble.is(typeName)
				&& bauble.getLevel() >= minLevel)
			{
				return true;
			}
		}
		return false;
	}
	
	// === IBefriendedMob interface
	@Override
	public default MobRespawnerItem getRespawnerType()
	{
		return DwmgItems.MOB_RESPAWNER.get();
	}
	
	@Override
	public default DeathRespawnerGenerationType getDeathRespawnerGenerationType()
	{
		return DeathRespawnerGenerationType.GIVE;
	}
	
	// === IAttributeMonitor interface
	
	@Override
	public default void onAttributeChange(Attribute attr, double oldVal, double newVal) 
	{
		if (attr == Attributes.MAX_HEALTH)
		{
			this.asMob().setHealth((float) (this.asMob().getHealth() * newVal / oldVal));
		}
	}

	@Override
	public HealingItemTable getHealingItems();
	
	// === IItemStackMonitor interface
	
	private static UUID getSharpnessModifierUUID()
	{
		return UUID.fromString("9c12b503-63c0-43e6-bd30-d7aae9818c99");
	}
	
	@Override
	public default void onItemStackChange(String key, ItemStack oldStack, ItemStack newStack) {
		if (key.equals("main_hand"))
		{
			this.asMob().getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(getSharpnessModifierUUID());
			@SuppressWarnings("deprecation")
			int lv = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, newStack);
			if (lv > 0)
			{
				this.asMob().getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new AttributeModifier(
						getSharpnessModifierUUID(), "sharpness_modifier", 0.5d + 0.5d * (double) lv, AttributeModifier.Operation.ADDITION));
			}
		}
	}
	
	// ===== Util ===
	
	public default HashMap<String, ItemStack> continuousBaubleSlots(int startIndex, int endIndexExclude)
	{
		HashMap<String, ItemStack> map = new HashMap<>();
		int j = 0;
		for (int i = startIndex; i < endIndexExclude; ++i)
		{
			map.put(Integer.toString(j), this.getAdditionalInventory().getItem(i));
			j++;
		}
		return map;
	}
	
}
