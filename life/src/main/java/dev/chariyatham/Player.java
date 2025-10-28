package dev.chariyatham;

import java.util.List;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Player {

    // --- รูปร่าง ---
    private ImageView gifView; // ตัวแสดงผล (GIF)
    private Rectangle hitbox; // กล่องชน 

    // --- คลังรูปภาพ ---
    private Image idleGifImage;
    private Image jumpGifImage; // <<< เพิ่มรูป Jump

    // --- สถานะฟิสิกส์, ชาร์จ, ชนะ, ลื่น ---
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

    // --- ค่าคงที่ ขนาด Hitbox
    private static final int HITBOX_WIDTH = 40;
    private static final int HITBOX_HEIGHT = 50;

    // --- ค่าคงที่ ขนาด GIF
    private static final int GIF_WIDTH = 180;
    private static final int GIF_HEIGHT = 120;

    // (ค่าคงที่โลก - ใช้ HITBOX_HEIGHT)
    private static final int WORLD_HEIGHT = 1800;
    private static final int WINDOW_WIDTH = 800;
    private static final double GROUND_LEVEL = WORLD_HEIGHT - HITBOX_HEIGHT;

    public Player(double startX, double startY) {

        // สร้าง Hitbox
        this.hitbox = new Rectangle(HITBOX_WIDTH, HITBOX_HEIGHT, Color.TRANSPARENT);
        // this.hitbox = new Rectangle(HITBOX_WIDTH, HITBOX_HEIGHT, Color.RED);
        this.hitbox.setX(startX);
        this.hitbox.setY(startY);

        // --- โหลดไฟล์ GIF ทั้งสอง ---
        try {
            idleGifImage = new Image(getClass().getResourceAsStream("/player_idle.gif"));
            jumpGifImage = new Image(getClass().getResourceAsStream("/player_jump.gif")); // <<< โหลด Jump GIF

            // เช็ค Error หลังโหลด
            if (idleGifImage != null && idleGifImage.isError()) {
                System.err.println("!!! Error loading Idle GIF: " + idleGifImage.getException());
                idleGifImage = null;
            }
            if (jumpGifImage != null && jumpGifImage.isError()) {
                System.err.println("!!! Error loading Jump GIF: " + jumpGifImage.getException());
                jumpGifImage = null;
            }
        } catch (NullPointerException e) {
            System.err.println("!!! หาไฟล์ GIF ไม่เจอใน resources (idle หรือ jump)");
        } catch (Exception e) {
            System.err.println("!!! เกิด Error ไม่คาดคิดตอนโหลด GIF:");
            e.printStackTrace();
        }

        // --- สร้าง ImageView ---
        // เริ่มด้วย Idle หรือรูปสำรองถ้าโหลดไม่ได้
        Image startingImage = (idleGifImage != null) ? idleGifImage : null;

        if (startingImage != null) {
            this.gifView = new ImageView(startingImage);
            this.gifView.setFitWidth(GIF_WIDTH);
            this.gifView.setFitHeight(GIF_HEIGHT);
        } else {
            // ถ้ารูปerrorให้ใช้ Hitbox แสดงแทน
            System.err.println("!!! ใช้ Hitbox สีส้มแสดงแทน GIF");
            this.hitbox.setFill(Color.ORANGE);
            this.gifView = new ImageView();
            this.gifView.setFitWidth(0);
            this.gifView.setFitHeight(0);
        }

        // ตั้งค่าตำแหน่ง GIF เริ่มต้น
        updateGifPosition();

        this.onGround = true;
    }


    private void updateGifPosition() {
        double hitboxBottomCenterX = this.hitbox.getX() + HITBOX_WIDTH / 2.0;
        double hitboxBottomY = this.hitbox.getY() + HITBOX_HEIGHT;

        // ลองปรับค่า +/- ตรงนี้เพื่อจัดตำแหน่ง GIF ให้ตรง Hitbox
        double gifX = hitboxBottomCenterX - GIF_WIDTH / 2.0; // <<< ปรับตรงนี้
        double gifY = hitboxBottomY - GIF_HEIGHT; // <<< ปรับตรงนี้

        this.gifView.setX(gifX);
        this.gifView.setY(gifY);
    }


    public void update(double deltaTime, List<Platform> allPlatforms) {

        // อัปเดต Animation (สลับ GIF!) ---
        if (onGround) {
            // ถ้าอยู่บนพื้น และยังไม่ได้แสดง Idle GIF (และมี Idle GIF)
            if (this.gifView.getImage() != idleGifImage && idleGifImage != null) {
                this.gifView.setImage(idleGifImage); // <<< เปลี่ยนเป็น Idle GIF
            }
        } else {
            // ถ้าลอยอยู่ และยังไม่ได้แสดง Jump GIF (และมี Jump GIF)
            if (this.gifView.getImage() != jumpGifImage && jumpGifImage != null) {
                this.gifView.setImage(jumpGifImage); // <<< เปลี่ยนเป็น Jump GIF
            }
        }

        //อัปเดตการกลับทิศ (Flip) ---
        if (isMovingLeft) {
            this.gifView.setScaleX(-1); // กลับด้าน GIF
        } else if (isMovingRight) {
            this.gifView.setScaleX(1); // กลับด้าน GIF (ปกติ)
        }

        // (อัปเดตชาร์จ)
        if (isChargingJump && onGround) {
            chargeTimer += deltaTime;
            if (chargeTimer > MAX_CHARGE_TIME)
                chargeTimer = MAX_CHARGE_TIME;
            if (chargeTimer >= MAX_CHARGE_TIME)
                currentChargeLevel = 5;
            else if (chargeTimer >= 0.8)
                currentChargeLevel = 4;
            else if (chargeTimer >= 0.6)
                currentChargeLevel = 3;
            else if (chargeTimer >= 0.4)
                currentChargeLevel = 2;
            else if (chargeTimer >= 0.2)
                currentChargeLevel = 1;
            else
                currentChargeLevel = 0;
        } else {
            chargeTimer = 0.0;
            currentChargeLevel = 0;
        }
        // (แรงเสียดทาน)
        if (onGround && onSlipperyGround) {
            if (velocityX > 0) {
                velocityX -= FRICTION_DECEL * deltaTime;
                if (velocityX < 0)
                    velocityX = 0;
            } else if (velocityX < 0) {
                velocityX += FRICTION_DECEL * deltaTime;
                if (velocityX > 0)
                    velocityX = 0;
            }
        }

        // ฟิสิกส์
        if (!onGround) {

            // แกน X
            double oldX = this.hitbox.getX();
            this.hitbox.setX(this.hitbox.getX() + velocityX * deltaTime);

            for (Platform platform : allPlatforms) {
                boolean isOverlappingY = this.hitbox.getY() + this.hitbox.getHeight() > platform.getY() &&
                        this.hitbox.getY() < platform.getY() + platform.getHeight();

                if (isOverlappingY) {
                    // ชนซ้าย (เด้งเบาๆ)
                    if (velocityX > 0 &&
                            oldX + this.hitbox.getWidth() <= platform.getX() &&
                            this.hitbox.getX() + this.hitbox.getWidth() >= platform.getX()) {
                        this.hitbox.setX(platform.getX() - this.hitbox.getWidth());
                        velocityX = -velocityX / 2; // <<< ใช้ค่าที่คุณปรับ
                        break;
                    }
                    // ชนขวา (เด้งเบาๆ)
                    else if (velocityX < 0 &&
                            oldX >= platform.getX() + platform.getWidth() &&
                            this.hitbox.getX() <= platform.getX() + platform.getWidth()) {
                        this.hitbox.setX(platform.getX() + platform.getWidth());
                        velocityX = -velocityX / 2; // <<< ใช้ค่าที่คุณปรับ
                        break;
                    }
                }
            }

            // แกน Y
            double oldY = this.hitbox.getY();
            velocityY += GRAVITY * deltaTime;
            this.hitbox.setY(this.hitbox.getY() + velocityY * deltaTime);

            for (Platform platform : allPlatforms) {
                boolean isOverlappingX = this.hitbox.getX() + this.hitbox.getWidth() > platform.getX() &&
                        this.hitbox.getX() < platform.getX() + platform.getWidth();

                if (isOverlappingX) {
                    // เคสเหยียบ
                    if (velocityY > 0 &&
                            oldY + this.hitbox.getHeight() <= platform.getY() &&
                            this.hitbox.getY() + this.hitbox.getHeight() >= platform.getY()) {
                        this.hitbox.setY(platform.getY() - this.hitbox.getHeight());
                        velocityY = 0;
                        onGround = true;
                        if (platform.isSlippery()) {
                            this.onSlipperyGround = true;
                        } else {
                            this.onSlipperyGround = false;
                            this.velocityX = 0;
                        }
                        if (platform.isVictoryPlatform()) {
                            this.hasWon = true;
                        }
                        break;
                    }
                    // เคสหัวชน
                    else if (velocityY < 0 &&
                            oldY >= platform.getY() + platform.getHeight() &&
                            this.hitbox.getY() <= platform.getY() + platform.getHeight()) {
                        this.hitbox.setY(platform.getY() + platform.getHeight());
                        velocityY = 0;
                        break;
                    }
                }
            }
        } // สิ้นสุด if (!onGround)

        // (ชนพื้น/ขอบจอ - ใช้ Hitbox)
        if (!onGround && this.hitbox.getY() >= GROUND_LEVEL) {
            this.hitbox.setY(GROUND_LEVEL);
            velocityY = 0;
            velocityX = 0;
            onGround = true;
            onSlipperyGround = false;
        }
        if (this.hitbox.getX() < 0) {
            this.hitbox.setX(0);
            velocityX = 0;
        }
        if (this.hitbox.getX() > WINDOW_WIDTH - this.hitbox.getWidth()) {
            this.hitbox.setX(WINDOW_WIDTH - this.hitbox.getWidth());
            velocityX = 0;
        }

        //อัปเดตตำแหน่ง GIF ให้ตาม Hitbox
        updateGifPosition();
    }

    public void setMoveLeft(boolean isActive) {
        this.isMovingLeft = isActive;
    }

    public void setMoveRight(boolean isActive) {
        this.isMovingRight = isActive;
    }

    public void startCharging() {
        if (this.onGround) {
            this.isChargingJump = true;
        }
    }

    public void releaseJump() {
        if (isChargingJump) {
            if (currentChargeLevel > 0) {
                double launchSpeedY = -200 + (currentChargeLevel * JUMP_POWER_Y_PER_LEVEL);
                this.velocityY = launchSpeedY;
                double launchSpeedX = 150 + (currentChargeLevel * JUMP_POWER_X_PER_LEVEL);
                if (isMovingLeft) {
                    this.velocityX = -launchSpeedX;
                } else if (isMovingRight) {
                    this.velocityX = launchSpeedX;
                } else {
                    this.velocityX = 0;
                }
                this.onGround = false;
                this.onSlipperyGround = false;
            }
            this.isChargingJump = false;
        }
    }

    public boolean hasWon() {
        return this.hasWon;
    }

    public Node getShape() {
        return this.gifView;
    } 

    public String getChargeLevelText() {
        return "Level: ".concat(String.valueOf(currentChargeLevel));
    }

    public double getY() {
        return this.hitbox.getY();
    } 

    public double getHeight() {
        return this.hitbox.getHeight();
    } 

    public double getWidth() {
        return this.hitbox.getWidth();
    } 
}