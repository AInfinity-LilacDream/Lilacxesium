package mcevent.lilacxesium.client.musicdodge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 粒子渲染器 - 负责将攻击数据转换为客户端粒子效果
 */
public class ParticleRenderer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("ParticleRenderer");
    
    private final MinecraftClient client;
    
    public ParticleRenderer() {
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * 渲染攻击数据列表
     */
    public void renderAttacks(List<AttackDataDecoder.AttackData> attacks) {
        ClientWorld world = client.world;
        if (world == null) {
            LOGGER.warn("ParticleRenderer: World is null, cannot render");
            return;
        }
        
        for (AttackDataDecoder.AttackData attack : attacks) {
            renderAttack(attack, world);
        }
    }
    
    /**
     * 渲染单个攻击
     */
    private void renderAttack(AttackDataDecoder.AttackData attack, ClientWorld world) {
        // 根据攻击相位确定粒子效果
        DustParticleEffect particleEffect = getParticleEffect(attack.phase);
        
        // 根据攻击类型渲染粒子
        switch (attack.type) {
            case LASER:
                renderLaser((AttackDataDecoder.LaserParameters) attack.parameters, world, particleEffect);
                break;
            case SQUARE_RING:
                renderSquareRing((AttackDataDecoder.SquareRingParameters) attack.parameters, world, particleEffect);
                break;
            case SPIN:
                renderSpin((AttackDataDecoder.SpinParameters) attack.parameters, world, particleEffect);
                break;
            case CIRCLE:
                renderCircle((AttackDataDecoder.CircleParameters) attack.parameters, world, particleEffect);
                break;
            case WALL:
                renderWall((AttackDataDecoder.WallParameters) attack.parameters, world, particleEffect);
                break;
        }
    }
    
    /**
     * 获取粒子效果
     */
    private DustParticleEffect getParticleEffect(AttackDataDecoder.AttackPhase phase) {
        return switch (phase) {
            case ALERT ->
                // 灰色 - RGB (0.5, 0.5, 0.5)
                    new DustParticleEffect(0x808080, 1.0f);
            case ATTACK ->
                // 红色 - RGB (1.0, 0.0, 0.0)
                    new DustParticleEffect(0xFF0000, 1.0f);
        };
    }
    
    /**
     * 渲染激光攻击
     */
    private void renderLaser(AttackDataDecoder.LaserParameters params, ClientWorld world, DustParticleEffect particleEffect) {
        Vec3d start = new Vec3d(params.x1, params.y1, params.z1);
        Vec3d end = new Vec3d(params.x2, params.y2, params.z2);
        
        Vec3d direction = end.subtract(start);
        double distance = direction.length();
        direction = direction.normalize();
        
        double step = 0.5; // 每0.5格一个粒子
        
        for (double d = 0; d < distance; d += step) {
            Vec3d currentPos = start.add(direction.multiply(d));
            
            // 检查是否在固体方块内
            if (!isPositionSolid(world, currentPos)) {
                // 生成2格高的激光
                for (int y = 0; y < 2; y++) {
                    Vec3d particlePos = currentPos.add(0, y, 0);
                    spawnParticle(world, particlePos, particleEffect);
                }
            }
        }
    }
    
    /**
     * 渲染正方形环攻击
     */
    private void renderSquareRing(AttackDataDecoder.SquareRingParameters params, ClientWorld world, DustParticleEffect particleEffect) {
        Vec3d center = new Vec3d(params.centerX, params.centerY, params.centerZ);
        
        // 渲染从内半径到外半径的所有环
        for (int radius = params.innerRadius; radius <= params.outerRadius; radius++) {
            renderSquareBorder(world, center, radius, particleEffect);
        }
    }
    
    /**
     * 渲染正方形边框
     */
    private void renderSquareBorder(ClientWorld world, Vec3d center, int radius, DustParticleEffect particleEffect) {
        // 上边
        for (int x = -radius; x <= radius; x++) {
            Vec3d pos = center.add(x, 0, -radius);
            spawnParticleIfNotSolid(world, pos, particleEffect);
        }
        
        // 下边
        for (int x = -radius; x <= radius; x++) {
            Vec3d pos = center.add(x, 0, radius);
            spawnParticleIfNotSolid(world, pos, particleEffect);
        }
        
        // 左边（排除角落）
        for (int z = -radius + 1; z < radius; z++) {
            Vec3d pos = center.add(-radius, 0, z);
            spawnParticleIfNotSolid(world, pos, particleEffect);
        }
        
        // 右边（排除角落）
        for (int z = -radius + 1; z < radius; z++) {
            Vec3d pos = center.add(radius, 0, z);
            spawnParticleIfNotSolid(world, pos, particleEffect);
        }
    }
    
    /**
     * 渲染旋转攻击
     */
    private void renderSpin(AttackDataDecoder.SpinParameters params, ClientWorld world, DustParticleEffect particleEffect) {
        Vec3d center = new Vec3d(params.centerX, params.centerY, params.centerZ);
        double angleStep = 360.0 / params.rayCount;
        
        for (int i = 0; i < params.rayCount; i++) {
            double angle = Math.toRadians(params.angleOffset + (i * angleStep));
            renderSpinRay(world, center, angle, params.maxDistance, particleEffect);
        }
    }
    
    /**
     * 渲染旋转攻击的单条射线
     */
    private void renderSpinRay(ClientWorld world, Vec3d center, double angle, int maxDistance, DustParticleEffect particleEffect) {
        Vec3d direction = new Vec3d(Math.cos(angle), 0, Math.sin(angle));
        
        for (int distance = 0; distance < maxDistance; distance++) {
            Vec3d currentPos = center.add(direction.multiply(distance));
            
            // 检查是否碰到固体方块
            if (isPositionSolid(world, currentPos)) {
                break;
            }
            
            // 生成2格高的射线
            for (int y = 0; y < 2; y++) {
                Vec3d particlePos = currentPos.add(0, y, 0);
                spawnParticle(world, particlePos, particleEffect);
            }
        }
    }
    
    /**
     * 渲染圆形攻击
     */
    private void renderCircle(AttackDataDecoder.CircleParameters params, ClientWorld world, DustParticleEffect particleEffect) {
        Vec3d center = new Vec3d(params.centerX, params.centerY, params.centerZ);
        
        // 根据半径确定圆周上的点数
        int points = Math.max(16, (int)(params.radius * 16));
        
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.x + params.radius * Math.cos(angle);
            double z = center.z + params.radius * Math.sin(angle);
            
            Vec3d pos = new Vec3d(x, center.y, z);
            spawnParticleIfNotSolid(world, pos, particleEffect);
        }
    }
    
    /**
     * 渲染墙攻击
     */
    private void renderWall(AttackDataDecoder.WallParameters params, ClientWorld world, DustParticleEffect particleEffect) {
        // MusicDodge场地范围
        final int FIELD_MIN_X = -7;
        final int FIELD_MAX_X = 35;
        final int FIELD_MIN_Z = -46;
        final int FIELD_MAX_Z = -4;
        final int FIELD_Y = -60;
        final int WALL_HEIGHT = 6; // 墙高度设为6格
        
        
        if (params.direction.equals("x")) {
            // 垂直于X轴的墙（从上到下）
            // 将游戏坐标转换为世界坐标
            double worldX = FIELD_MIN_X + params.position;
            for (int z = FIELD_MIN_Z; z <= FIELD_MAX_Z; z++) {
                for (int y = 0; y < WALL_HEIGHT; y++) {
                    Vec3d pos = new Vec3d(worldX, FIELD_Y + y, z);
                    spawnParticleIfNotSolid(world, pos, particleEffect);
                }
            }
        } else if (params.direction.equals("y") || params.direction.equals("z")) {
            // 垂直于Z轴的墙（从左到右）
            // 将游戏坐标转换为世界坐标
            double worldZ = FIELD_MIN_Z + params.position;
            for (int x = FIELD_MIN_X; x <= FIELD_MAX_X; x++) {
                for (int y = 0; y < WALL_HEIGHT; y++) {
                    Vec3d pos = new Vec3d(x, FIELD_Y + y, worldZ);
                    spawnParticleIfNotSolid(world, pos, particleEffect);
                }
            }
        }
    }
    
    /**
     * 在非固体方块位置生成粒子
     */
    private void spawnParticleIfNotSolid(ClientWorld world, Vec3d pos, DustParticleEffect particleEffect) {
        if (!isPositionSolid(world, pos)) {
            spawnParticle(world, pos, particleEffect);
        }
    }
    
    /**
     * 生成粒子
     */
    private void spawnParticle(ClientWorld world, Vec3d pos, DustParticleEffect particleEffect) {
        world.addParticle(particleEffect, pos.x, pos.y, pos.z, 0, 0, 0);
    }
    
    /**
     * 检查位置是否为固体方块
     */
    private boolean isPositionSolid(ClientWorld world, Vec3d pos) {
        try {
            net.minecraft.util.math.BlockPos blockPos = new net.minecraft.util.math.BlockPos(
                (int)Math.floor(pos.x), (int)Math.floor(pos.y), (int)Math.floor(pos.z)
            );
            // 使用新的API替代已弃用的isSolid()
            return world.getBlockState(blockPos).isSolidBlock(world, blockPos);
        } catch (Exception e) {
            return true; // 安全起见，遇到错误时认为是固体
        }
    }
}