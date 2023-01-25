package com.sodium.dwmg.registries;

import com.sodium.dwmg.DwMG;
import com.sodium.dwmg.DwMGTab;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DwMG.MOD_ID);
	
	/* Blocks */
	
	public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of
		(Material.METAL, MaterialColor.COLOR_PURPLE).strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()));		

	
	// Auto register block items
	@SubscribeEvent
	public static void onRegisterItems(final RegistryEvent.Register<Item> event) {
	    final IForgeRegistry<Item> registry = event.getRegistry();

	    BLOCKS.getEntries().stream().map(RegistryObject::get).forEach( (block) -> {
	        final Item.Properties properties = new Item.Properties().tab(DwMGTab.TAB);
	        final BlockItem blockItem = new BlockItem(block, properties);
	        blockItem.setRegistryName(block.getRegistryName());
	        registry.register(blockItem);
	    });
	}
	
	
	public static void register(IEventBus eventBus) {
	    BLOCKS.register(eventBus);
	}


}
