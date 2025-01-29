package ninja.trek.endnew;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.World;
import ninja.trek.endnew.world.CustomEndChunkGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;

public class EndNew implements ModInitializer {
	public static final String MOD_ID = "endnew";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Register the chunk generator type
		Registry.register(
				Registries.CHUNK_GENERATOR,
				Identifier.of(MOD_ID, "custom_end"),
				CustomEndChunkGenerator.CODEC
		);

		// Handle dimension setup when the server starts
		ServerWorldEvents.LOAD.register((server, world) -> {
			if (world.getRegistryKey().equals(World.END)) {
				// Get the vanilla end generator
				ChunkGenerator vanillaEndGenerator = server.getWorld(World.END).getChunkManager().getChunkGenerator();

				// Create our custom generator
				ChunkGenerator customGenerator = new CustomEndChunkGenerator(
						TheEndBiomeSource.createVanilla(server.getRegistryManager().getWrapperOrThrow(RegistryKeys.BIOME)),
						vanillaEndGenerator
				);

				// Replace the end dimension's chunk generator
				try {
					Field generatorField = world.getChunkManager().getClass().getDeclaredField("chunkGenerator");
					generatorField.setAccessible(true);
					generatorField.set(world.getChunkManager(), customGenerator);
				} catch (Exception e) {
					LOGGER.error("Failed to set custom End generator", e);
				}
			}
		});
	}
}