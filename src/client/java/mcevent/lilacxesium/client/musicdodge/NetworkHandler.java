package mcevent.lilacxesium.client.musicdodge;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * 网络处理器 - 负责接收和处理来自服务器的Plugin Message
 * 使用1.21.4的CustomPayload API
 */
public class NetworkHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("NetworkHandler");
    
    private Consumer<List<AttackDataDecoder.AttackData>> attackDataHandler;
    private boolean isRegistered = false;
    
    /**
     * 注册网络处理器
     */
    public void register() {
        if (isRegistered) {
            return;
        }
        
        try {
            // 注册CustomPayload接收器
            ClientPlayNetworking.registerGlobalReceiver(MusicDodgePayload.TYPE, (payload, context) -> {
                // 获取攻击数据
                String encodedData = payload.data();
                
                // 立即处理数据，减少延迟
                try {
                    handleAttackData(encodedData);
                } catch (Exception e) {
                    LOGGER.error("Failed to process attack data: {}", e.getMessage(), e);
                    e.printStackTrace();
                }
            });
            
            isRegistered = true;
            
        } catch (Exception e) {
            LOGGER.error("Failed to register MusicDodge network handler: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }
    
    /**
     * 取消注册网络处理器
     */
    public void unregister() {
        if (!isRegistered) {
            return;
        }
        
        ClientPlayNetworking.unregisterGlobalReceiver(MusicDodgePayload.ID);
        isRegistered = false;
    }
    
    /**
     * 设置攻击数据处理器
     */
    public void setAttackDataHandler(Consumer<List<AttackDataDecoder.AttackData>> handler) {
        this.attackDataHandler = handler;
    }
    
    /**
     * 处理接收到的攻击数据
     */
    private void handleAttackData(String encodedData) {
        if (attackDataHandler == null) {
            return;
        }
        
        try {
            // 解码攻击数据
            List<AttackDataDecoder.AttackData> attacks = AttackDataDecoder.decode(encodedData);
            
            // 调用处理器
            attackDataHandler.accept(attacks);
            
        } catch (Exception e) {
            LOGGER.error("Failed to handle attack data: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }
    
    /**
     * 检查是否已注册
     */
    public boolean isRegistered() {
        return isRegistered;
    }
}