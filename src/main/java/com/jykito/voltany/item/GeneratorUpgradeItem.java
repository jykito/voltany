package com.jykito.voltany.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GeneratorUpgradeItem extends Item {

    public enum Type {
        OVERCLOCK_1, OVERCLOCK_2, OVERCLOCK_3,
        DISPERSION_1, DISPERSION_2, DISPERSION_3,
        EFFICIENCY,
        SPARK
    }

    private final Type type;

    public GeneratorUpgradeItem(Type type, Properties props) {
        super(props);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.voltany.upgrade." + type.name().toLowerCase())
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
