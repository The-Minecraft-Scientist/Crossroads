package com.Da_Technomancer.crossroads.render;

import com.Da_Technomancer.crossroads.ambient.sounds.CRSounds;
import com.Da_Technomancer.crossroads.api.render.CRRenderUtil;
import com.Da_Technomancer.crossroads.api.render.IVisualEffect;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.Random;

public class LooseArcRenderable implements IVisualEffect{

	private final float xSt;
	private final float ySt;
	private final float zSt;
	private final float xEn;
	private final float yEn;
	private final float zEn;
	private final int count;
	private final float diffusionRate;
	private final int color;
	private byte lifeTime;
	private long lastTick = -1;
	private final Vec3[][] states;

	private LooseArcRenderable(Level world, float xSt, float ySt, float zSt, float xEn, float yEn, float zEn, int count, float diffusionRate, byte lifespan, int color, boolean sound){
		this.xSt = xSt;
		this.ySt = ySt;
		this.zSt = zSt;
		this.xEn = xEn;
		this.yEn = yEn;
		this.zEn = zEn;
//		this.xStFin = xStFin;
//		this.yStFin = yStFin;
//		this.zStFin = zStFin;
		this.count = count;
		this.diffusionRate = diffusionRate;
		this.color = color;
		states = new Vec3[count][9];
		this.lifeTime = lifespan;
		if(sound){
			SoundEvent soundEvent;
			float volume;
			float pitch;
			float arcLength = (Math.abs(zEn - zSt) + Math.abs(yEn - ySt) + Math.abs(xEn - xSt));//Done in taxicab distance, because it isn't that important
			if(arcLength >= 1.1F){//Very short arcs use a sparking sound effect, longer arcs use a buzzing sound
				soundEvent = CRSounds.ELECTRIC_ARC;
				volume = Math.max(arcLength / 10F, 0.1F);//Longer arcs are louder
				pitch = 1.5F;
			}else{
				soundEvent = CRSounds.ELECTRIC_SPARK;
				volume = 0.2F;
				pitch = 1F;
			}
			CRSounds.playSoundClientLocal(world, new BlockPos((xSt + xEn) / 2F, (ySt + yEn) / 2F, (zSt + zEn) / 2F), soundEvent, SoundSource.BLOCKS, volume, pitch);
		}
	}

	public static LooseArcRenderable readFromNBT(Level world, CompoundTag nbt){
		return new LooseArcRenderable(world, nbt.getFloat("x"), nbt.getFloat("y"), nbt.getFloat("z"), nbt.getFloat("x_e"), nbt.getFloat("y_e"), nbt.getFloat("z_e"), nbt.getInt("count"), nbt.getFloat("diffu"), nbt.getByte("lif"), nbt.getInt("color"), nbt.getBoolean("sound"));
	}

