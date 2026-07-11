package world.bentobox.brix.generators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import world.bentobox.brix.Brix;
import world.bentobox.brix.CommonTestSetup;
import world.bentobox.brix.Settings;

/**
 * Tests for {@link ChunkGeneratorWorld}.
 */
class ChunkGeneratorWorldTest extends CommonTestSetup {

    private Brix addon;
    private Settings settings;
    private BiomeGrid biomeGrid;
    private ChunkGeneratorWorld cg;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        addon = mock(Brix.class);
        settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);
        biomeGrid = mock(BiomeGrid.class);
        when(world.getMinHeight()).thenReturn(0);
        when(world.getMaxHeight()).thenReturn(64);
        cg = new ChunkGeneratorWorld(addon);
    }

    @Test
    void testGenerateChunkDataNormal() {
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        ChunkData data = cg.generateChunkData(world, new Random(0), 0, 0, biomeGrid);
        // Grass up to and including the island height, air above
        assertEquals(Material.GRASS_BLOCK, data.getType(0, settings.getIslandHeight(), 0));
        assertEquals(Material.AIR, data.getType(0, settings.getIslandHeight() + 1, 0));
        verify(biomeGrid, atLeastOnce()).setBiome(anyInt(), anyInt(), anyInt(), eq(Biome.PLAINS));
    }

    @Test
    void testGenerateChunkDataNether() {
        when(world.getEnvironment()).thenReturn(World.Environment.NETHER);
        ChunkData data = cg.generateChunkData(world, new Random(0), 0, 0, biomeGrid);
        assertEquals(Material.NETHERRACK, data.getType(0, settings.getIslandHeight(), 0));
        verify(biomeGrid, atLeastOnce()).setBiome(anyInt(), anyInt(), anyInt(), eq(Biome.NETHER_WASTES));
    }

    @Test
    void testGenerateChunkDataEnd() {
        when(world.getEnvironment()).thenReturn(World.Environment.THE_END);
        ChunkData data = cg.generateChunkData(world, new Random(0), 0, 0, biomeGrid);
        assertEquals(Material.END_STONE, data.getType(0, settings.getIslandHeight(), 0));
        verify(biomeGrid, atLeastOnce()).setBiome(anyInt(), anyInt(), anyInt(), eq(Biome.THE_END));
    }

    @Test
    void testCanSpawn() {
        assertTrue(cg.canSpawn(world, 0, 0));
    }

    @Test
    void testGetDefaultPopulatorsIsEmpty() {
        assertTrue(cg.getDefaultPopulators(world).isEmpty());
    }
}
