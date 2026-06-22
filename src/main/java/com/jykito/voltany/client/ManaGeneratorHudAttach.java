package com.jykito.voltany.client;

import com.jykito.voltany.Voltany;
import com.jykito.voltany.block.entity.ManaGeneratorBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.WandHUD;

@Mod.EventBusSubscriber(modid = Voltany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ManaGeneratorHudAttach {

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof ManaGeneratorBlockEntity gen) {
            LazyOptional<WandHUD> hud = LazyOptional.of(() -> new ManaGeneratorWandHud(gen));
            event.addCapability(new ResourceLocation(Voltany.MODID, "wand_hud"),
                    new ICapabilityProvider() {
                        @NotNull
                        @Override
                        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                            return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, hud);
                        }
                    });
            event.addListener(hud::invalidate);
        }
    }
}