	@Override
	public boolean render(PoseStack matrix, MultiBufferSource buffer, long worldTime, float partialTicks, Random rand){
		final float arcWidth = 0.03F;
		Color colorObj = new Color(color, true);
		int[] col = {colorObj.getRed(), colorObj.getGreen(), colorObj.getBlue(), colorObj.getAlpha()};
//		float mult = ((float) (lifeSpan - lifeTime) + partialTicks) / (float) lifeSpan;
		Vec3 start = new Vec3(xSt, ySt, zSt);
		VertexConsumer builder = buffer.getBuffer(CRRenderTypes.ELECTRIC_ARC_TYPE);

		matrix.translate(start.x, start.y, start.z);

		//If the arc is newly created, generate its path
		if(lastTick != worldTime && lastTick < 0){
			Vec3 lengthVec = new Vec3(xEn - start.x, yEn - start.y, zEn - start.z);
			double length = lengthVec.length();
			//lengthVec is a normalized vector pointing from the start to end point
			lengthVec = lengthVec.normalize();
			//crossVec is a normalized vector perpendicular to the lengthVec. Used to make bolts jut outwards from the center
			Vec3 crossVec = lengthVec.cross(CRRenderUtil.VEC_I);
			//In the unlikely event that the I unit vector happens to be parallel to lengthVec, we use an a different arbitrary vector
			if(crossVec.lengthSqr() == 0){
				crossVec = lengthVec.cross(CRRenderUtil.VEC_J);
			}
			crossVec = crossVec.normalize();

			//The angle in radians that each bolt shall be offset from the adjacent bolts. Used to ensure consistent spacing
			double angle = 2D * Math.PI / count;

			for(int i = 0; i < count; i++){
				//Whether this bolt has "diverged" from its target, causing it to miss the end position
				boolean diverged = rand.nextFloat() < diffusionRate;

				for(int node = 0; node < states[0].length; node++){
					//portion is the the fraction of the total length this node traverses (from 0 to ~1), ignoring the random component. The function used is arbitrary, but was chosen due to a pleasant scaling.
					double portion = -0.012229D * Math.pow(node, 2) + 0.222835D * node - 0.0030303D;
					if(portion < 0){
						portion = 0D;
					}
					//Generate the next node position
					states[i][node] = lengthVec.scale(portion * length).add(crossVec.scale(length / 6D * (diverged ? Math.sqrt(portion) : Math.sqrt(portion) - Math.pow(portion, 2))));
					//Endpoints are fixed- otherwise add some random variance in position
					if(node != 0 && node != states[i].length - 1){
						states[i][node] = states[i][node].add(rand.nextDouble() / 4D, rand.nextDouble() / 4D, rand.nextDouble() / 4D);
					}
				}
				
				//Rotates crossVec angle radians about lengthVec
				//This formula is only valid because crossVec and lengthVec are perpendicular
				crossVec = crossVec.scale(Math.cos(angle)).add(crossVec.cross(lengthVec).scale(Math.sin(angle)));
			}
		}

		//Render the arcs as stored in states
		for(int i = 0; i < count; i++){
			for(int node = 1; node < states[0].length; node++){
				//Replaced with a full 3d render using a helper method

//				//We generate a vector based on the player's perspective for use creating a thickness to rendered lines. The vector is chosen such to maximize the apparent thickness from the given view angle
//				//The width vector is the vector from the player's eyes to the closest point on the link line (were it extended indefinitely) to the player's eyes, all cross the link line vector.
//				//If you want to know where this formula comes from... I'm not cramming a quarter page of calculus into these comments
//				Vec3d offsetVec = new Vec3d(states[i][node - 1].x + start.x - playerX, states[i][node - 1].y + start.y - playerY - Minecraft.getInstance().player.getEyeHeight(), states[i][node - 1].z + start.z - playerZ);
//				Vec3d deltaVec = states[i][node].subtract(states[i][node - 1]);
//				Vec3d vec = offsetVec.add(deltaVec.scale(-deltaVec.dotProduct(offsetVec) / deltaVec.lengthSquared())).crossProduct(deltaVec);
//				vec = vec.scale(arcWidth / 2F / vec.length());
//
//				buf.pos(states[i][node].x - vec.x, states[i][node].y - vec.y, states[i][node].z - vec.z).endVertex();
//				buf.pos(states[i][node].x + vec.x, states[i][node].y + vec.y, states[i][node].z + vec.z).endVertex();
//				buf.pos(states[i][node - 1].x + vec.x, states[i][node - 1].y + vec.y, states[i][node - 1].z + vec.z).endVertex();
//				buf.pos(states[i][node - 1].x - vec.x, states[i][node - 1].y - vec.y, states[i][node - 1].z - vec.z).endVertex();

				CRRenderUtil.draw3dLine(builder, matrix, states[i][node - 1], states[i][node], arcWidth, col, CRRenderUtil.BRIGHT_LIGHT);
			}
		}

		if(lastTick != worldTime){
			lastTick = worldTime;
			return --lifeTime < 0;
		}

		return false;
	}
}
