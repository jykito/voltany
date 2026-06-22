package com.jykito.voltany.registry;

import com.jykito.voltany.Voltany;
import com.jykito.voltany.menu.ManaGeneratorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Voltany.MODID);

    public static final RegistryObject<MenuType<ManaGeneratorMenu>> MANA_GENERATOR =
            MENUS.register("mana_generator",
                    () -> IForgeMenuType.create((id, inv, buf) -> new ManaGeneratorMenu(id, inv, buf)));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
