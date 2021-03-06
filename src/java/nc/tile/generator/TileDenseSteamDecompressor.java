package nc.tile.generator;

import nc.NuclearCraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.TileFluidHandler;

public class TileDenseSteamDecompressor extends TileEntity implements IFluidHandler {
	public int fluid;
	public FluidTank tank;
	public int fluid2;
	public FluidTank tank2;

	public TileDenseSteamDecompressor() {
		super();
		tank = new FluidTank(2);
		tank2 = new FluidTank(2000);
	}
	
	public void updateEntity() {
    	super.updateEntity();
    	if(!this.worldObj.isRemote) {
    		steam();
    		addSteam();
    	}
        markDirty();
    }
	
	public void addSteam() {
		int i = 0;
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = this.worldObj.getTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);
			if (tile instanceof TileSteamDecompressor) {
				if ((tile instanceof IFluidHandler)) {
					tank2.drain(((IFluidHandler)tile).fill(side.getOpposite(), tank2.drain(tank2.getCapacity(), false), true), true);
					i ++;
				}
			}
		}
		if (i > 0) return;
		
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = this.worldObj.getTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);
			
			if (!(tile instanceof TileDenseSteamDecompressor)) {
				if ((tile instanceof IFluidHandler)) {
					tank2.drain(((IFluidHandler)tile).fill(side.getOpposite(), tank2.drain(tank2.getCapacity(), false), true), true);
				} else if ((tile instanceof TileFluidHandler)) {
					tank2.drain(((TileFluidHandler)tile).fill(side.getOpposite(), tank2.drain(tank2.getCapacity(), false), true), true);
				}
			}
		}
	}
	
	public void steam() {
		if (tank.getFluidAmount() != 0) {
			if (tank.getFluid().getFluid() == NuclearCraft.superdenseSteam || tank.getFluid().getFluid() == FluidRegistry.getFluid("superdenseSteam")) {
				for (int i = 0; i < 4; i++) {
					if (tank2.getFluidAmount() <= tank2.getCapacity() - 1000 && tank.getFluidAmount() != 0) {
						tank2.fill(new FluidStack(NuclearCraft.denseSteam, 1000), true);
						tank.drain(1, true);
					} else break;
				}
			}
		}
	}

	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return this.tank.fill(resource, doFill);
	}

	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return this.tank2.drain(resource.amount, doDrain);
	}

	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return this.tank2.drain(maxDrain, doDrain);
	}

	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return fluid == NuclearCraft.superdenseSteam || fluid == FluidRegistry.getFluid("superdenseSteam");
	}

	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}

	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[] {this.tank.getInfo()};
	}
	
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.tank.readFromNBT(nbt.getCompoundTag("tank"));
		this.tank2.readFromNBT(nbt.getCompoundTag("tank2"));
	}

	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagCompound fluidTag = new NBTTagCompound();
		this.tank.writeToNBT(fluidTag);
		nbt.setTag("tank", fluidTag);
		this.tank2.writeToNBT(fluidTag);
		nbt.setTag("tank2", fluidTag);
	}
	
	public void writeFluid(NBTTagCompound nbt) {
		NBTTagCompound fluidTag = new NBTTagCompound();
		this.tank.writeToNBT(fluidTag);
		nbt.setTag("tank", fluidTag);
		this.tank2.writeToNBT(fluidTag);
		nbt.setTag("tank2", fluidTag);
	}

	public Packet getDescriptionPacket() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		nbtTag.setInteger("fluid", this.tank.getFluidAmount());
		this.fluid = nbtTag.getInteger("fluid");
		nbtTag.setInteger("fluid2", this.tank.getFluidAmount());
		this.fluid2 = nbtTag.getInteger("fluid2");
		writeFluid(nbtTag);
		this.writeToNBT(nbtTag);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbtTag);
	}
	
	public void readFluid(NBTTagCompound nbt) {
		if (nbt.hasKey("tank")) {
			this.tank.readFromNBT(nbt.getCompoundTag("tank"));
		}
		if (nbt.hasKey("tank2")) {
			this.tank2.readFromNBT(nbt.getCompoundTag("tank2"));
		}
	}

	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		super.onDataPacket(net, packet);
		readFluid(packet.func_148857_g());
		this.readFromNBT(packet.func_148857_g());
	}
}