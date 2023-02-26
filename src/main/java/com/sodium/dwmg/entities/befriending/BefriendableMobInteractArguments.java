package com.sodium.dwmg.entities.befriending;

import javax.annotation.Nonnull;

import com.sodium.dwmg.entities.capabilities.ICapBefriendableMob;
import com.sodium.dwmg.registries.ModCapabilities;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.fml.LogicalSide;

public class BefriendableMobInteractArguments {

	private LogicalSide side;
	private Player player;
	private LivingEntity target;
	private InteractionHand hand;
	
	private BefriendableMobInteractArguments()
	{
		side = LogicalSide.SERVER;
		player = null;
		target = null;
		hand = null;
	}
	
	public static BefriendableMobInteractArguments of(LogicalSide side, @Nonnull Player player, @Nonnull LivingEntity target, @Nonnull InteractionHand hand)
	{
		BefriendableMobInteractArguments res = new BefriendableMobInteractArguments();
		res.side = side;
		res.player = player;
		res.target = target;
		res.hand = hand;
		if (!target.getCapability(ModCapabilities.CAP_BEFRIENDABLE_MOB).isPresent())
			throw new IllegalStateException("BefriendableMobInteraction event: target is not befriendable.");
		return res;
	}
	
	public LogicalSide getSide()
	{
		return side;
	}
	
	public boolean isClient()
	{
		return side == LogicalSide.CLIENT;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public LivingEntity getTarget()
	{
		return target;
	}
	
	public InteractionHand getHand()
	{
		return hand;
	}
	
	public boolean isMainHand()
	{
		return hand == InteractionHand.MAIN_HAND;
	}
	
	public void exec(NonNullConsumer<ICapBefriendableMob> consumer)
	{
		target.getCapability(ModCapabilities.CAP_BEFRIENDABLE_MOB).ifPresent(consumer);
	}
	
	public void execClient(NonNullConsumer<ICapBefriendableMob> consumer)
	{
		if (isClient())
			exec(consumer);
	}
	
	public void execServer(NonNullConsumer<ICapBefriendableMob> consumer)
	{
		if (!isClient())
			exec(consumer);
	}
	
}
