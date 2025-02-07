package com.Da_Technomancer.crossroads.blocks.alchemy;

import com.Da_Technomancer.crossroads.api.Capabilities;
import com.Da_Technomancer.crossroads.api.alchemy.*;
import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import com.Da_Technomancer.crossroads.blocks.CRTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;

public class ReagentTankTileEntity extends AlchemyCarrierTE{

	public static final BlockEntityType<ReagentTankTileEntity> TYPE = CRTileEntity.createType(ReagentTankTileEntity::new, CRBlocks.reagentTankGlass, CRBlocks.reagentTankCrystal);

	public static final int CAPACITY = 1_000;

	public ReagentTankTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	public ReagentTankTileEntity(BlockPos pos, BlockState state, boolean glass){
		super(TYPE, pos, state, glass);
	}

	public float getReds(){
		return Math.min(CAPACITY, contents.getTotalQty());
	}

	@Override
	public int transferCapacity(){
		return CAPACITY;
	}

	public ReagentMap getMap(){
		return contents;
	}

	public void writeContentNBT(CompoundTag nbt){
		contents = ReagentMap.readFromNBT(nbt);
		dirtyReag = true;
	}

	@Override
	public EnumContainerType getChannel(){
		return EnumContainerType.NONE;
	}

	@Override
	protected EnumTransferMode[] getModes(){
		return new EnumTransferMode[] {EnumTransferMode.BOTH, EnumTransferMode.BOTH, EnumTransferMode.BOTH, EnumTransferMode.BOTH, EnumTransferMode.BOTH, EnumTransferMode.BOTH};
	}

	@Override
	protected void performTransfer(){
		vesselTransfer(this);
	}

	@Override
	public void setRemoved(){
		super.setRemoved();
		itemOpt.invalidate();
	}

	private final LazyOptional<IItemHandler> itemOpt = LazyOptional.of(ItemHandler::new);

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
		if(cap == Capabilities.CHEMICAL_CAPABILITY){
			return (LazyOptional<T>) chemOpt;
		}
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (LazyOptional<T>) itemOpt;
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void correctReag(){
		super.correctReag();
		correctTemp();

		boolean destroy = false;

		ArrayList<IReagent> toRemove = new ArrayList<>(1);

		for(IReagent type : contents.keySetReag()){
			ReagentStack reag = contents.getStack(type);
			if(reag.isEmpty()){
				continue;
			}
			if(glass && reag.getType().requiresCrystal()){
				destroy |= reag.getType().destroysBadContainer();
				toRemove.add(type);
			}
		}

		if(destroy){
			destroyChamber();
		}else{
			for(IReagent type : toRemove){
				contents.removeReagent(type, contents.get(type));
			}
		}
	}

	private boolean broken = false;

	private void destroyChamber(){
		if(!broken){
			broken = true;
			BlockState state = level.getBlockState(worldPosition);
			level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
			SoundType sound = state.getBlock().getSoundType(state, level, worldPosition, null);
			level.playSound(null, worldPosition, sound.getBreakSound(), SoundSource.BLOCKS, sound.getVolume(), sound.getPitch());
			AlchemyUtil.releaseChemical(level, worldPosition, contents);
		}
	}
}
