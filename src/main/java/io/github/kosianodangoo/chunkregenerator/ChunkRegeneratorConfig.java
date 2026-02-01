package io.github.kosianodangoo.chunkregenerator;

import net.minecraftforge.common.ForgeConfigSpec;

public class ChunkRegeneratorConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue CHUNK_REGENERATOR_ENERGY_USAGE = BUILDER.comment("The energy usage of chunk regenerator per target chunk").defineInRange("chunk_regenerator_energy_usage", 100_000, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue CHUNK_REGENERATOR_ENERGY_CAPACITY = BUILDER.comment("The energy capacity of chunk regenerator").defineInRange("chunk_regenerator_energy_capacity", 10_000_000, 0, Integer.MAX_VALUE);


    static final ForgeConfigSpec SPEC = BUILDER.build();
}
