package app.masterwork.simple.stats.xp;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public enum XpRewardTargetType {
    BLOCK("block", true),
    BLOCK_TAG("block_tag", false),
    ITEM("item", true),
    ITEM_TAG("item_tag", false);

    private static final Map<String, XpRewardTargetType> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(XpRewardTargetType::serializedName, Function.identity()));

    public static final Codec<XpRewardTargetType> CODEC = Codec.STRING.comapFlatMap(
            XpRewardTargetType::decode,
            XpRewardTargetType::serializedName
    );

    private final String serializedName;
    private final boolean exactMatch;

    XpRewardTargetType(String serializedName, boolean exactMatch) {
        this.serializedName = serializedName;
        this.exactMatch = exactMatch;
    }

    public String serializedName() {
        return serializedName;
    }

    public boolean exactMatch() {
        return exactMatch;
    }

    public boolean supports(XpRewardActivity activity) {
        return switch (activity) {
            case BLOCK_BREAK -> this == BLOCK || this == BLOCK_TAG;
            case SMELTING -> this == ITEM || this == ITEM_TAG;
        };
    }

    private static DataResult<XpRewardTargetType> decode(String value) {
        XpRewardTargetType type = BY_NAME.get(value);
        return type != null
                ? DataResult.success(type)
                : DataResult.error(() -> "Unknown xp reward target type: " + value);
    }
}
