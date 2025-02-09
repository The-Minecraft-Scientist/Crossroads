package com.Da_Technomancer.crossroads.blocks.witchcraft;

import com.Da_Technomancer.crossroads.api.Capabilities;
import com.Da_Technomancer.crossroads.api.heat.HeatUtil;
import com.Da_Technomancer.crossroads.api.templates.InventoryTE;
import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import com.Da_Technomancer.crossroads.blocks.CRTileEntity;
import com.Da_Technomancer.crossroads.crafting.CRRecipes;
import com.Da_Technomancer.crossroads.crafting.FormulationVatRec;
import com.Da_Technomancer.crossroads.gui.container.FormulationVatContainer;
import com.Da_Technomancer.essentials.api.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

public class FormulationVatTileEntity extends InventoryTE{

	public static final BlockEntityType<FormulationVatTileEntity> TYPE = CRTileEntity.createType(FormulationVatTileEntity::new, CRBlocks.formulationVat);

	public static final int[] TEMP_TIERS = {0, 75, 85, 95, 98, 100};
	public static final double[] SPEED_MULT = {0.25D, 0.5D, 1, 2, 4, 0};
	public static final int[] HEAT_DRAIN = {0, 2, 4, 8, 8, 8};
	public static final int REQUIRED = 200;
	private double progress = 0;

	public FormulationVatTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state, 1);
		fluidProps[0] = new TankProperty(4_000, true, false);
		fluidProps[1] = new TankProperty(4_000, false, true);
		initFluidManagers();
	}

	@Override
	public void addInfo(ArrayList<Component> chat, Player player, BlockHitResult hit){
		chat.add(Component.translatable("tt.crossroads.boilerplate.progress", progress, REQUIRED));
		super.addInfo(chat, player, hit);
	}

	@Override
	protected int fluidTanks(){
		return 2;
	}

	@Override
	protected boolean useHeat(){
		return true;
	}

	public FluidStack getInputFluid(){
		return fluids[0];
	}

	public int getProgess(){
		return (int) progress;
	}

	@Override
	public void serverTick(){
		super.serverTick();

		FormulationVatRec rec = null;

		if(!inventory[0].isEmpty() && !fluids[0].isEmpty()){
			Optional<FormulationVatRec> recOpt = level.getRecipeManager().getRecipeFor(CRRecipes.FORMULATION_VAT_TYPE, this, level);
			if(recOpt.isPresent()){
				rec = recOpt.get();
				if(rec.getInputQty() > fluids[0].getAmount() || (!fluids[1].isEmpty() && !BlockUtil.sameFluid(rec.getOutput(), fluids[1])) || rec.getOutput().getAmount() > fluidProps[1].capacity - fluids[1].getAmount()){
					//Ensure that there is sufficient fluid to craft, and we can fit the output
					rec = null;
				}
			}
		}

		if(rec == null){
			progress = 0;
		}

		//Actually advance the progress and consume heat
		int tier = HeatUtil.getHeatTier(temp, TEMP_TIERS);
		if(tier >= 0){
			temp -= HEAT_DRAIN[tier];

			if(rec != null){
				progress += SPEED_MULT[tier];
				if(progress >= REQUIRED){
					FluidStack created = rec.getOutput();
					progress = 0;
					if(fluids[1].isEmpty()){
						fluids[1] = created.copy();
					}else{
						fluids[1].grow(created.getAmount());
					}
					inventory[0].shrink(1);
					fluids[0].shrink(rec.getInputQty());
				}
			}

			setChanged();
		}
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);
		progress = nbt.getDouble("prog");
	}

	@Override
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);
		nbt.putDouble("prog", progress);
	}

	@Override
	public void setRemoved(){
		super.setRemoved();
		itemOpt.invalidate();
	}

	private final LazyOptional<IItemHandler> itemOpt = LazyOptional.of(ItemHandler::new);

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing){
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			return (LazyOptional<T>) globalFluidOpt;
		}

		if(capability == Capabilities.HEAT_CAPABILITY){
			return (LazyOptional<T>) heatOpt;
		}

		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (LazyOptional<T>) itemOpt;
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction){
		return false;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack){
		return index == 0 && level.getRecipeManager().getAllRecipesFor(CRRecipes.FORMULATION_VAT_TYPE).stream().anyMatch(rec -> rec.getIngredients().get(0).test(stack));
	}

	@Override
	public Component getDisplayName(){
		return Component.translatable("container.crossroads.formulation_vat");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity){
		return new FormulationVatContainer(id, playerInventory, createContainerBuf());
	}
}
