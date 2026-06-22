package com.jykito.voltany.registry;

import com.jykito.voltany.Voltany;
import com.jykito.voltany.block.ManaGeneratorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Voltany.MODID);

    public static final RegistryObject<Block> MANA_GENERATOR = BLOCKS.register("mana_generator",
            () -> new ManaGeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.LAPIS)
                    .strength(3.5f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(ManaGeneratorBlock.LIT) ? 7 : 0)));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
