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
package com.viaversion.viabackwards.protocol.protocol1_15_2to1_16.packets;

import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.protocol1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viabackwards.protocol.protocol1_15_2to1_16.data.WorldNameTracker;
import com.viaversion.viabackwards.protocol.protocol1_15_2to1_16.storage.WolfDataMaskStorage;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.entity.StoredEntityData;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_15;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import com.viaversion.viaversion.util.Key;

public class EntityPackets1_16 extends EntityRewriter<ClientboundPackets1_16, Protocol1_15_2To1_16> {

    private final ValueTransformer<String, Integer> dimensionTransformer = new ValueTransformer<String, Integer>(Type.STRING, Type.INT) {
        @Override
        public Integer transform(PacketWrapper wrapper, String input) {
            input = Key.namespaced(input);
            switch (input) {
                case "minecraft:the_nether":
                    return -1;
                default:
                case "minecraft:overworld":
                    return 0;
                case "minecraft:the_end":
                    return 1;
            }
        }
    };

    public EntityPackets1_16(Protocol1_15_2To1_16 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_16.SPAWN_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - Entity UUID
                map(Type.VAR_INT); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Pitch
                map(Type.BYTE); // 7 - Yaw
                map(Type.INT); // 8 - Data
                handler(wrapper -> {
                    EntityType entityType = typeFromId(wrapper.get(Type.VAR_INT, 1));
                    if (entityType == EntityTypes1_16.LIGHTNING_BOLT) {
                        // Map to old weather entity packet
                        wrapper.cancel();

                        PacketWrapper spawnLightningPacket = wrapper.create(ClientboundPackets1_15.SPAWN_GLOBAL_ENTITY);
                        spawnLightningPacket.write(Type.VAR_INT, wrapper.get(Type.VAR_INT, 0)); // Entity id
                        spawnLightningPacket.write(Type.BYTE, (byte) 1); // Lightning type
                        spawnLightningPacket.write(Type.DOUBLE, wrapper.get(Type.DOUBLE, 0)); // X
                        spawnLightningPacket.write(Type.DOUBLE, wrapper.get(Type.DOUBLE, 1)); // Y
                        spawnLightningPacket.write(Type.DOUBLE, wrapper.get(Type.DOUBLE, 2)); // Z
                        spawnLightningPacket.send(Protocol1_15_2To1_16.class);
                    }
                });
                handler(getSpawnTrackerWithDataHandler(EntityTypes1_16.FALLING_BLOCK));
            }
        });

        registerSpawnTracker(ClientboundPackets1_16.SPAWN_MOB);

        protocol.registerClientbound(ClientboundPackets1_16.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(dimensionTransformer); // Dimension Type
                handler(wrapper -> {
                    // Grab the tracker for world names
                    WorldNameTracker worldNameTracker = wrapper.user().get(WorldNameTracker.class);
                    String nextWorldName = wrapper.read(Type.STRING); // World Name

                    wrapper.passthrough(Type.LONG); // Seed
                    wrapper.passthrough(Type.UNSIGNED_BYTE); // Gamemode
                    wrapper.read(Type.BYTE); // Previous gamemode

                    // Grab client world
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimension = wrapper.get(Type.INT, 0);

                    // Send a dummy respawn with a different dimension if the world name was different and the same dimension was used
                    if (clientWorld.getEnvironment() != null && dimension == clientWorld.getEnvironment().id()
                        && (wrapper.user().isClientSide() || Via.getPlatform().isProxy()
                        || wrapper.user().getProtocolInfo().protocolVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2) // Hotfix for https://github.com/ViaVersion/ViaBackwards/issues/381
                        || !nextWorldName.equals(worldNameTracker.getWorldName()))) {
                        PacketWrapper packet = wrapper.create(ClientboundPackets1_15.RESPAWN);
                        packet.write(Type.INT, dimension == 0 ? -1 : 0);
                        packet.write(Type.LONG, 0L);
                        packet.write(Type.UNSIGNED_BYTE, (short) 0);
                        packet.write(Type.STRING, "default");
                        packet.send(Protocol1_15_2To1_16.class);
                    }

                    clientWorld.setEnvironment(dimension);

                    wrapper.write(Type.STRING, "default"); // Level type
                    wrapper.read(Type.BOOLEAN); // Debug
                    if (wrapper.read(Type.BOOLEAN)) {
                        wrapper.set(Type.STRING, 0, "flat");
                    }
                    wrapper.read(Type.BOOLEAN); // Keep all playerdata

                    // Finally update the world name
                    worldNameTracker.setWorldName(nextWorldName);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); //  Entity ID
                map(Type.UNSIGNED_BYTE); // Gamemode
                read(Type.BYTE); // Previous gamemode
                read(Type.STRING_ARRAY); // World list
                read(Type.NAMED_COMPOUND_TAG); // whatever this is
                map(dimensionTransformer); // Dimension Type
                handler(wrapper -> {
                    WorldNameTracker worldNameTracker = wrapper.user().get(WorldNameTracker.class);
                    worldNameTracker.setWorldName(wrapper.read(Type.STRING)); // Save the world name
                });
                map(Type.LONG); // Seed
                map(Type.UNSIGNED_BYTE); // Max players
                handler(wrapper -> {
                    ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                    clientChunks.setEnvironment(wrapper.get(Type.INT, 1));
                    tracker(wrapper.user()).addEntity(wrapper.get(Type.INT, 0), EntityTypes1_16.PLAYER);

                    wrapper.write(Type.STRING, "default"); // Level type

                    wrapper.passthrough(Type.VAR_INT); // View distance
                    wrapper.passthrough(Type.BOOLEAN); // Reduced debug info
                    wrapper.passthrough(Type.BOOLEAN); // Show death screen

                    wrapper.read(Type.BOOLEAN); // Debug
                    if (wrapper.read(Type.BOOLEAN)) {
                        wrapper.set(Type.STRING, 0, "flat");
                    }
                });
            }
        });

        registerTracker(ClientboundPackets1_16.SPAWN_EXPERIENCE_ORB, EntityTypes1_16.EXPERIENCE_ORB);
        // F Spawn Global Object, it is no longer with us :(
        registerTracker(ClientboundPackets1_16.SPAWN_PAINTING, EntityTypes1_16.PAINTING);
        registerTracker(ClientboundPackets1_16.SPAWN_PLAYER, EntityTypes1_16.PLAYER);
        registerRemoveEntities(ClientboundPackets1_16.DESTROY_ENTITIES);
        registerMetadataRewriter(ClientboundPackets1_16.ENTITY_METADATA, Types1_16.METADATA_LIST, Types1_14.METADATA_LIST);

        protocol.registerClientbound(ClientboundPackets1_16.ENTITY_PROPERTIES, wrapper -> {
            wrapper.passthrough(Type.VAR_INT);
            int size = wrapper.passthrough(Type.INT);
            for (int i = 0; i < size; i++) {
                String attributeIdentifier = wrapper.read(Type.STRING);
                String oldKey = protocol.getMappingData().attributeIdentifierMappings().get(attributeIdentifier);
                wrapper.write(Type.STRING, oldKey != null ? oldKey : Key.stripMinecraftNamespace(attributeIdentifier));

                wrapper.passthrough(Type.DOUBLE);
                int modifierSize = wrapper.passthrough(Type.VAR_INT);
                for (int j = 0; j < modifierSize; j++) {
                    wrapper.passthrough(Type.UUID);
                    wrapper.passthrough(Type.DOUBLE);
                    wrapper.passthrough(Type.BYTE);
                }
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16.PLAYER_INFO, wrapper -> {
            int action = wrapper.passthrough(Type.VAR_INT);
            int playerCount = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < playerCount; i++) {
                wrapper.passthrough(Type.UUID);
                if (action == 0) { // Add
                    wrapper.passthrough(Type.STRING);
                    int properties = wrapper.passthrough(Type.VAR_INT);
                    for (int j = 0; j < properties; j++) {
                        wrapper.passthrough(Type.STRING);
                        wrapper.passthrough(Type.STRING);
                        wrapper.passthrough(Type.OPTIONAL_STRING);
                    }
                    wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.VAR_INT);
                    // Display Name
                    protocol.getTranslatableRewriter().processText(wrapper.user(), wrapper.passthrough(Type.OPTIONAL_COMPONENT));
                } else if (action == 1) { // Update Game Mode
                    wrapper.passthrough(Type.VAR_INT);
                } else if (action == 2) { // Update Ping
                    wrapper.passthrough(Type.VAR_INT);
                } else if (action == 3) { // Update Display Name
                    // Display name
                    protocol.getTranslatableRewriter().processText(wrapper.user(), wrapper.passthrough(Type.OPTIONAL_COMPONENT));
                } // 4 = Remove Player
            }
        });
    }

    @Override
    protected void registerRewrites() {
        filter().handler((event, meta) -> {
            meta.setMetaType(Types1_14.META_TYPES.byId(meta.metaType().typeId()));

            MetaType type = meta.metaType();
            if (type == Types1_14.META_TYPES.itemType) {
                meta.setValue(protocol.getItemRewriter().handleItemToClient(event.user(), (Item) meta.getValue()));
            } else if (type == Types1_14.META_TYPES.blockStateType) {
                meta.setValue(protocol.getMappingData().getNewBlockStateId((int) meta.getValue()));
            } else if (type == Types1_14.META_TYPES.particleType) {
                rewriteParticle(event.user(), (Particle) meta.getValue());
            } else if (type == Types1_14.META_TYPES.optionalComponentType) {
                JsonElement text = meta.value();
                if (text != null) {
                    protocol.getTranslatableRewriter().processText(event.user(), text);
                }
            }
        });

        mapEntityType(EntityTypes1_16.ZOMBIFIED_PIGLIN, EntityTypes1_15.ZOMBIE_PIGMAN);
        mapTypes(EntityTypes1_16.values(), EntityTypes1_15.class);

        mapEntityTypeWithData(EntityTypes1_16.HOGLIN, EntityTypes1_16.COW).jsonName();
        mapEntityTypeWithData(EntityTypes1_16.ZOGLIN, EntityTypes1_16.COW).jsonName();
        mapEntityTypeWithData(EntityTypes1_16.PIGLIN, EntityTypes1_16.ZOMBIFIED_PIGLIN).jsonName();
        mapEntityTypeWithData(EntityTypes1_16.STRIDER, EntityTypes1_16.MAGMA_CUBE).jsonName();

        filter().type(EntityTypes1_16.ZOGLIN).cancel(16);
        filter().type(EntityTypes1_16.HOGLIN).cancel(15);

        filter().type(EntityTypes1_16.PIGLIN).cancel(16);
        filter().type(EntityTypes1_16.PIGLIN).cancel(17);
        filter().type(EntityTypes1_16.PIGLIN).cancel(18);

        filter().type(EntityTypes1_16.STRIDER).index(15).handler((event, meta) -> {
            boolean baby = meta.value();
            meta.setTypeAndValue(Types1_14.META_TYPES.varIntType, baby ? 1 : 3);
        });
        filter().type(EntityTypes1_16.STRIDER).cancel(16);
        filter().type(EntityTypes1_16.STRIDER).cancel(17);
        filter().type(EntityTypes1_16.STRIDER).cancel(18);

        filter().type(EntityTypes1_16.FISHING_BOBBER).cancel(8);

        filter().type(EntityTypes1_16.ABSTRACT_ARROW).cancel(8);
        filter().type(EntityTypes1_16.ABSTRACT_ARROW).handler((event, meta) -> {
            if (event.index() >= 8) {
                event.setIndex(event.index() + 1);
            }
        });

        filter().type(EntityTypes1_16.WOLF).index(16).handler((event, meta) -> {
            byte mask = meta.value();
            StoredEntityData data = tracker(event.user()).entityData(event.entityId());
            data.put(new WolfDataMaskStorage(mask));
        });

        filter().type(EntityTypes1_16.WOLF).index(20).handler((event, meta) -> {
            StoredEntityData data = tracker(event.user()).entityDataIfPresent(event.entityId());
            byte previousMask = 0;
            if (data != null) {
                WolfDataMaskStorage wolfData = data.get(WolfDataMaskStorage.class);
                if (wolfData != null) {
                    previousMask = wolfData.tameableMask();
                }
            }

            int angerTime = meta.value();
            byte tameableMask = (byte) (angerTime > 0 ? previousMask | 2 : previousMask & -3);
            event.createExtraMeta(new Metadata(16, Types1_14.META_TYPES.byteType, tameableMask));
            event.cancel();
        });
    }

    @Override
    public EntityType typeFromId(int typeId) {
        return EntityTypes1_16.getTypeFromId(typeId);
    }
}
