package com.Da_Technomancer.crossroads.blocks.heat;

import com.Da_Technomancer.crossroads.api.Capabilities;
import com.Da_Technomancer.crossroads.api.heat.HeatUtil;
import com.Da_Technomancer.crossroads.api.templates.ModuleTE;
import com.Da_Technomancer.crossroads.blocks.CRTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;

import static com.Da_Technomancer.crossroads.blocks.CRBlocks.heatSink;

public class HeatSinkTileEntity extends ModuleTE{

	public static final BlockEntityType<HeatSinkTileEntity> TYPE = CRTileEntity.createType(HeatSinkTileEntity::new, heatSink);

	public static final int[] MODES = {5, 10, 15, 20, 25};
	private int mode = 0;

	public HeatSinkTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	public int cycleMode(){
		mode = (mode + 1) % MODES.length;
		setChanged();
		return mode;
	}

	private Double biomeTempCache = null;

	private double getBiomeTemp(){
		if(biomeTempCache == null){
			biomeTempCache = HeatUtil.convertBiomeTemp(level, worldPosition);
		}
		return biomeTempCache;
	}

	@Override
	protected boolean useHeat(){
		return true;
	}

	@Override
	public void addInfo(ArrayList<Component> chat, Player player, BlockHitResult hit){
		chat.add(Component.translatable("tt.crossroads.heat_sink.loss",  MODES[mode]));
		super.addInfo(chat, player, hit);
	}

	@Override
	public void serverTick(){
		super.serverTick();

		double prevTemp = temp;
		double biomeTemp = getBiomeTemp();
		temp += Math.min(MODES[mode], Math.abs(temp - biomeTemp)) * Math.signum(biomeTemp - temp);
		if(temp != prevTemp){
			setChanged();
		}
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);
		mode = nbt.getInt("mode");
	}

	@Override
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);
		nbt.putInt("mode", mode);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing){
		if(capability == Capabilities.HEAT_CAPABILITY){
			return (LazyOptional<T>) heatOpt;
		}

		return super.getCapability(capability, facing);
	}
}
