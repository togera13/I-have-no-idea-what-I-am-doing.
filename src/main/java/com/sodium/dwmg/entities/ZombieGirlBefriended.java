package com.sodium.dwmg.entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

public class ZombieGirlBefriended extends AbstractBefriendedMob {

	protected ZombieGirlBefriended(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
		return null;
	}


}