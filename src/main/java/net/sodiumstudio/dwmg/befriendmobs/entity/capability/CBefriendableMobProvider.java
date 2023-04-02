package net.sodiumstudio.dwmg.befriendmobs.entity.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.sodiumstudio.dwmg.befriendmobs.registry.BefMobCapabilities;

public class CBefriendableMobProvider implements ICapabilitySerializable<CompoundTag> {

	protected CBefriendableMob capability = new CBefriendableMobImpl();
		
	public CBefriendableMobProvider(Mob owner)
	{
		((CBefriendableMobImpl)capability).setOwner(owner);
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		return capability != null ? capability.serializeNBT() : new CompoundTag();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) 
	{
		capability.deserializeNBT(nbt);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if(cap == BefMobCapabilities.CAP_BEFRIENDABLE_MOB)
			return LazyOptional.of(() -> {return this.capability;}).cast();
		else
			return LazyOptional.empty();
	}

}