package com.Da_Technomancer.crossroads.API.effects;

import com.Da_Technomancer.crossroads.API.beams.EnumBeamAlignments;
import com.Da_Technomancer.crossroads.CRConfig;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.blocks.BlockSalt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class GrowEffect extends BeamEffect{

	//Crop types can be blacklisted from growth through the beam using the grow_blacklist tag. Intended for things like magical crops
	private static final Tag<Block> growBlacklist = BlockTags.createOptional(new ResourceLocation(Crossroads.MODID, "grow_blacklist"));
	protected static final DamageSource POTENTIAL_VOID = new DamageSource("potentialvoid").setMagic().bypassArmor();
	protected static final DamageSource POTENTIAL_VOID_ABSOLUTE = new DamageSource("potentialvoid").setMagic().bypassArmor().bypassMagic();


	@Override
	public void doBeamEffect(EnumBeamAlignments align, boolean voi, int power, Level worldIn, BlockPos pos, @Nullable Direction dir){
		if(!performTransmute(align, voi, power, worldIn, pos)){
			double range = Math.sqrt(power) / 2D;
			if(voi){
				//Kill plants
				if(!BlockSalt.salinate(worldIn, pos)){
					//Also target the plant on this block so we can hit the soil and affect the plant on it
					BlockSalt.salinate(worldIn, pos.above());
				}

				List<LivingEntity> ents = worldIn.getEntitiesOfClass(LivingEntity.class, new AABB(pos.getX() - range, pos.getY() - range, pos.getZ() - range, pos.getX() + range + 1, pos.getY() + range + 1, pos.getZ() + range + 1), EntitySelector.ENTITY_STILL_ALIVE);
				boolean absoluteDamage = CRConfig.beamDamageAbsolute.get();
				for(LivingEntity ent : ents){
					if(ent.isInvertedHealAndHarm()){
						ent.heal(power * 3F / 4F);
					}else{
						ent.hurt(absoluteDamage ? POTENTIAL_VOID_ABSOLUTE : POTENTIAL_VOID, power * 3F / 4F);
					}
				}
			}else{
				List<LivingEntity> ents = worldIn.getEntitiesOfClass(LivingEntity.class, new AABB(pos.getX() - range, pos.getY() - range, pos.getZ() - range, pos.getX() + range, pos.getY() + range, pos.getZ() + range), EntitySelector.ENTITY_STILL_ALIVE);
				boolean absoluteDamage = CRConfig.beamDamageAbsolute.get();
				for(LivingEntity ent : ents){
					if(ent.isInvertedHealAndHarm()){
						ent.hurt(absoluteDamage ? POTENTIAL_VOID_ABSOLUTE : POTENTIAL_VOID, power / 2F);
					}else{
						ent.heal(power / 2F);
					}
				}

				//Optional config option to nerf the bonemeal effect
				int growMultiplier = CRConfig.growMultiplier.get();
				if(growMultiplier > 1){
					power = power / growMultiplier + ((worldIn.random.nextInt(growMultiplier) < power % growMultiplier) ? 1 : 0);
				}

				BlockState state = worldIn.getBlockState(pos);
				//We check above the hit block if it isn't growable, as that allows growing plants by hitting the soil
				if(!(state.getBlock() instanceof BonemealableBlock)){
					pos = pos.above();
					state = worldIn.getBlockState(pos);
				}

				for(int i = 0; i < power; i++){
					if(!(state.getBlock() instanceof BonemealableBlock)){
						return;
					}

					if(growBlacklist.contains(state.getBlock())){
						return;
					}
					BonemealableBlock growable = (BonemealableBlock) state.getBlock();
					if(growable.isValidBonemealTarget(worldIn, pos, state, false)){
						growable.performBonemeal((ServerLevel) worldIn, worldIn.random, pos, state);
					}
					//The state must be quarried every loop because some plants could break themselves upon growing
					state = worldIn.getBlockState(pos);
				}
			}
		}
	}
}
