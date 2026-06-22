package com.jykito.voltany.client;

import com.jykito.voltany.block.entity.ManaGeneratorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.WandHUD;

public class ManaGeneratorWandHud implements WandHUD {

    private final ManaGeneratorBlockEntity be;

    public ManaGeneratorWandHud(ManaGeneratorBlockEntity be) {
        this.be = be;
    }

    @Override
    public void renderHUD(GuiGraphics gui, Minecraft mc) {
        ItemStack icon = new ItemStack(be.getBlockState().getBlock());
        String name = icon.getHoverName().getString();
        BotaniaAPIClient.instance().drawSimpleManaHUD(gui, 0x0095FF, be.getMana(), be.getManaCapacity(), name);
    }
}
