package app.masterwork.simple.skills;

import java.util.List;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;

import app.masterwork.simple.skills.sync.PlayerSkillsSnapshotPayload;
import app.masterwork.simple.skills.sync.SkillSnapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerSkillsSnapshotPayloadTest {
    @Test
    void snapshotPayloadRoundTripsThroughStreamCodec() {
        PlayerSkillsSnapshotPayload payload = new PlayerSkillsSnapshotPayload(
                650,
                1_750,
                List.of(
                        new SkillSnapshot(SkillRegistry.HEAT_RESISTANCE.id(), true),
                        new SkillSnapshot(SkillRegistry.MAGIC_SENSE.id(), false),
                        new SkillSnapshot(SkillRegistry.GREAT_SAGE.id(), false)
                )
        );

        RegistryFriendlyByteBuf writeBuffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        PlayerSkillsSnapshotPayload.STREAM_CODEC.encode(writeBuffer, payload);

        RegistryFriendlyByteBuf readBuffer = new RegistryFriendlyByteBuf(writeBuffer.copy(), RegistryAccess.EMPTY);
        PlayerSkillsSnapshotPayload decoded = PlayerSkillsSnapshotPayload.STREAM_CODEC.decode(readBuffer);

        assertEquals(payload, decoded);
    }
}
