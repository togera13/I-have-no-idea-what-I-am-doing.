package net.sodiumstudio.dwmg.entities.capabilities;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.network.PacketDistributor;
import net.sodiumstudio.befriendmobs.entity.befriended.IBefriendedMob;
import net.sodiumstudio.nautils.Wrapped;
import net.sodiumstudio.dwmg.network.ClientGamePacketHandler;
import net.sodiumstudio.dwmg.network.DwmgChannels;
import net.sodiumstudio.dwmg.registries.DwmgCapabilities;

/**
 * Main capability of Favorability system.
 * Favorability change rule:
 * 
 * Increase
 * * Sleep to skip night near the mob: +2
 * * Attack an entity attacking the mob: +(final damage / 50)
 * * Kill an entity attacking the mob: +1
 * * The mob killing an entity attacking the player: +0.5
 * * The mob attacking an entity attacking the player: +(final damage / 100)
 * * Apply healing item: + (healing value / 50)
 * 
 * Decrease
 * * Hurting the mob: -(0~5) depending on the damage
 * * Killing the mob: to zero
 * * Mob dying with player nearby (8 blocks): -20
 */
public interface CFavorabilityHandler extends INBTSerializable<CompoundTag>
{
	
	public Mob getMob();
	
	public CompoundTag getExtraNbt();
	
	public float getFavorability();
	
	public float getMaxFavorability();
	
	public void setFavorability(float value);
	
	public void setMaxFavorability(float value);

	public void addFavorability(float deltaValue);
	/**
	 * Sync the data to client.
	 * executed on server every tick
	 */
	public void sync(ServerPlayer toPlayer);

	// ========================
	
	public static class Impl implements CFavorabilityHandler
	{

		protected float value = 50f;
		protected float maxValue = 100f;
		protected final Mob mob;
		protected CompoundTag extraNbt = new CompoundTag();
		
		public Impl(Mob mob)
		{
			this.mob = mob;
		}
		
		@Override
		public CompoundTag serializeNBT() {
			CompoundTag tag = new CompoundTag();
			tag.putFloat("value", value);
			tag.putFloat("max", maxValue);
			tag.put("nbt", extraNbt);
			return tag;
		}
		@Override
		public void deserializeNBT(CompoundTag nbt) {
			this.value = nbt.getFloat("value");
			this.maxValue = nbt.getFloat("max");
			this.extraNbt = nbt.getCompound("nbt");
		}
		@Override
		public Mob getMob() {
			return mob;
		}
		
		@Override
		public CompoundTag getExtraNbt() {
			return extraNbt;
		}
		
		@Override
		public float getFavorability() {
			return value;
		}
		@Override
		public void setFavorability(float value) {
			float actualValue = Mth.clamp(value, 0, getMaxFavorability());
			if (this.value == actualValue)
				return;
			if (!MinecraftForge.EVENT_BUS.post(new ChangeValueEvent(getMob(), this.value, actualValue)))
			{
				this.value = actualValue;
			}
		}
		@Override
		public void addFavorability(float deltaValue) {
			setFavorability(value + deltaValue);
		}

		@Override
		public float getMaxFavorability() {
			return maxValue;
		}

		@Override
		public void setMaxFavorability(float value) {
			if (this.maxValue == value)
				return;
			if (!MinecraftForge.EVENT_BUS.post(new ChangeMaxEvent(getMob(), this.maxValue, value)))
			{
				this.maxValue = value;					
				if (this.getFavorability() > this.getMaxFavorability())
				{
					this.setFavorability(this.getMaxFavorability());
				}
			}
		}	
		
		@Override
		public void sync(ServerPlayer toPlayer)
		{
			SyncPacket packet = new SyncPacket(mob.getId(), getFavorability(), getMaxFavorability());
			DwmgChannels.SYNC_CHANNEL.send(PacketDistributor.PLAYER.with(() -> toPlayer), packet);
		}
	}
	
	// ========================
	
	public class Prvd implements ICapabilitySerializable<CompoundTag>
	{

		CFavorabilityHandler cap;
		
