package com.jykito.voltany.block.entity;

import com.google.common.base.Predicates;
import com.jykito.voltany.block.ManaGeneratorBlock;
import com.jykito.voltany.item.GeneratorUpgradeItem;
import com.jykito.voltany.menu.ManaGeneratorMenu;
import com.jykito.voltany.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.ManaNetwork;
import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManaGeneratorBlockEntity extends BlockEntity implements MenuProvider, ManaPool, SparkAttachable {

    public static final int FE_PER_MANA = 10;
    public static final int FE_PER_MANA_EFFICIENT = 6;
    public static final int BASE_EMPTY = 80;
    public static final int BASE_UNIT = 80;
    public static final int RADIUS = 1;

    public static final int ENERGY_CAPACITY = 256_000;
    public static final int ENERGY_MAX_RECEIVE = 8_000;
    public static final int MANA_CAPACITY = 1_000_000;

    private final GenEnergy energy = new GenEnergy();
    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    private final ItemStackHandler flower = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            recalc();
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return flowerTier(stack) > 0;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Math.max(1, maxFlowerStack);
        }
    };

    private final ItemStackHandler upgrades = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            recalc();
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof GeneratorUpgradeItem;
        }
    };

    private int mana = 0;

    private double overclockMult = 1.0;
    private int maxFlowerStack = 1;
    private boolean dispersionMode = false;
    private int dispersionRadius = 0;
    private int feRatio = FE_PER_MANA;
    private boolean sparkMode = false;
    private int manaPerTickEff = BASE_EMPTY;

    private final SimpleContainerData data = new SimpleContainerData(4);

    public ManaGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_GENERATOR.get(), pos, state);
    }

    public static int flowerTier(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !id.getNamespace().equals("botania")) {
            return 0;
        }
        return switch (id.getPath()) {
            case "daybloom", "nightshade", "hydroangeas", "endoflame" -> 4;
            case "thermalily", "munchdew", "gourmaryllis", "kekimurus",
                 "entropinnium", "narslimmus", "spectrolus" -> 8;
            case "rafflowsia", "shulk_me_not", "dandelifeon" -> 16;
            default -> id.getPath().endsWith("_mystical_flower") ? 2 : 0;
        };
    }

    private void recalc() {
        int oc = 0, disp = 0;
        boolean eff = false, spark = false;
        for (int i = 0; i < upgrades.getSlots(); i++) {
            if (!(upgrades.getStackInSlot(i).getItem() instanceof GeneratorUpgradeItem up)) {
                continue;
            }
            switch (up.getType()) {
                case OVERCLOCK_1 -> oc = Math.max(oc, 1);
                case OVERCLOCK_2 -> oc = Math.max(oc, 2);
                case OVERCLOCK_3 -> oc = Math.max(oc, 3);
                case DISPERSION_1 -> disp = Math.max(disp, 1);
                case DISPERSION_2 -> disp = Math.max(disp, 2);
                case DISPERSION_3 -> disp = Math.max(disp, 3);
                case EFFICIENCY -> eff = true;
                case SPARK -> spark = true;
            }
        }
        overclockMult = switch (oc) {
            case 1 -> 1.5;
            case 2, 3 -> 2.0;
            default -> 1.0;
        };
        maxFlowerStack = oc == 3 ? 8 : 1;
        dispersionMode = disp > 0;
        dispersionRadius = switch (disp) {
            case 1 -> 5;
            case 2 -> 15;
            case 3 -> 30;
            default -> 0;
        };
        feRatio = eff ? FE_PER_MANA_EFFICIENT : FE_PER_MANA;
        sparkMode = spark;

        ItemStack f = flower.getStackInSlot(0);
        int tier = flowerTier(f);
        int gen;
        if (tier <= 0) {
            gen = BASE_EMPTY;
        } else {
            int count = Math.min(f.getCount(), maxFlowerStack);
            gen = BASE_UNIT * tier * count;
        }
        manaPerTickEff = (int) Math.round(gen * overclockMult);

        int feNeeded = Math.max(ENERGY_MAX_RECEIVE, manaPerTickEff * feRatio);
        energy.configure(Math.max(ENERGY_CAPACITY, feNeeded * 4), feNeeded);
    }

    public void tickServer() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        boolean working = false;

        int room = MANA_CAPACITY - mana;
        int generated = Math.min(manaPerTickEff, Math.min(room, energy.getEnergyStored() / feRatio));
        if (generated > 0) {
            mana += generated;
            energy.consume(generated * feRatio);
            working = true;
        }

        boolean sparkActive = sparkMode && getAttachedSpark() != null;
        int sent = 0;
        if (mana > 0 && !sparkActive) {
            int budget = mana;
            if (dispersionMode) {
                sent = distributeToPools(level, getBlockPos(), dispersionRadius, budget);
            } else {
                ManaNetwork network = BotaniaAPI.instance().getManaNetworkInstance();
                ManaPool pool = network.getClosestPool(getBlockPos(), level, RADIUS);
                if (pool != null && !pool.isFull()) {
                    sent = sendTo(pool, pool.getMaxMana(), budget);
                } else {
                    ManaCollector collector = network.getClosestCollector(getBlockPos(), level, RADIUS);
                    if (collector != null && !collector.isFull()) {
                        sent = sendTo(collector, collector.getMaxMana(), budget);
                    }
                }
            }
            mana -= sent;
        }
        if (sent > 0 || sparkActive) {
            working = true;
        }

        updateLit(working);
        if (generated > 0 || sent > 0) {
            setChanged();
        }

        int e = energy.getEnergyStored();
        data.set(0, e & 0xFFFF);
        data.set(1, (e >> 16) & 0xFFFF);
        data.set(2, mana & 0xFFFF);
        data.set(3, (mana >> 16) & 0xFFFF);

        if (level.getGameTime() % 10L == 0L) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static int sendTo(ManaReceiver target, int targetMax, int budget) {
        int headroom = targetMax - target.getCurrentMana();
        int send = Math.min(budget, Math.max(0, headroom));
        if (send <= 0) {
            return 0;
        }
        target.receiveMana(send);
        return send;
    }

    private static int distributeToPools(Level level, BlockPos origin, int radius, int budget) {
        ManaNetwork net = BotaniaAPI.instance().getManaNetworkInstance();
        long radiusSq = (long) radius * radius;
        List<ManaPool> targets = new ArrayList<>();
        for (ManaPool pool : net.getAllPoolsInWorld(level)) {
            if (!pool.isFull() && pool.getManaReceiverPos().distSqr(origin) <= radiusSq) {
                targets.add(pool);
            }
        }
        if (targets.isEmpty()) {
            return 0;
        }
        int per = budget / targets.size();
        int remainder = budget % targets.size();
        int sent = 0;
        for (ManaPool pool : targets) {
            int give = per + (remainder-- > 0 ? 1 : 0);
            int headroom = pool.getMaxMana() - pool.getCurrentMana();
            give = Math.min(give, Math.max(0, headroom));
            if (give > 0) {
                pool.receiveMana(give);
                sent += give;
            }
        }
        return sent;
    }

    private void updateLit(boolean lit) {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(ManaGeneratorBlock.LIT) && state.getValue(ManaGeneratorBlock.LIT) != lit) {
            level.setBlock(getBlockPos(), state.setValue(ManaGeneratorBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(flower.getSlots() + upgrades.getSlots());
        int idx = 0;
        for (int i = 0; i < flower.getSlots(); i++) {
            container.setItem(idx++, flower.getStackInSlot(i));
        }
        for (int i = 0; i < upgrades.getSlots(); i++) {
            container.setItem(idx++, upgrades.getStackInSlot(i));
        }
        Containers.dropContents(level, pos, container);
    }

    public ItemStackHandler getFlowerHandler() {
        return flower;
    }

    public ItemStackHandler getUpgradeHandler() {
        return upgrades;
    }

    public ContainerData getContainerData() {
        return data;
    }

    public int getMana() {
        return mana;
    }

    public int getManaCapacity() {
        return MANA_CAPACITY;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.voltany.mana_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ManaGeneratorMenu(id, inv, this);
    }

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    public boolean isFull() {
        return true;
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return false;
    }

    @Override
    public void receiveMana(int delta) {

        if (delta < 0) {
            mana = Math.max(0, mana + delta);
            setChanged();
        }
    }

    @Override
    public boolean isOutputtingPower() {
        return sparkMode;
    }

    @Override
    public int getMaxMana() {
        return MANA_CAPACITY;
    }

    @Override
    public Optional<DyeColor> getColor() {
        return Optional.empty();
    }

    @Override
    public void setColor(Optional<DyeColor> color) {
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return sparkMode;
    }

    @Override
    public ManaSpark getAttachedSpark() {
        Level level = getLevel();
        if (level == null) {
            return null;
        }
        BlockPos above = getBlockPos().above();
        List<Entity> sparks = level.getEntitiesOfClass(Entity.class,
                new AABB(above, above.offset(1, 1, 1)), Predicates.instanceOf(ManaSpark.class));
        return sparks.size() == 1 ? (ManaSpark) sparks.get(0) : null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return true;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return 0;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        energyCap = LazyOptional.of(() -> energy);
        recalc();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Mana", mana);
        tag.put("Flower", flower.serializeNBT());
        tag.put("Upgrades", upgrades.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.load(tag.getInt("Energy"));
        mana = Mth.clamp(tag.getInt("Mana"), 0, MANA_CAPACITY);
        flower.deserializeNBT(tag.getCompound("Flower"));
        upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        recalc();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    private class GenEnergy extends EnergyStorage {
        GenEnergy() {
            super(ENERGY_CAPACITY, ENERGY_MAX_RECEIVE, 0);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = super.receiveEnergy(toReceive, simulate);
            if (received > 0 && !simulate) {
                setChanged();
            }
            return received;
        }

        void consume(int amount) {
            this.energy = Math.max(0, this.energy - amount);
        }

        void load(int amount) {
            this.energy = Mth.clamp(amount, 0, this.capacity);
        }

        void configure(int cap, int maxRec) {
            this.capacity = cap;
            this.maxReceive = maxRec;
            if (this.energy > cap) {
                this.energy = cap;
            }
        }
    }
}
