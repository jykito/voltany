package com.jykito.voltany.menu;

import com.jykito.voltany.block.entity.ManaGeneratorBlockEntity;
import com.jykito.voltany.registry.ModBlocks;
import com.jykito.voltany.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ManaGeneratorMenu extends AbstractContainerMenu {

    public static final int CONTAINER_SLOTS = 5;

    private static final int FLOWER_X = 120, FLOWER_Y = 64;
    private static final int[][] UPGRADES = {{65, 38}, {65, 91}, {176, 38}, {176, 91}};
    private static final int INV_X = 44, INV_Y = 133, HOTBAR_Y = 196, SLOT_STEP = 19;

    private final ManaGeneratorBlockEntity be;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public ManaGeneratorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, resolve(inv, buf));
    }

    public ManaGeneratorMenu(int id, Inventory inv, ManaGeneratorBlockEntity be) {
        super(ModMenus.MANA_GENERATOR.get(), id);
        this.be = be;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());
        this.data = be.getContainerData();

        addSlot(new SlotItemHandler(be.getFlowerHandler(), 0, FLOWER_X, FLOWER_Y));
        for (int i = 0; i < UPGRADES.length; i++) {
            addSlot(new SlotItemHandler(be.getUpgradeHandler(), i, UPGRADES[i][0], UPGRADES[i][1]));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, INV_X + col * SLOT_STEP, INV_Y + row * SLOT_STEP));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, INV_X + col * SLOT_STEP, HOTBAR_Y));
        }

        addDataSlots(this.data);
    }

    private static ManaGeneratorBlockEntity resolve(Inventory inv, FriendlyByteBuf buf) {
        return (ManaGeneratorBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos());
    }

    public int getEnergy() {
        return (data.get(1) << 16) | (data.get(0) & 0xFFFF);
    }

    public int getEnergyCapacity() {
        return ManaGeneratorBlockEntity.ENERGY_CAPACITY;
    }

    public int getMana() {
        return (data.get(3) << 16) | (data.get(2) & 0xFFFF);
    }

    public int getManaCapacity() {
        return ManaGeneratorBlockEntity.MANA_CAPACITY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < CONTAINER_SLOTS) {
                if (!moveItemStackTo(stack, CONTAINER_SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, CONTAINER_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.MANA_GENERATOR.get());
    }
}
