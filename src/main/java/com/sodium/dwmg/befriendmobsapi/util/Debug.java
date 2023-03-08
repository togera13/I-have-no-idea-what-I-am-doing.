package com.sodium.dwmg.befriendmobsapi.util;

import com.sodium.dwmg.befriendmobsapi.BefriendMobsAPI;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

// All debug output should work only in development.
public class Debug {
	
	// param receiver should be player
	public static void printToScreen(String text, Player receiver, Entity sender)
	{
		if(BefriendMobsAPI.IS_DEBUG_MODE)			
			receiver.sendMessage(new TextComponent(text), sender.getUUID());
	}
	
	public static void printToScreen(String text, Player receiver)
	{		
		printToScreen(text, receiver, receiver);
	}
}
