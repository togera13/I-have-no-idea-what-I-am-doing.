package net.sodiumstudio.dwmg.entities.item.baublesystem;

import net.minecraftforge.fml.common.Mod;
import net.sodiumstudio.befriendmobs.item.baublesystem.BaubleHandler;
import net.sodiumstudio.dwmg.Dwmg;
import net.sodiumstudio.dwmg.entities.item.baublesystem.handlers.BaubleHandlerDrowned;
import net.sodiumstudio.dwmg.entities.item.baublesystem.handlers.BaubleHandlerEnderExecutor;
import net.sodiumstudio.dwmg.entities.item.baublesystem.handlers.BaubleHandlerGeneral;
import net.sodiumstudio.dwmg.entities.item.baublesystem.handlers.BaubleHandlerHornet;
import net.sodiumstudio.dwmg.entities.item.baublesystem.handlers.BaubleHandlerVanillaUndead;

@Mod.EventBusSubscriber(modid = Dwmg.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DwmgBaubleHandlers {
	
	public static final BaubleHandler GENERAL = new BaubleHandlerGeneral();
	public static final BaubleHandler VANILLA_UNDEAD = new BaubleHandlerVanillaUndead();
	public static final BaubleHandler ENDER_EXECUTOR = new BaubleHandlerEnderExecutor();
	public static final BaubleHandler DROWNED = new BaubleHandlerDrowned();
	public static final BaubleHandler HORNET = new BaubleHandlerHornet();

}
