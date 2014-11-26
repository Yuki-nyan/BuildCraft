/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.schematics;

import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class BptPipeWooden extends BptPipeExtension {

	public BptPipeWooden(Item i) {
		super (i);
	}

	@Override
	public void rotateLeft(SchematicTile slot, IBuilderContext context) {
		int orientation = slot.meta & 7;
		int others = slot.meta - orientation;

		slot.meta = EnumFacing.values()[orientation].getRotation(EnumFacing.UP).ordinal() + others;
	}

}
