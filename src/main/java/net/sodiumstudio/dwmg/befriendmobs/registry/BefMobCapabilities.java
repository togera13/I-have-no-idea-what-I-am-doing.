package net.sodiumstudio.dwmg.befriendmobs.registry;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.sodiumstudio.dwmg.befriendmobs.entitiy.capability.CBefriendableMob;

public class BefMobCapabilities {

	public static Capability<CBefriendableMob> CAP_BEFRIENDABLE_MOB = CapabilityManager.get(new CapabilityToken<>(){});

	@SubscribeEvent
	public static void register(RegisterCapabilitiesEvent event)
	{
		// Entities
		event.register(CBefriendableMob.class);
	}
}
