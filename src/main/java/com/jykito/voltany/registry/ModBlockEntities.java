package com.jykito.voltany.registry;

import com.jykito.voltany.Voltany;
import com.jykito.voltany.block.entity.ManaGeneratorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Voltany.MODID);

    public static final RegistryObject<BlockEntityType<ManaGeneratorBlockEntity>> MANA_GENERATOR =
            BLOCK_ENTITIES.register("mana_generator",
                    () -> BlockEntityType.Builder.of(ManaGeneratorBlockEntity::new,
                            ModBlocks.MANA_GENERATOR.get()).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
