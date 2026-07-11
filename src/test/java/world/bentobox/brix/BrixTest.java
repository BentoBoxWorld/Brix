package world.bentobox.brix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.brix.generators.ChunkGeneratorWorld;

/**
 * Tests for the main {@link Brix} addon class.
 */
class BrixTest extends CommonTestSetup {

    private static final String CONFIG_YML =
            """
                    # Brix Configuration
                    uniqueId: config
                    """;

    @Mock
    private AddonsManager am;

    private Brix addon;
    private MockedStatic<DatabaseSetup> mockDb;

    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Database mock
        AbstractDatabaseHandler<Object> h = mock(AbstractDatabaseHandler.class);
        mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));

        // CommandsManager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // AddonsManager
        when(plugin.getAddonsManager()).thenReturn(am);
        when(am.getGameModeAddons()).thenReturn(Collections.emptyList());

        // FlagsManager
        when(plugin.getFlagsManager()).thenReturn(fm);
        when(fm.getFlags()).thenReturn(Collections.emptyList());

        // Create addon with a JAR containing config.yml
        addon = new Brix();
        File jFile = new File("addon.jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jFile))) {
            addJarEntry(jos, "config.yml", CONFIG_YML);
        }
        File dataFolder = new File("addons/Brix");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "Brix", "1.0.0")
                .description("test").authors("tastybento").build();
        addon.setDescription(desc);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        if (mockDb != null) {
            mockDb.closeOnDemand();
        }
        super.tearDown();
        new File("addon.jar").delete();
        deleteAll(new File("addons"));
    }

    private static void addJarEntry(JarOutputStream jos, String name, String content) throws Exception {
        JarEntry entry = new JarEntry(name);
        jos.putNextEntry(entry);
        jos.write(content.getBytes(StandardCharsets.UTF_8));
        jos.closeEntry();
    }

    @Test
    void testGetSettingsNullBeforeLoad() {
        assertNull(addon.getSettings());
    }

    @Test
    void testOnLoad() {
        addon.onLoad();
        assertNotNull(addon.getSettings());
        assertTrue(new File("addons/Brix", "config.yml").exists());
    }

    @Test
    void testOnLoadSettingsDefaults() {
        addon.onLoad();
        Settings s = addon.getSettings();
        assertNotNull(s);
        assertEquals("Brix", s.getFriendlyName());
        assertEquals("brix_world", s.getWorldName());
        assertEquals(400, s.getIslandDistance());
        assertEquals(50, s.getIslandProtectionRange());
        assertEquals(5, s.getIslandHeight());
        assertTrue(s.isNetherGenerate());
        assertTrue(s.isEndGenerate());
    }

    @Test
    void testOnLoadRegistersCommands() {
        addon.onLoad();
        assertTrue(addon.getPlayerCommand().isPresent());
        assertTrue(addon.getAdminCommand().isPresent());
    }

    @Test
    void testOnEnableRegistersListener() {
        addon.onLoad();
        addon.onEnable();
        verify(am).registerListener(addon, addon);
    }

    @Test
    void testOnDisable() {
        // onDisable() is a no-op — must not throw
        addon.onDisable();
        assertNotNull(addon);
    }

    @Test
    void testOnReload() {
        addon.onLoad();
        addon.onReload();
        assertNotNull(addon.getSettings());
    }

    @Test
    void testGetWorldSettings() {
        addon.onLoad();
        assertEquals(addon.getSettings(), addon.getWorldSettings());
    }

    @Test
    void testGetDefaultWorldGenerator() {
        addon.onLoad();
        assertNotNull(addon.getDefaultWorldGenerator("brix_world", ""));
        assertTrue(addon.getDefaultWorldGenerator("brix_world", "") instanceof ChunkGeneratorWorld);
    }

    @Test
    void testCreateWorlds() {
        addon.onLoad();
        addon.createWorlds();
        assertNotNull(addon.getOverWorld());
        assertNotNull(addon.getNetherWorld());
        assertNotNull(addon.getEndWorld());
    }

    @Test
    void testSaveWorldSettings() {
        addon.onLoad();
        // Must not throw and must keep the loaded settings
        addon.saveWorldSettings();
        assertNotNull(addon.getSettings());
    }

    @Test
    void testAllLoaded() {
        addon.onLoad();
        addon.allLoaded();
        assertNotNull(addon.getSettings());
    }
}
