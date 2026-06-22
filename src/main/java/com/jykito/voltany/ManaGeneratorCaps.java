package com.jykito.voltany;

import com.jykito.voltany.block.entity.ManaGeneratorBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;

@Mod.EventBusSubscriber(modid = Voltany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ManaGeneratorCaps {

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (!(event.getObject() instanceof ManaGeneratorBlockEntity gen)) {
            return;
        }
        LazyOptional<ManaReceiver> recv = LazyOptional.of(() -> gen);
        LazyOptional<SparkAttachable> spark = LazyOptional.of(() -> gen);

        event.addCapability(new ResourceLocation(Voltany.MODID, "mana_receiver"),
                new ICapabilityProvider() {
                    @NotNull
                    @Override
                    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                        return BotaniaForgeCapabilities.MANA_RECEIVER.orEmpty(cap, recv);
                    }
                });
        event.addCapability(new ResourceLocation(Voltany.MODID, "spark_attachable"),
                new ICapabilityProvider() {
                    @NotNull
                    @Override
                    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                        return BotaniaForgeCapabilities.SPARK_ATTACHABLE.orEmpty(cap, spark);
                    }
                });
        event.addListener(recv::invalidate);
        event.addListener(spark::invalidate);
    }
}
