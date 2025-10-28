package dev.chariyatham;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle; // <<< ใช้ Rectangle
import javafx.scene.Node;            // <<< คืนค่า Node
import java.util.List;

/**
 * นี่คือ Class ผู้เล่น (เวอร์ชัน บทที่ 10 - ใช้ Rectangle)
 */
public class Player {

    // --- 1. "รูปร่าง" (เป็น Rectangle) ---
    private Rectangle shape;

    // --- (สถานะฟิสิกส์, ชาร์จ, ชนะ, ลื่น ... เหมือนเดิมเป๊ะ) ---
    private double velocityX = 0;
    private double velocityY = 0;
    private boolean onGround = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private boolean isChargingJump = false;
    private double chargeTimer = 0.0;
    private int currentChargeLevel = 0;
    private boolean hasWon = false;
    private boolean onSlipperyGround = false;
    private static final double FRICTION_DECEL = 500.0;
    private static final double GRAVITY = 1000.0;
    private static final double MAX_CHARGE_TIME = 1.0;
    private static final double JUMP_POWER_Y_PER_LEVEL = -180.0;
    private static final double JUMP_POWER_X_PER_LEVEL = 100.0;

    // --- 2. ค่าคงที่ "ขนาด" (60x80) ---
    private static final int PLAYER_WIDTH = 60;
    private static final int PLAYER_HEIGHT = 80;

    // (ค่าคงที่โลก ... เหมือนเดิม)
    private static final int WORLD_HEIGHT = 1800;
    private static final int WINDOW_WIDTH = 800;
    private static final double GROUND_LEVEL = WORLD_HEIGHT - PLAYER_HEIGHT;

    /**
     * Constructor (ตัวสร้าง) - สร้าง Rectangle
     */
    public Player(double startX, double startY) {
        this.shape = new Rectangle(PLAYER_WIDTH, PLAYER_HEIGHT, Color.ORANGE); // <<< สร้าง Rectangle
        this.shape.setX(startX);
        this.shape.setY(startY);
        this.onGround = true;
    }

