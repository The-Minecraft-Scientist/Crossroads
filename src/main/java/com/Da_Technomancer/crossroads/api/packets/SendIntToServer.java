package com.Da_Technomancer.crossroads.api.packets;

import com.Da_Technomancer.essentials.api.packets.ServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class SendIntToServer extends ServerPacket{

	public byte identifier;
	public int message;
	public BlockPos pos;
	public int dim;

	private static final Field[] FIELDS = fetchFields(SendIntToServer.class, "identifier", "message", "pos", "dim");

	@SuppressWarnings("unused")
	public SendIntToServer(){

	}

	public SendIntToServer(byte identifier, int message, BlockPos pos, int dim){
		this.identifier = identifier;
		this.message = message;
		this.pos = pos;
		this.dim = dim;
	}

	@Nonnull
	@Override
	protected Field[] getFields(){
		return FIELDS;
	}

	@Override
	protected void run(@Nullable ServerPlayer serverPlayerEntity){
		if(serverPlayerEntity != null){
			BlockEntity te = serverPlayerEntity.level.getBlockEntity(pos);

			if(te instanceof IDoubleReceiver){
				((IIntReceiver) te).receiveInt(identifier, message, serverPlayerEntity);
			}
		}
	}
}
