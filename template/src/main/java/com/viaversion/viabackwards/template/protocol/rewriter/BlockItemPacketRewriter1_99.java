/*
 * This file is part of ViaBackwards - https://github.com/ViaVersion/ViaBackwards
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viabackwards.template.protocol.rewriter;

import com.viaversion.viabackwards.api.rewriters.BackwardsStructuredItemRewriter;
import com.viaversion.viabackwards.template.protocol.Protocol1_98To1_99;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.rewriter.BlockRewriter;

// To replace if needed:
//   ChunkType1_20_2
//   RecipeRewriter1_20_3
public final class BlockItemPacketRewriter1_99 extends BackwardsStructuredItemRewriter<ClientboundPacket1_20_5, ServerboundPacket1_20_5, Protocol1_98To1_99> {

    public BlockItemPacketRewriter1_99(final Protocol1_98To1_99 protocol) {
        super(protocol, /*old types*/Types1_20_5.ITEM, Types1_20_5.ITEM_ARRAY);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_20_5> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockAction(ClientboundPackets1_20_5.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_20_5.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange1_20(ClientboundPackets1_20_5.MULTI_BLOCK_CHANGE);
        blockRewriter.registerEffect(ClientboundPackets1_20_5.EFFECT, 1010, 2001);
        blockRewriter.registerChunkData1_19(ClientboundPackets1_20_5.CHUNK_DATA, ChunkType1_20_2::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_20_5.BLOCK_ENTITY_DATA);

        // registerOpenWindow(ClientboundPackets1_20_5.OPEN_WINDOW);
        registerSetCooldown(ClientboundPackets1_20_5.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_20_5.WINDOW_ITEMS);
        registerSetSlot1_17_1(ClientboundPackets1_20_5.SET_SLOT);
        registerAdvancements1_20_3(ClientboundPackets1_20_5.ADVANCEMENTS);
        registerEntityEquipmentArray(ClientboundPackets1_20_5.ENTITY_EQUIPMENT);
        registerClickWindow1_17_1(ServerboundPackets1_20_5.CLICK_WINDOW);
        registerTradeList1_20_5(ClientboundPackets1_20_5.TRADE_LIST, Types1_20_5.ITEM_COST, Types1_20_5.ITEM_COST, Types1_20_5.OPTIONAL_ITEM_COST, Types1_20_5.OPTIONAL_ITEM_COST);
        registerCreativeInvAction(ServerboundPackets1_20_5.CREATIVE_INVENTORY_ACTION);
        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_20_5.WINDOW_PROPERTY);
        registerSpawnParticle1_20_5(ClientboundPackets1_20_5.SPAWN_PARTICLE, Types1_20_5.PARTICLE, Types1_20_5.PARTICLE);
        registerExplosion(ClientboundPackets1_20_5.EXPLOSION, Types1_20_5.PARTICLE, Types1_20_5.PARTICLE);

        new RecipeRewriter1_20_3<>(protocol).register1_20_5(ClientboundPackets1_20_5.DECLARE_RECIPES);
    }
}