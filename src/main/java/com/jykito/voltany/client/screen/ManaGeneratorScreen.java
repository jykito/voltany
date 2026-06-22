package com.jykito.voltany.client.screen;

import com.jykito.voltany.Voltany;
import com.jykito.voltany.menu.ManaGeneratorMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ManaGeneratorScreen extends AbstractContainerScreen<ManaGeneratorMenu> {

    private static final ResourceLocation BG =
            new ResourceLocation(Voltany.MODID, "textures/gui/mana_generator.png");
    private static final ResourceLocation ENERGY_FILL =
            new ResourceLocation(Voltany.MODID, "textures/gui/energybot_fill_6x_anim.png");
    private static final ResourceLocation MANA_FILL =
            new ResourceLocation(Voltany.MODID, "textures/gui/mana_fill_anim_6x.png");

    private static final int TEX_W = 256, TEX_H = 315;

    private static final int BAR_W = 55, BAR_H = 6;
    private static final int ENERGY_X = 101, ENERGY_Y = 34;
    private static final int MANA_X = 101, MANA_Y = 104;
    private static final int FRAMES = 6;
    private static final int FRAME_TICKS = 12;

    private int fillTick = 0;
    private int fillFrame = 0;

    public ManaGeneratorScreen(ManaGeneratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 256;
        this.imageHeight = 224;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blit(BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, TEX_W, TEX_H);

        if (++fillTick >= FRAME_TICKS) {
            fillTick = 0;
            fillFrame = (fillFrame + 1) % FRAMES;
        }
        int vOffset = fillFrame * BAR_H;

        int energyFill = (int) (BAR_W * frac(menu.getEnergy(), menu.getEnergyCapacity()));
        if (energyFill > 0) {
            g.blit(ENERGY_FILL, leftPos + ENERGY_X, topPos + ENERGY_Y,
                    0, vOffset, energyFill, BAR_H, BAR_W, BAR_H * FRAMES);
        }

        int manaFill = (int) (BAR_W * frac(menu.getMana(), menu.getManaCapacity()));
        if (manaFill > 0) {
            g.blit(MANA_FILL, leftPos + MANA_X, topPos + MANA_Y,
                    0, vOffset, manaFill, BAR_H, BAR_W, BAR_H * FRAMES);
        }
    }

    private static float frac(int value, int max) {
        return max <= 0 ? 0f : Math.min(1f, (float) value / max);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {

    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partial);
        renderTooltip(g, mouseX, mouseY);
    }
}
