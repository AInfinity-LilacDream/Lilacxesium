package mcevent.lilacxesium.client.musicdodge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 攻击数据解码器 - 客户端版本
 * 负责解码从服务端接收的攻击数据
 */
public class AttackDataDecoder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("AttackDecoder");
    
    /**
     * 攻击类型枚举
     */
    public enum AttackType {
        LASER,      // 直线激光
        SQUARE_RING, // 正方形环
        SPIN,       // 旋转激光
        CIRCLE,     // 圆形攻击
        WALL        // 墙攻击
    }
    
    /**
     * 攻击相位枚举
     */
    public enum AttackPhase {
        ALERT,      // 预警阶段（灰色）
        ATTACK      // 攻击阶段（红色）
    }
    
    /**
     * 攻击数据类
     */
    public static class AttackData {
        public final AttackType type;
        public final AttackPhase phase;
        public final int ticksRemaining;
        public final AttackParameters parameters;
        
        public AttackData(AttackType type, AttackPhase phase, int ticksRemaining, AttackParameters parameters) {
            this.type = type;
            this.phase = phase;
            this.ticksRemaining = ticksRemaining;
            this.parameters = parameters;
        }
        
        /**
         * 生成攻击的唯一标识符（基于类型和参数）
         */
        public String getUniqueId() {
            return type.toString() + "_" + parameters.toString();
        }
    }
    
    /**
     * 攻击参数基类
     */
    public abstract static class AttackParameters {
        public abstract AttackType getType();
    }
    
    /**
     * 激光攻击参数
     */
    public static class LaserParameters extends AttackParameters {
        public final double x1, y1, z1, x2, y2, z2;
        
        public LaserParameters(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.x1 = x1; this.y1 = y1; this.z1 = z1;
            this.x2 = x2; this.y2 = y2; this.z2 = z2;
        }
        
        @Override
        public AttackType getType() {
            return AttackType.LASER;
        }
    }
    
    /**
     * 正方形环攻击参数
     */
    public static class SquareRingParameters extends AttackParameters {
        public final double centerX, centerY, centerZ;
        public final int innerRadius, outerRadius;
        
        public SquareRingParameters(double centerX, double centerY, double centerZ, int innerRadius, int outerRadius) {
            this.centerX = centerX; this.centerY = centerY; this.centerZ = centerZ;
            this.innerRadius = innerRadius; this.outerRadius = outerRadius;
        }
        
        @Override
        public AttackType getType() {
            return AttackType.SQUARE_RING;
        }
    }
    
    /**
     * 旋转攻击参数
     */
    public static class SpinParameters extends AttackParameters {
        public final double centerX, centerY, centerZ;
        public final int rayCount, maxDistance;
        public final double angleOffset;
        
        public SpinParameters(double centerX, double centerY, double centerZ, int rayCount, double angleOffset, int maxDistance) {
            this.centerX = centerX; this.centerY = centerY; this.centerZ = centerZ;
            this.rayCount = rayCount; this.angleOffset = angleOffset; this.maxDistance = maxDistance;
        }
        
        @Override
        public AttackType getType() {
            return AttackType.SPIN;
        }
    }
    
    /**
     * 圆形攻击参数
     */
    public static class CircleParameters extends AttackParameters {
        public final double centerX, centerY, centerZ, radius;
        
        public CircleParameters(double centerX, double centerY, double centerZ, double radius) {
            this.centerX = centerX; this.centerY = centerY; this.centerZ = centerZ;
            this.radius = radius;
        }
        
        @Override
        public AttackType getType() {
            return AttackType.CIRCLE;
        }
    }
    
    /**
     * 墙攻击参数
     */
    public static class WallParameters extends AttackParameters {
        public final String direction;
        public final double position;
        
        public WallParameters(String direction, double position) {
            this.direction = direction;
            this.position = position;
        }
        
        @Override
        public AttackType getType() {
            return AttackType.WALL;
        }
    }
    
    /**
     * 解码攻击数据字符串
     * 格式：攻击类型|参数|颜色|剩余时间#攻击类型|参数|颜色|剩余时间
     */
    public static List<AttackData> decode(String encodedData) {
        List<AttackData> attacks = new ArrayList<>();
        
        if (encodedData == null || encodedData.trim().isEmpty()) {
            return attacks;
        }
        
        String[] attackStrings = encodedData.split("#");
        for (String attackString : attackStrings) {
            try {
                AttackData attack = decodeAttack(attackString);
                if (attack != null) {
                    attacks.add(attack);
                    
                    // 计算预警和攻击小节数（假设每小节20tick）
                    int totalBeats = attack.ticksRemaining / 20;
                    int alertBeats, attackBeats;
                    
                    if (attack.phase == AttackPhase.ALERT) {
                        alertBeats = totalBeats;
                        attackBeats = 0;
                    } else {
                        // 攻击阶段
                        alertBeats = 0;
                        attackBeats = totalBeats;
                    }
                    
                    // 攻击数据解析成功
                }
            } catch (Exception e) {
                // 忽略格式错误的攻击数据，继续处理其他数据
            }
        }
        
        return attacks;
    }
    
    /**
     * 解码单个攻击数据
     */
    private static AttackData decodeAttack(String attackString) {
        String[] parts = attackString.split("\\|");
        if (parts.length != 4) {
            return null;
        }
        
        try {
            AttackType type = AttackType.valueOf(parts[0]);
            String parametersString = parts[1];
            AttackPhase phase = parts[2].equals("GRAY") ? AttackPhase.ALERT : AttackPhase.ATTACK;
            int ticksRemaining = Integer.parseInt(parts[3]);
            
            AttackParameters parameters = parseParameters(type, parametersString);
            if (parameters == null) {
                return null;
            }
            
            return new AttackData(type, phase, ticksRemaining, parameters);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 解析攻击参数
     */
    private static AttackParameters parseParameters(AttackType type, String parametersString) {
        String[] params = parametersString.split(",");
        
        switch (type) {
            case LASER:
                if (params.length == 6) {
                    return new LaserParameters(
                        Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2]),
                        Double.parseDouble(params[3]), Double.parseDouble(params[4]), Double.parseDouble(params[5])
                    );
                }
                break;
                
            case SQUARE_RING:
                if (params.length == 5) {
                    return new SquareRingParameters(
                        Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2]),
                        Integer.parseInt(params[3]), Integer.parseInt(params[4])
                    );
                }
                break;
                
            case SPIN:
                if (params.length == 6) {
                    return new SpinParameters(
                        Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2]),
                        Integer.parseInt(params[3]), Double.parseDouble(params[4]), Integer.parseInt(params[5])
                    );
                }
                break;
                
            case CIRCLE:
                if (params.length == 4) {
                    return new CircleParameters(
                        Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2]),
                        Double.parseDouble(params[3])
                    );
                }
                break;
                
            case WALL:
                if (params.length == 2) {
                    return new WallParameters(params[0], Double.parseDouble(params[1]));
                }
                break;
        }
        
        return null;
    }
}