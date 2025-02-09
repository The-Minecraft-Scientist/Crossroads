package com.Da_Technomancer.crossroads.gui.container;

import com.Da_Technomancer.crossroads.api.MiscUtil;
import com.Da_Technomancer.crossroads.api.beams.BeamUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class BeamExtractorCreativeContainer extends AbstractContainerMenu{

	public BeamUnit output;
	public String[] conf;
	public BlockPos pos;

	private static final Supplier<MenuType<?>> TYPE_SPL = MiscUtil.getCRRegistryObject("beam_extractor_creative", ForgeRegistries.Keys.MENU_TYPES);

	public BeamExtractorCreativeContainer(int id, Inventory playerInventory, FriendlyByteBuf data){
		this(id, playerInventory, data == null ? BeamUnit.EMPTY : new BeamUnit(data.readVarIntArray(4)), data == null ? null : new String[] {data.readUtf(), data.readUtf(), data.readUtf(), data.readUtf()}, data == null ? null : data.readBlockPos());
	}

	public BeamExtractorCreativeContainer(int id, Inventory playerInventory, BeamUnit output, String[] settingStrings, BlockPos pos){
		super(TYPE_SPL.get(), id);
		this.output = output;
		this.conf = settingStrings;
		this.pos = pos;
	}

	@Override
	public boolean stillValid(Player playerIn){
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int fromSlot){
		return ItemStack.EMPTY;
	}
}
