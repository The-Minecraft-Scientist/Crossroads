package com.Da_Technomancer.crossroads.tileentities.rotary;

import com.Da_Technomancer.crossroads.API.CRProperties;
import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.rotary.IAxisHandler;
import com.Da_Technomancer.crossroads.API.rotary.IAxleHandler;
import com.Da_Technomancer.crossroads.API.templates.InventoryTE;
import com.Da_Technomancer.crossroads.CRConfig;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import com.Da_Technomancer.crossroads.crafting.CRRecipes;
import com.Da_Technomancer.crossroads.crafting.recipes.StampMillRec;
import com.Da_Technomancer.crossroads.gui.container.StampMillContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

@ObjectHolder(Crossroads.MODID)
public class StampMillTileEntity extends InventoryTE{

	@ObjectHolder("stamp_mill")
	public static TileEntityType<StampMillTileEntity> type = null;

	public static final int TIME_LIMIT = 100;
	public static final int INERTIA = 200;
	public static final double REQUIRED = 800;
	public static final double PROGRESS_PER_RADIAN = 20D;//Energy to consume per radian the internal gear turns
	private double progress = 0;
	private int timer = 0;

	public StampMillTileEntity(){
		super(type, 2);
	}

	public int getProgress(){
		return (int) Math.round(progress);
	}

	public int getTimer(){
		return timer;
	}

	@Override
	protected boolean useRotary(){
		return true;
	}

	@Override
	protected AxleHandler createAxleHandler(){
		return new ThroughAxleHandler();
	}

	public float renderAngle(float partialTicks){
		//This machines doesn't visually switch rotation direction when rotary direction changes
		float prev = axleHandler.getAngle(partialTicks - 1F);
		float cur = axleHandler.getAngle(partialTicks);
		return Math.signum(cur - prev) * cur;
	}

	@Override
	public void addInfo(ArrayList<ITextComponent> chat, PlayerEntity player, BlockRayTraceResult hit){
		chat.add(new TranslationTextComponent("tt.crossroads.boilerplate.progress", (int) progress, (int) REQUIRED));
		super.addInfo(chat, player, hit);
	}

	@Override
	public void tick(){
		super.tick();
		if(!level.isClientSide){
			BlockState state = getBlockState();

			if(state.getBlock() != CRBlocks.stampMill){
				return;
			}

			double progChange = Math.min(Math.abs(energy), Math.min(REQUIRED - progress, PROGRESS_PER_RADIAN * Math.abs(axleHandler.getSpeed()) / 20D));
			axleHandler.addEnergy(-progChange, false);
			if(inventory[1].isEmpty() && !inventory[0].isEmpty()){
				progress += progChange;
				if(++timer >= TIME_LIMIT || progress >= REQUIRED){
					timer = 0;
					if(progress >= REQUIRED){
						progress = 0;
						level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, SoundEvents.ANVIL_PLACE, SoundCategory.BLOCKS, 1, level.random.nextFloat(), true);
						Optional<StampMillRec> recOpt = level.getRecipeManager().getRecipeFor(CRRecipes.STAMP_MILL_TYPE, this, level);
						ItemStack produced;
						if(recOpt.isPresent()){
							produced = recOpt.get().getResultItem();
							produced = produced.copy();
						}else{
							produced = inventory[0].copy();
							produced.setCount(1);
						}
						inventory[0].shrink(1);
						inventory[1] = produced;
					}else{
						inventory[1] = inventory[0].split(1);
						progress -= REQUIRED * CRConfig.stampMillDamping.get() / 100;//By default, stamp mill damping is zero
						if(progress < 0){
							progress = 0;
						}
					}
				}
				setChanged();
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt){
		super.save(nbt);
		nbt.putDouble("prog", progress);
		nbt.putInt("timer", timer);
		return nbt;
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt){
		super.load(state, nbt);
		progress = nbt.getDouble("prog");
		timer = nbt.getInt("timer");
	}

	@Override
	public void setRemoved(){
		super.setRemoved();
		itemOpt.invalidate();
	}

	@Override
	public void clearCache(){
		super.clearCache();
		axleOpt.invalidate();
		axleOpt = LazyOptional.of(() -> axleHandler);
	}

	private final LazyOptional<IItemHandler> itemOpt = LazyOptional.of(ItemHandler::new);

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (LazyOptional<T>) itemOpt;
		}

		BlockState state = getBlockState();
		if(state.getBlock() == CRBlocks.stampMill && cap == Capabilities.AXLE_CAPABILITY && (side == null || side.getAxis() == state.getValue(CRProperties.HORIZ_AXIS))){
			return (LazyOptional<T>) axleOpt;
		}

		return super.getCapability(cap, side);
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity player){
		return new StampMillContainer(id, playerInv, createContainerBuf());
	}

	private class ThroughAxleHandler extends AxleHandler{

		@Override
		public void propagate(IAxisHandler masterIn, byte key, double rotRatioIn, double lastRadius, boolean renderOffset){
			//If true, this has already been checked.
			if(key == updateKey || masterIn.addToList(this)){
				return;
			}

			rotRatio = rotRatioIn == 0 ? 1 : rotRatioIn;
			updateKey = key;
			axis = masterIn;

			BlockState state = getBlockState();
			if(state.getBlock() != CRBlocks.stampMill){
				return;
			}
			Direction.Axis ax = state.getValue(CRProperties.HORIZ_AXIS);
			for(Direction.AxisDirection dir : Direction.AxisDirection.values()){
				Direction side = Direction.get(dir, ax);
				TileEntity te = level.getBlockEntity(worldPosition.relative(side));
				if(te != null){
					LazyOptional<IAxisHandler> axisOpt = te.getCapability(Capabilities.AXIS_CAPABILITY, side.getOpposite());
					if(axisOpt.isPresent()){
						axisOpt.orElseThrow(NullPointerException::new).trigger(masterIn, key);
					}
					LazyOptional<IAxleHandler> oAxleOpt = te.getCapability(Capabilities.AXLE_CAPABILITY, side.getOpposite());
					if(oAxleOpt.isPresent()){
						oAxleOpt.orElseThrow(NullPointerException::new).propagate(masterIn, key, rotRatioIn, lastRadius, renderOffset);
					}
				}
			}
		}
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction){
		return index == 1;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack){
		return index == 0 && level.getRecipeManager().getRecipeFor(CRRecipes.STAMP_MILL_TYPE, new Inventory(stack), level).isPresent();
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TranslationTextComponent("container.stamp_mill");
	}

	@Override
	public double getMoInertia(){
		return INERTIA;
	}

	private static final AxisAlignedBB RENDER_BOX = new AxisAlignedBB(0, 0, 0, 1, 2, 1);

	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		return RENDER_BOX.move(worldPosition);
	}
}
