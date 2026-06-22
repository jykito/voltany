package com.jykito.voltany;

import com.jykito.voltany.registry.ModBlockEntities;
import com.jykito.voltany.registry.ModBlocks;
import com.jykito.voltany.registry.ModCreativeTab;
import com.jykito.voltany.registry.ModItems;
import com.jykito.voltany.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Voltany.MODID)
public class Voltany {
    public static final String MODID = "voltany";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Voltany() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenus.register(modBus);
        ModCreativeTab.register(modBus);

        LOGGER.info("Voltany initializing — Botania addon loaded.");
    }
}
