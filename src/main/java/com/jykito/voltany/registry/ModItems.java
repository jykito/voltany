package com.jykito.voltany.registry;

import com.jykito.voltany.Voltany;
import com.jykito.voltany.item.GeneratorUpgradeItem;
import com.jykito.voltany.item.GeneratorUpgradeItem.Type;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Voltany.MODID);

    public static final RegistryObject<Item> MANA_GENERATOR = ITEMS.register("mana_generator",
            () -> new BlockItem(ModBlocks.MANA_GENERATOR.get(), new Item.Properties()));

    private static RegistryObject<Item> upgrade(String id, Type type) {
        return ITEMS.register(id, () -> new GeneratorUpgradeItem(type, new Item.Properties().stacksTo(1)));
    }

    public static final RegistryObject<Item> OVERCLOCK_1 = upgrade("overclock_1", Type.OVERCLOCK_1);
    public static final RegistryObject<Item> OVERCLOCK_2 = upgrade("overclock_2", Type.OVERCLOCK_2);
    public static final RegistryObject<Item> OVERCLOCK_3 = upgrade("overclock_3", Type.OVERCLOCK_3);
    public static final RegistryObject<Item> DISPERSION_1 = upgrade("dispersion_1", Type.DISPERSION_1);
    public static final RegistryObject<Item> DISPERSION_2 = upgrade("dispersion_2", Type.DISPERSION_2);
    public static final RegistryObject<Item> DISPERSION_3 = upgrade("dispersion_3", Type.DISPERSION_3);
    public static final RegistryObject<Item> EFFICIENCY_CORE = upgrade("efficiency_core", Type.EFFICIENCY);
    public static final RegistryObject<Item> SPARK_RESONATOR = upgrade("spark_resonator", Type.SPARK);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
