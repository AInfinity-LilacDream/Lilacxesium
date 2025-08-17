package mcevent.lilacxesium.client.musicdodge;

/**
 * 客户端配置管理器
 * 管理MusicDodge客户端的各种设置
 */
public class ClientConfig {
    
    private static ClientConfig instance;
    
    // 调试选项
    private boolean debugMode = false;
    private boolean showParticleCount = false;
    private boolean logNetworkData = false;
    
    // 渲染选项
    private float particleScale = 1.0f;
    private boolean enableParticleOptimization = true;
    private int maxParticlesPerFrame = 1000;
    
    // 网络选项
    private boolean enableNetworkCompression = false;
    private int networkTimeout = 5000; // ms
    
    private ClientConfig() {
        loadConfig();
    }
    
    public static ClientConfig getInstance() {
        if (instance == null) {
            instance = new ClientConfig();
        }
        return instance;
    }
    
    /**
     * 加载配置（从系统属性或配置文件）
     */
    private void loadConfig() {
        // 从系统属性加载调试选项
        debugMode = Boolean.getBoolean("musicdodge.debug");
        showParticleCount = Boolean.getBoolean("musicdodge.debug.particles");
        logNetworkData = Boolean.getBoolean("musicdodge.debug.network");
        
        // 从系统属性加载渲染选项
        String scaleStr = System.getProperty("musicdodge.particle.scale");
        if (scaleStr != null) {
            try {
                particleScale = Float.parseFloat(scaleStr);
                particleScale = Math.max(0.1f, Math.min(5.0f, particleScale)); // 限制范围
            } catch (NumberFormatException e) {
                particleScale = 1.0f;
            }
        }
        
        String maxParticlesStr = System.getProperty("musicdodge.particle.max");
        if (maxParticlesStr != null) {
            try {
                maxParticlesPerFrame = Integer.parseInt(maxParticlesStr);
                maxParticlesPerFrame = Math.max(100, Math.min(10000, maxParticlesPerFrame));
            } catch (NumberFormatException e) {
                maxParticlesPerFrame = 1000;
            }
        }
        
        enableParticleOptimization = !Boolean.getBoolean("musicdodge.particle.noopt");
    }
    
    // Getter方法
    public boolean isDebugMode() { return debugMode; }
    public boolean isShowParticleCount() { return showParticleCount; }
    public boolean isLogNetworkData() { return logNetworkData; }
    public float getParticleScale() { return particleScale; }
    public boolean isEnableParticleOptimization() { return enableParticleOptimization; }
    public int getMaxParticlesPerFrame() { return maxParticlesPerFrame; }
    public boolean isEnableNetworkCompression() { return enableNetworkCompression; }
    public int getNetworkTimeout() { return networkTimeout; }
    
    // Setter方法（运行时修改）
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    public void setShowParticleCount(boolean showParticleCount) { this.showParticleCount = showParticleCount; }
    public void setLogNetworkData(boolean logNetworkData) { this.logNetworkData = logNetworkData; }
    public void setParticleScale(float particleScale) { 
        this.particleScale = Math.max(0.1f, Math.min(5.0f, particleScale)); 
    }
    public void setEnableParticleOptimization(boolean enableParticleOptimization) { 
        this.enableParticleOptimization = enableParticleOptimization; 
    }
    public void setMaxParticlesPerFrame(int maxParticlesPerFrame) { 
        this.maxParticlesPerFrame = Math.max(100, Math.min(10000, maxParticlesPerFrame)); 
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        loadConfig();
    }
    
    /**
     * 打印当前配置
     */
    public void printConfig() {
        // 配置信息已移除以减少日志输出
    }
}