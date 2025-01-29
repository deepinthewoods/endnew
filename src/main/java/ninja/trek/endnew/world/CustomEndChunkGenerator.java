package ninja.trek.endnew.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.GenerationStep;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CustomEndChunkGenerator extends ChunkGenerator {
    private static final int INNER_ISLAND_RADIUS = 128;
    private final ChunkGenerator defaultEndGenerator;

    public static final MapCodec<CustomEndChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    ChunkGenerator.CODEC.fieldOf("end_generator").forGetter(gen -> gen.defaultEndGenerator)
            ).apply(instance, instance.stable(CustomEndChunkGenerator::new))
    );

    public CustomEndChunkGenerator(BiomeSource biomeSource, ChunkGenerator defaultEndGenerator) {
        super(biomeSource);
        this.defaultEndGenerator = defaultEndGenerator;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig,
                                                  StructureAccessor structureAccessor, Chunk chunk) {
        int chunkCenterX = (chunk.getPos().x * 16) + 8;
        int chunkCenterZ = (chunk.getPos().z * 16) + 8;
        double distanceFromCenter = Math.sqrt(chunkCenterX * chunkCenterX + chunkCenterZ * chunkCenterZ);

        if (distanceFromCenter <= INNER_ISLAND_RADIUS) {
            return defaultEndGenerator.populateNoise(blender, noiseConfig, structureAccessor, chunk);
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures,
                             NoiseConfig noiseConfig, Chunk chunk) {
        int chunkCenterX = (chunk.getPos().x * 16) + 8;
        int chunkCenterZ = (chunk.getPos().z * 16) + 8;
        double distanceFromCenter = Math.sqrt(chunkCenterX * chunkCenterX + chunkCenterZ * chunkCenterZ);

        if (distanceFromCenter <= INNER_ISLAND_RADIUS) {
            defaultEndGenerator.buildSurface(region, structures, noiseConfig, chunk);
        }
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig,
                      BiomeAccess biomeAccess, StructureAccessor structureAccessor,
                      Chunk chunk, GenerationStep.Carver carverStep) {
        int chunkCenterX = (chunk.getPos().x * 16) + 8;
        int chunkCenterZ = (chunk.getPos().z * 16) + 8;
        double distanceFromCenter = Math.sqrt(chunkCenterX * chunkCenterX + chunkCenterZ * chunkCenterZ);

        if (distanceFromCenter <= INNER_ISLAND_RADIUS) {
            defaultEndGenerator.carve(chunkRegion, seed, noiseConfig, biomeAccess, structureAccessor, chunk, carverStep);
        }
    }

    @Override
    public void populateEntities(ChunkRegion region) {
        defaultEndGenerator.populateEntities(region);
    }

    @Override
    public int getWorldHeight() {
        return defaultEndGenerator.getWorldHeight();
    }

    @Override
    public int getSeaLevel() {
        return defaultEndGenerator.getSeaLevel();
    }

    @Override
    public int getMinimumY() {
        return defaultEndGenerator.getMinimumY();
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world,
                         NoiseConfig noiseConfig) {
        double distanceFromCenter = Math.sqrt(x * x + z * z);
        if (distanceFromCenter <= INNER_ISLAND_RADIUS) {
            return defaultEndGenerator.getHeight(x, z, heightmap, world, noiseConfig);
        }
        return world.getBottomY();
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world,
                                               NoiseConfig noiseConfig) {
        double distanceFromCenter = Math.sqrt(x * x + z * z);
        if (distanceFromCenter <= INNER_ISLAND_RADIUS) {
            return defaultEndGenerator.getColumnSample(x, z, world, noiseConfig);
        }

        BlockState[] states = new BlockState[world.getHeight()];
        for (int y = 0; y < world.getHeight(); y++) {
            states[y] = Blocks.AIR.getDefaultState();
        }
        return new VerticalBlockSample(world.getBottomY(), states);
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
        text.add("Custom End Generator");
        double distance = Math.sqrt(pos.getX() * pos.getX() + pos.getZ() * pos.getZ());
        text.add(String.format("Distance from center: %.2f", distance));
        if (distance <= INNER_ISLAND_RADIUS) {
            text.add("Using vanilla generation");
        } else {
            text.add("Custom generation zone");
        }
    }
}