package ninja.trek.endnew.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import java.util.stream.Stream;

public class EndBiomeSource extends BiomeSource {
    public static final MapCodec<EndBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.point(new EndBiomeSource())
    );

    private final RegistryEntry<Biome> endBiome;

    public EndBiomeSource() {
        // We'll set the biome later during world generation
        this.endBiome = null;
    }

    @Override
    protected MapCodec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    public Stream<RegistryEntry<Biome>> biomeStream() {
        return Stream.of(endBiome);
    }

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
        
        return endBiome;
    }
}