    /**
     * นี่คือ "สมอง" ของผู้เล่น
     */
    public void update(double deltaTime, List<Platform> allPlatforms) {

        // --- อัปเดตการกลับทิศ (Flip) ---
        // (Rectangle ไม่มี setScaleX เราเลยไม่ต้องทำอะไรตรงนี้)

        // --- (โค้ดอัปเดตชาร์จ, แรงเสียดทาน, ฟิสิกส์ ... เหมือนเดิมเป๊ะ) ---

        // (อัปเดตชาร์จ)
        if (isChargingJump && onGround) {
            chargeTimer += deltaTime;
            if (chargeTimer > MAX_CHARGE_TIME) chargeTimer = MAX_CHARGE_TIME;
            if (chargeTimer >= MAX_CHARGE_TIME) currentChargeLevel = 5;
            else if (chargeTimer >= 0.8) currentChargeLevel = 4;
            else if (chargeTimer >= 0.6) currentChargeLevel = 3;
            else if (chargeTimer >= 0.4) currentChargeLevel = 2;
            else if (chargeTimer >= 0.2) currentChargeLevel = 1;
            else currentChargeLevel = 0;
        } else {
            chargeTimer = 0.0;
            currentChargeLevel = 0;
        }

        // (แรงเสียดทาน)
        if (onGround && onSlipperyGround) {
            if (velocityX > 0) {
                velocityX -= FRICTION_DECEL * deltaTime;
                if (velocityX < 0) velocityX = 0;
            } else if (velocityX < 0) {
                velocityX += FRICTION_DECEL * deltaTime;
                if (velocityX > 0) velocityX = 0;
            }
         }

        // (ฟิสิกส์ - ใช้ .getWidth() / .getHeight() ของ Rectangle)
        if (!onGround) {

            // แกน X
            double oldX = this.shape.getX();
            this.shape.setX(this.shape.getX() + velocityX * deltaTime);

            for (Platform platform : allPlatforms) {
                boolean isOverlappingY =
                    this.shape.getY() + this.shape.getHeight() > platform.getY() && // <<< ใช้ getHeight()
                    this.shape.getY() < platform.getY() + platform.getHeight();

                if (isOverlappingY) {
                    if (velocityX > 0 &&
                        oldX + this.shape.getWidth() <= platform.getX() && // <<< ใช้ getWidth()
                        this.shape.getX() + this.shape.getWidth() >= platform.getX()) { // <<< ใช้ getWidth()
                        this.shape.setX(platform.getX() - this.shape.getWidth()); // <<< ใช้ getWidth()
                        velocityX = 0;
                        break;
                    }
                    else if (velocityX < 0 &&
                             oldX >= platform.getX() + platform.getWidth() &&
                             this.shape.getX() <= platform.getX() + platform.getWidth()) {
                        this.shape.setX(platform.getX() + platform.getWidth());
                        velocityX = 0;
                        break;
                    }
                }
            }

            // แกน Y
            double oldY = this.shape.getY();
            velocityY += GRAVITY * deltaTime;
            this.shape.setY(this.shape.getY() + velocityY * deltaTime);

            for (Platform platform : allPlatforms) {
                boolean isOverlappingX =
                    this.shape.getX() + this.shape.getWidth() > platform.getX() && // <<< ใช้ getWidth()
                    this.shape.getX() < platform.getX() + platform.getWidth();

                if (isOverlappingX) {
                    // (เคสเหยียบ)
                    if (velocityY > 0 &&
                        oldY + this.shape.getHeight() <= platform.getY() && // <<< ใช้ getHeight()
                        this.shape.getY() + this.shape.getHeight() >= platform.getY()) // <<< ใช้ getHeight()
                    {
                        this.shape.setY(platform.getY() - this.shape.getHeight()); // <<< ใช้ getHeight()
                        velocityY = 0;
                        onGround = true;
                        // (เช็คแท่นลื่น/ชนะ ... เหมือนเดิม)
                        if (platform.isSlippery()) { this.onSlipperyGround = true; }
                        else { this.onSlipperyGround = false; this.velocityX = 0; }
                        if (platform.isVictoryPlatform()) { this.hasWon = true; }
                        break;
                    }
                    // (เคสหัวชน ... เหมือนเดิม)
                    else if (velocityY < 0 &&
                             oldY >= platform.getY() + platform.getHeight() &&
                             this.shape.getY() <= platform.getY() + platform.getHeight()) {
                        this.shape.setY(platform.getY() + platform.getHeight());
                        velocityY = 0;
                        break;
                    }
                }
            }
        } // สิ้นสุด if (!onGround)

        // (ชนพื้น/ขอบจอ - ใช้ .getWidth())
        if (!onGround && this.shape.getY() >= GROUND_LEVEL) {
            this.shape.setY(GROUND_LEVEL);
            velocityY = 0;
            velocityX = 0;
            onGround = true;
            onSlipperyGround = false;
        }
        if (this.shape.getX() < 0) {
            this.shape.setX(0);
            velocityX = 0;
        }
        if (this.shape.getX() > WINDOW_WIDTH - this.shape.getWidth()) { // <<< ใช้ getWidth()
            this.shape.setX(WINDOW_WIDTH - this.shape.getWidth()); // <<< ใช้ getWidth()
            velocityX = 0;
        }
    }

    // --- "ปุ่ม" ให้ Game Class กด (Public Methods) ---
    // (เหมือนเดิมเป๊ะ!)
    public void setMoveLeft(boolean isActive) { this.isMovingLeft = isActive; }
    public void setMoveRight(boolean isActive) { this.isMovingRight = isActive; }
    public void startCharging() { if (this.onGround) { this.isChargingJump = true; } }
    public void releaseJump() {
        if (isChargingJump) {
            if (currentChargeLevel > 0) {
                // ... (โค้ดปล่อยกระโดดเหมือนเดิม) ...
                double launchSpeedY = -200 + (currentChargeLevel * JUMP_POWER_Y_PER_LEVEL);
                this.velocityY = launchSpeedY;
                double launchSpeedX = 150 + (currentChargeLevel * JUMP_POWER_X_PER_LEVEL);
                if (isMovingLeft) { this.velocityX = -launchSpeedX; }
                else if (isMovingRight) { this.velocityX = launchSpeedX; }
                else { this.velocityX = 0; }
                this.onGround = false;
                this.onSlipperyGround = false;
            }
            this.isChargingJump = false;
        }
    }

    // --- "หน้าปัด" ให้ Game Class อ่านค่า (Public Getters) ---
    public boolean hasWon() { return this.hasWon; }

    // คืนค่าเป็น Node (Class แม่ของ Rectangle)
    public Node getShape() { return this.shape; }

    public String getChargeLevelText() { return "Level: ".concat(String.valueOf(currentChargeLevel)); }

    // (ใช้ .getY() และ .getHeight() / .getWidth() ของ Rectangle)
    public double getY() { return this.shape.getY(); }
    public double getHeight() { return this.shape.getHeight(); } // <<< ใช้ getHeight()
    public double getWidth() { return this.shape.getWidth(); }   // <<< ใช้ getWidth()
}