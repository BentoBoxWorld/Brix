package world.bentobox.brix.generators;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import world.bentobox.brix.Brix;

/**
 * @author tastybento
 *         Creates the world
 */
public class ChunkGeneratorWorld extends ChunkGenerator {

    private final Brix addon;

    /**
     * @param addon - addon
     */
    public ChunkGeneratorWorld(Brix addon) {
        super();
        this.addon = addon;
    }

    public ChunkData generateChunks(World world) {
        ChunkData result = createChunkData(world);
        Material m = Material.GRASS_BLOCK;
        switch (world.getEnvironment()) {
        case NETHER:
            m = Material.NETHERRACK;
            break;
        case THE_END:
            m = Material.END_STONE;
        default:
            break;

        }
        result.setRegion(0, 0, 0, 16, addon.getSettings().getIslandHeight() + 1, 16, m);
        return result;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
        setBiome(world, biomeGrid);
        return generateChunks(world);
    }

    private void setBiome(World world, BiomeGrid biomeGrid) {
        Biome biome = world.getEnvironment() == Environment.NORMAL ? addon.getSettings().getDefaultBiome() :
            world.getEnvironment() == Environment.NETHER ? addon.getSettings().getDefaultNetherBiome() : addon.getSettings().getDefaultEndBiome();
        for (int x = 0; x < 16; x+=4) {
            for (int z = 0; z < 16; z+=4) {
                for (int y = 0; y < world.getMaxHeight(); y+=4) {
                    biomeGrid.setBiome(x, y, z, biome);
                }
            }
        }

    }

    // This needs to be set to return true to override minecraft's default
    // behavior
    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(final World world) {
        return Collections.emptyList();
    }

}