package mcevent.lilacxesium.client;

import mcevent.lilacxesium.client.musicdodge.MusicDodgeClientManager;
import mcevent.lilacxesium.client.musicdodge.MusicDodgePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lilacxesium客户端Mod入口点
 * 负责初始化所有客户端功能
 */
public class LilacxesiumClient implements ClientModInitializer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("Lilacxesium");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Lilacxesium Client Mod...");
        
        // 注册Payload类型
        try {
            LOGGER.info("Registering MusicDodgePayload type...");
            PayloadTypeRegistry.playS2C().register(MusicDodgePayload.TYPE, MusicDodgePayload.CODEC);
            LOGGER.info("MusicDodgePayload type registered successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to register MusicDodgePayload type: {}", e.getMessage(), e);
        }
        
        // 初始化MusicDodge客户端管理器
        initializeMusicDodge();
        
        LOGGER.info("Lilacxesium Client Mod initialized successfully!");
    }
    
    /**
     * 初始化MusicDodge客户端功能
     */
    private void initializeMusicDodge() {
        try {
            MusicDodgeClientManager manager = MusicDodgeClientManager.getInstance();
            manager.initialize();
            
            LOGGER.info("MusicDodge client features initialized");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize MusicDodge client features: " + e.getMessage(), e);
        }
    }
}
