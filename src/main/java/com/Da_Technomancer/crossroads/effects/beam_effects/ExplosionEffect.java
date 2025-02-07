package com.Da_Technomancer.crossroads.effects.beam_effects;

import com.Da_Technomancer.crossroads.api.beams.EnumBeamAlignments;
import com.Da_Technomancer.crossroads.entity.EntityGhostMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ExplosionEffect extends BeamEffect{

	@Override
	public void doBeamEffect(EnumBeamAlignments align, boolean voi, int power, Level worldIn, BlockPos pos, @Nullable Direction dir){
		if(!performTransmute(align, voi, power, worldIn, pos)){
			if(voi){
				worldIn.explode(null, pos.getX(), pos.getY(), pos.getZ(), (int) Math.min(Math.ceil(power / 4D), 16), Explosion.BlockInteraction.BREAK);
			}else{
				//Suppress explosions
				EntityGhostMarker marker = new EntityGhostMarker(worldIn, EntityGhostMarker.EnumMarkerType.EQUILIBRIUM);
				marker.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
				CompoundTag rangeData = new CompoundTag();
				rangeData.putInt("range", power);
				marker.data = rangeData;
				worldIn.addFreshEntity(marker);

				//Effect in crystal master axis
			}
		}
	}
}
