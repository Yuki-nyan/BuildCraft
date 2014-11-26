package buildcraft.transport.pipes;

import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.util.EnumFacing;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.RFBattery;
import buildcraft.transport.PipeIconProvider;

public class PipePowerEmerald extends PipePowerWood {

	protected int standardIconIndex = PipeIconProvider.TYPE.PipePowerEmerald_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllEmerald_Solid.ordinal();

	public PipePowerEmerald(Item item) {
		super(item);
		
		battery = new RFBattery(2560 * 50, 2560, 0);
		
		transport.initFromPipe(this.getClass());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(EnumFacing direction) {
		if (direction != EnumFacing.UNKNOWN && powerSources[direction.ordinal()]) {
			return solidIconIndex;
		} else {
			return standardIconIndex;
		}
	}
}
