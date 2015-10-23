/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.proxy.CoreProxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ChannelHandler extends FMLIndexedMessageToMessageCodec<Packet> {
    private static boolean recordStats = false;

    public static ChannelHandler createChannelHandler() {
        return recordStats ? new ChannelHandlerStats() : new ChannelHandler();
    }

    private int maxDiscriminator;

    protected ChannelHandler() {
        // Packets common to buildcraft.core.network
        addDiscriminator(0, PacketTileUpdate.class);
        addDiscriminator(1, PacketTileState.class);
        addDiscriminator(2, PacketSlotChange.class);
        addDiscriminator(3, PacketGuiReturn.class);
        addDiscriminator(4, PacketGuiWidget.class);
        addDiscriminator(5, PacketUpdate.class);
        addDiscriminator(6, PacketCommand.class);
        addDiscriminator(7, PacketEntityUpdate.class);
        maxDiscriminator = 8;
    }

    public void registerPacketType(Class<? extends Packet> packetType) {
        addDiscriminator(maxDiscriminator++, packetType);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, Packet packet, ByteBuf data) throws Exception {
        INetHandler handler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        EntityPlayer player = CoreProxy.proxy.getPlayerFromNetHandler(handler);
        Side side = ctx.channel().attr(NetworkRegistry.CHANNEL_SOURCE).get();
        World world = packet.tempWorld;

        if (player == null) {
            switch (side) {
                case CLIENT: {
                    // ok, WTF?
                    player = getMinecraftPlayer();
                    BCLog.logger.warn("Had to get the player via MINECRAFT rather than the channel!");
                    if (player == null) {
                        BCLog.logger.warn("... BUT THE PLAYER WAS STILL NULL? MAJOR BUG HERE!");
                    } else {
                        if (world == null) {
                            world = player.worldObj;
                        }
                    }
                    break;
                }
                case SERVER: {
                    MinecraftServer server = MinecraftServer.getServer();
                    for (WorldServer serverWorld : server.worldServers) {
                        if (serverWorld.provider.getDimensionId() == packet.dimensionId) {
                            if (world == null) {
                                world = serverWorld;
                            }
                            for (EntityPlayer p2 : (List<EntityPlayer>) serverWorld.playerEntities) {
                                player = p2;
                                BCLog.logger.warn("Had to manually search the server for the first available player! THIS IS BAD!!!");
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
        boolean noPlayer = player == null;
        boolean noWorld = world == null;
        if (noWorld) {
            BCLog.logger.warn("The world  was null when trying to write a packet! Very bad!");
        }
        try {
            packet.writeData(data, world, player);
        } catch (Throwable t) {
            if (noPlayer) {
                BCLog.logger.error("A packet failed to write its data! This could be casued by there not being a player?", t);
            } else {
                BCLog.logger.error("A packet failed to write its data! THIS IS VERY BAD!", t);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer getMinecraftPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, Packet packet) {
        INetHandler handler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        packet.readData(data);
        packet.player = CoreProxy.proxy.getPlayerFromNetHandler(handler);
    }
}