		public Prvd(Mob mob)
		{
			cap = new Impl(mob);
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if (cap == DwmgCapabilities.CAP_FAVORABILITY_HANDLER)
				return LazyOptional.of(() -> {return this.cap;}).cast();
			else return LazyOptional.empty();
		}

		@Override
		public CompoundTag serializeNBT() {
			return cap.serializeNBT();
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			cap.deserializeNBT(nbt);		
		}
	}
	
	// ========================
	
	/**
	 * Fired when the favorability value changes.
	 * Cancelable. If canceled the value won't change.
	 */
	@Cancelable
	public class ChangeValueEvent extends Event
	{
		public final Mob mob;
		public final float fromValue;
		public final float toValue;
		
		public ChangeValueEvent(Mob mob, float fromVal, float toVal)
		{
			this.mob = mob;
			this.fromValue = fromVal;
			this.toValue = toVal;
		}
		
		public ChangeValueEvent(IBefriendedMob mob, float fromVal, float toVal)
		{
			this.mob = mob.asMob();
			this.fromValue = fromVal;
			this.toValue = toVal;
		}
		
		
		/**
		 * Cast the mob to IBefriendedMob.
		 * @return Cast result, or {@code null} if the mob doesn't implement IBefriendedMob.
		 */
		@Nullable
		public IBefriendedMob asBefriended()
		{
			if (mob instanceof IBefriendedMob bm)
			{
				return bm;
			}
			else return null;
		}
	}
	
	// ========================
	
	/**
	 * Fired when the max value changes.
	 * Cancelable. If canceled the value won't change.
	 */
	@Cancelable
	public class ChangeMaxEvent extends Event
	{
		public final Mob mob;
		public final float fromMax;
		public final float toMax;
		
		public ChangeMaxEvent(Mob mob, float fromVal, float toVal)
		{
			this.mob = mob;
			this.fromMax = fromVal;
			this.toMax = toVal;
		}
		
		public ChangeMaxEvent(IBefriendedMob mob, float fromVal, float toVal)
		{
			this.mob = mob.asMob();
			this.fromMax = fromVal;
			this.toMax = toVal;
		}

		/**
		 * Cast the mob to {@code IBefriendedMob}.
		 * @return Cast result, or {@code null} if the mob doesn't implement {@code IBefriendedMob}.
		 */
		@Nullable
		public IBefriendedMob asBefriended()
		{
			if (mob instanceof IBefriendedMob bm)
			{
				return bm;
			}
			else return null;
		}
	}
	
	// ====================================
	
	public static class SyncPacket implements Packet<ClientGamePacketListener>
	{
		public final int entityId;
		public final float favorability;
		public final float maxFavorability;
		
		public SyncPacket(int entityId, float favorability, float maxFavorability) {
			this.entityId = entityId;
			this.favorability = favorability;
			this.maxFavorability = maxFavorability;
		}

		public SyncPacket(FriendlyByteBuf buffer) {
			this.entityId = buffer.readInt();
			this.favorability = buffer.readFloat();
			this.maxFavorability = buffer.readFloat();
		}


		@Override
		public void write(FriendlyByteBuf buffer) {
			buffer.writeInt(this.entityId);
			buffer.writeFloat(this.favorability);
			buffer.writeFloat(this.maxFavorability);
			
		}

		@SuppressWarnings("resource")
		@Override
		public void handle(ClientGamePacketListener handler) 
		{
			ClientGamePacketHandler.handleFavorabilityHandlerSync(this, handler);
		}	
	}
	
	// =====================
	
	public static boolean isLowFavorability(Mob mob, float threshold)
	{
		Wrapped<Boolean> res = new Wrapped<Boolean>(true);
		mob.getCapability(DwmgCapabilities.CAP_FAVORABILITY_HANDLER).ifPresent((cap) -> {
			res.set(cap.getFavorability() < threshold);
		});
		return res.get();
	}
	
	public static boolean isLowFavorability(Mob mob)
	{
		return isLowFavorability(mob, 5f);
	}
}
