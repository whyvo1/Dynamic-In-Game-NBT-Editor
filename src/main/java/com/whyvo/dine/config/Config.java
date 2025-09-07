package com.whyvo.dine.config;

import com.whyvo.dine.DINE;
import net.minecraft.client.MinecraftClient;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Properties;

public class Config {
    private static final String DEFAULT_TRY_PAUSE_GAME = "defaultTryPauseGame";
    private static final String ENTITY_DEFAULT_FROM_SERVER = "entityDefaultFromServer";
    private static final String BLOCK_ENTITY_DEFAULT_FROM_SERVER = "blockEntityDefaultFromServer";
    private static final String CHECK_PERMISSION_WHEN_FROM_SERVER = "checkPermissionWhenFromServer";
    private static final String LOG_SENT_COMMAND = "logSentCommand";
    private static final String DATA_COMMAND = "dataCommand";

    private static final Path CONFIG_PATH = MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("dine.prop");
    private static final Properties defaultProperties;

    public static boolean defaultTryPauseGame;
    public static boolean entityDefaultFromServer;
    public static boolean blockEntityDefaultFromServer;
    public static boolean checkPermissionWhenFromServer;
    public static boolean logSentCommand;
    public static String dataCommand;

    static {
        defaultTryPauseGame = false;
        entityDefaultFromServer = false;
        blockEntityDefaultFromServer = false;
        checkPermissionWhenFromServer = true;
        logSentCommand = false;
        dataCommand = "data";

        defaultProperties = new Properties();
        defaultProperties.put(DEFAULT_TRY_PAUSE_GAME, defaultTryPauseGame);
        defaultProperties.put(ENTITY_DEFAULT_FROM_SERVER, entityDefaultFromServer);
        defaultProperties.put(BLOCK_ENTITY_DEFAULT_FROM_SERVER, blockEntityDefaultFromServer);
        defaultProperties.put(CHECK_PERMISSION_WHEN_FROM_SERVER, checkPermissionWhenFromServer);
        defaultProperties.put(LOG_SENT_COMMAND, logSentCommand);
        defaultProperties.put(DATA_COMMAND, dataCommand);
    }

    public static void refreshProperties() {
        Properties prop = new Properties(defaultProperties);

        try (FileInputStream inputStream = new FileInputStream(CONFIG_PATH.toFile())) {
            prop.load(inputStream);
            defaultTryPauseGame = Boolean.parseBoolean(prop.getProperty(DEFAULT_TRY_PAUSE_GAME));
            entityDefaultFromServer = Boolean.parseBoolean(prop.getProperty(ENTITY_DEFAULT_FROM_SERVER));
            blockEntityDefaultFromServer = Boolean.parseBoolean(prop.getProperty(BLOCK_ENTITY_DEFAULT_FROM_SERVER));
            checkPermissionWhenFromServer = Boolean.parseBoolean(prop.getProperty(CHECK_PERMISSION_WHEN_FROM_SERVER));
            logSentCommand = Boolean.parseBoolean(prop.getProperty(LOG_SENT_COMMAND));
            dataCommand = prop.getProperty(DATA_COMMAND);

            DINE.LOGGER.info("Refreshed config");
        } catch (Exception e) {
            writeToFile();
            refreshProperties();
        }
    }

    public static void writeToFile() {
        Properties prop = new Properties();

        try (FileOutputStream outputStream = new FileOutputStream(CONFIG_PATH.toFile())) {
            prop.setProperty(DEFAULT_TRY_PAUSE_GAME, Boolean.toString(defaultTryPauseGame));
            prop.setProperty(ENTITY_DEFAULT_FROM_SERVER, Boolean.toString(entityDefaultFromServer));
            prop.setProperty(BLOCK_ENTITY_DEFAULT_FROM_SERVER, Boolean.toString(blockEntityDefaultFromServer));
            prop.setProperty(CHECK_PERMISSION_WHEN_FROM_SERVER, Boolean.toString(checkPermissionWhenFromServer));
            prop.setProperty(LOG_SENT_COMMAND, Boolean.toString(logSentCommand));
            prop.setProperty(DATA_COMMAND, dataCommand);

            prop.store(outputStream, null);

            DINE.LOGGER.info("Wrote config to file");
        } catch (Exception e) {
            DINE.LOGGER.error("Error writing config to file", e);
        }
    }

}
