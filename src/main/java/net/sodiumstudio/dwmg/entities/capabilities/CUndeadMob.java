package net.sodiumstudio.dwmg.entities.capabilities;

import java.util.UUID;
import java.util.Vector;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

public interface CUndeadMob extends INBTSerializable<CompoundTag>{

	// Get this mob's hatred list
	Vector<UUID> getHatred();
	
	// Add a player to the hatred list
	// This action is permanent and there's no handler to remove a player
	void addHatred(LivingEntity entity);
	
}