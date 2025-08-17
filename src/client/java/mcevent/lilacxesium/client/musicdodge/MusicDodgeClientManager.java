package mcevent.lilacxesium.client.musicdodge;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MusicDodge客户端管理器 - 系统的核心控制器
 * 负责协调网络通信、数据处理和粒子渲染
 */
public class MusicDodgeClientManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("MusicDodgeClient");
    private static MusicDodgeClientManager instance;
    
    private final NetworkHandler networkHandler;
    private final ParticleRenderer particleRenderer;
    private final MinecraftClient client;
    
    private boolean isEnabled = false;
    private boolean isInGame = false;
    private List<AttackDataDecoder.AttackData> currentAttacks;
    
    /**
     * 私有构造函数（单例模式）
     */
    private MusicDodgeClientManager() {
        this.client = MinecraftClient.getInstance();
        this.networkHandler = new NetworkHandler();
        this.particleRenderer = new ParticleRenderer();
        
        // 设置攻击数据处理器
        this.networkHandler.setAttackDataHandler(this::handleAttackData);
        
        // 注册客户端事件
        registerClientEvents();
    }
    
    /**
     * 获取单例实例
     */
    public static MusicDodgeClientManager getInstance() {
        if (instance == null) {
            instance = new MusicDodgeClientManager();
        }
        return instance;
    }
    
    /**
     * 初始化管理器
     */
    public void initialize() {
        if (isEnabled) {
            return;
        }
        
        // 注册网络处理器
        networkHandler.register();
        isEnabled = true;
    }
    
    /**
     * 停止管理器
     */
    public void shutdown() {
        if (!isEnabled) {
            return;
        }
        
        // 取消注册网络处理器
        networkHandler.unregister();
        
        // 清除当前攻击数据
        currentAttacks = null;
        isInGame = false;
        isEnabled = false;
    }
    
    /**
     * 注册客户端事件
     */
    private void registerClientEvents() {
        // 客户端关闭时清理
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> shutdown());
        
        // 每tick检查游戏状态
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }
    
    /**
     * 客户端tick事件处理
     */
    private void onClientTick(MinecraftClient client) {
        if (!isEnabled || client.world == null || client.player == null) {
            return;
        }
        
        // 检查是否在MusicDodge世界中
        updateGameState();
        
        // 立即渲染攻击粒子，无冷却延迟
        if (isInGame && currentAttacks != null && !currentAttacks.isEmpty()) {
            particleRenderer.renderAttacks(currentAttacks);
        }
    }
    
    /**
     * 更新游戏状态
     */
    private void updateGameState() {
        if (client.world == null) {
            isInGame = false;
            return;
        }
        
        // 检查是否在MusicDodge世界中
        String worldName = client.world.getRegistryKey().getValue().toString();
        boolean shouldBeInGame = worldName.contains("musicdodge");
        
        if (shouldBeInGame != isInGame) {
            isInGame = shouldBeInGame;
            
            if (!isInGame) {
                // 离开MusicDodge世界时清除攻击数据
                currentAttacks = null;
            }
        }
    }
    
    /**
     * 处理接收到的攻击数据
     */
    private void handleAttackData(List<AttackDataDecoder.AttackData> attacks) {
        if (!isEnabled) {
            return;
        }
        
        // 存储攻击数据
        this.currentAttacks = attacks;
        
        
    }
    
    
    /**
     * 获取当前攻击数据（只读）
     */
    public List<AttackDataDecoder.AttackData> getCurrentAttacks() {
        return currentAttacks;
    }
    
    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * 检查是否在游戏中
     */
    public boolean isInGame() {
        return isInGame;
    }
    
    /**
     * 获取网络处理器（用于测试或高级用法）
     */
    public NetworkHandler getNetworkHandler() {
        return networkHandler;
    }
    
    /**
     * 获取粒子渲染器（用于测试或高级用法）
     */
    public ParticleRenderer getParticleRenderer() {
        return particleRenderer;
    }
    
    /**
     * 手动设置游戏状态（用于测试）
     */
    public void setInGame(boolean inGame) {
        this.isInGame = inGame;
    }
    
    /**
     * 手动清除攻击数据
     */
    public void clearAttacks() {
        this.currentAttacks = null;
    }
}