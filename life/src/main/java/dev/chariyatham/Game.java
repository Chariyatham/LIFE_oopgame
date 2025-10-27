package dev.chariyatham;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

// 1. เพิ่ม import สำหรับ List
import java.util.ArrayList;
import java.util.List;

public class Game extends Application {

    // --- ตัวแปรผู้เล่นและฟิสิกส์ ---
    private Rectangle player;
    private double velocityX = 0;
    private double velocityY = 0;
    private boolean onGround = false;

    // --- ตัวแปรระบบชาร์จ ---
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private long lastUpdateNanos = 0;
    private boolean isChargingJump = false;
    private double chargeTimer = 0.0;
    private Text chargeLevelText;
    private int currentChargeLevel = 0;

    // --- 2. เปลี่ยน! จากแท่นเดียวเป็น "กล่อง" (List) ---
    private List<Rectangle> allPlatforms;

    // --- ค่าคงที่เกม ---
    private static final double GRAVITY = 1000.0;
    private static final double MAX_CHARGE_TIME = 1.0;
    private static final double JUMP_POWER_Y_PER_LEVEL = -180.0;
    private static final double JUMP_POWER_X_PER_LEVEL = 100.0;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int PLAYER_HEIGHT = 80;
    private static final double GROUND_LEVEL = WINDOW_HEIGHT - PLAYER_HEIGHT;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("LIFE:D - บทที่ 7.3: สร้างด่าน!");

        Pane root = new Pane();
        root.setStyle("-fx-background-color: #3C486B;");

        // Player และ Text (เหมือนเดิม)
        player = new Rectangle(60, PLAYER_HEIGHT, Color.ORANGE);
        player.setX(370);
        player.setY(GROUND_LEVEL);
        chargeLevelText = new Text("Level: 0");
        chargeLevelText.setFont(Font.font("Arial", 24));
        chargeLevelText.setFill(Color.WHITE);
        chargeLevelText.setX(WINDOW_WIDTH - 120);
        chargeLevelText.setY(30);

        // --- 3. สร้าง "กล่อง" และ "แท่น" ---
        allPlatforms = new ArrayList<>(); // สร้างกล่องเปล่า

        // สร้างแท่นที่ 1
        Rectangle platform1 = new Rectangle(150, 20, Color.GREEN);
        platform1.setX(200);
        platform1.setY(400);

        // สร้างแท่นที่ 2
        Rectangle platform2 = new Rectangle(200, 20, Color.GREEN);
        platform2.setX(450); // ขยับไปทางขวา
        platform2.setY(300); // สูงขึ้นไปอีก

        // สร้างแท่นที่ 3 (แท่นเล็กๆ)
        Rectangle platform3 = new Rectangle(50, 20, Color.RED); // สีแดง
        platform3.setX(350);
        platform3.setY(200);

        // --- 4. "หย่อน" แท่นทั้งหมดลงกล่อง ---
        allPlatforms.add(platform1);
        allPlatforms.add(platform2);
        allPlatforms.add(platform3);

        // --- 5. เพิ่มทุกอย่างลงในฉาก ---
        root.getChildren().addAll(player, chargeLevelText); // เพิ่ม player, text
        root.getChildren().addAll(allPlatforms); // เพิ่ม "แท่นทั้งหมด" ในกล่องทีเดียว!

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // --- ส่วน "กด" (เหมือนเดิม) ---
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    isMovingLeft = true;
                    break;
                case RIGHT:
                    isMovingRight = true;
                    break;
                case SPACE:
                    if (onGround) {
                        isChargingJump = true;
                    }
                    break;
            }
        });

        // --- ส่วน "ปล่อย" (เหมือนเดิม) ---
        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case LEFT:
                    isMovingLeft = false;
                    break;
                case RIGHT:
                    isMovingRight = false;
                    break;
                case SPACE:
                    if (isChargingJump) {
                        if (currentChargeLevel > 0) {
                            double launchSpeedY = -200 + (currentChargeLevel * JUMP_POWER_Y_PER_LEVEL);
                            velocityY = launchSpeedY;

                            double launchSpeedX = 150 + (currentChargeLevel * JUMP_POWER_X_PER_LEVEL);

                            if (isMovingLeft) {
                                velocityX = -launchSpeedX;
                            } else if (isMovingRight) {
                                velocityX = launchSpeedX;
                            } else {
                                velocityX = 0;
                            }

                            onGround = false;
                        }
                        isChargingJump = false;
                    }
                    break;
            }
        });

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // (โค้ด deltaTime)
                if (lastUpdateNanos == 0) {
                    lastUpdateNanos = now;
                    return;
                }
                double deltaTime = (now - lastUpdateNanos) / 1_000_000_000.0;
                lastUpdateNanos = now;

                // (โค้ดอัปเดตชาร์จและ UI)
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
                chargeLevelText.setText("Level: " + currentChargeLevel);

                // --- 1. อัปเดตฟิสิกส์ (ถ้าลอยอยู่) ---
                if (!onGround) {

                    // --- 1A. ขยับและตรวจสอบ "แกน X" ---
                    double oldX = player.getX();
                    player.setX(player.getX() + velocityX * deltaTime);

                    // --- 6. หุ้มด้วย FOR LOOP ---
                    for (Rectangle platform : allPlatforms) { // "สำหรับแท่นทุกอัน..."
                        boolean isOverlappingY = player.getY() + player.getHeight() > platform.getY() &&
                                player.getY() < platform.getY() + platform.getHeight();

                        if (isOverlappingY) {
                            // เคส 1: ชน "ด้านซ้าย" ของแท่น
                            if (velocityX > 0 &&
                                    oldX + player.getWidth() <= platform.getX() &&
                                    player.getX() + player.getWidth() >= platform.getX()) {
                                player.setX(platform.getX() - player.getWidth()); // ยันกลับ
                                velocityX = 0;
                                break; // --- 8. สำคัญ: ชนแล้ว "หยุด" เช็คอันอื่น ---
                            }
                            // เคส 2: ชน "ด้านขวา" ของแท่น
                            else if (velocityX < 0 &&
                                    oldX >= platform.getX() + platform.getWidth() &&
                                    player.getX() <= platform.getX() + platform.getWidth()) {
                                player.setX(platform.getX() + platform.getWidth()); // ยันกลับ
                                velocityX = 0;
                                break; // --- 8. สำคัญ: ชนแล้ว "หยุด" เช็คอันอื่น ---
                            }
                        }
                    } // --- สิ้นสุด For Loop (แกน X) ---

                    // --- 1B. ขยับและตรวจสอบ "แกน Y" ---
                    double oldY = player.getY();
                    velocityY += GRAVITY * deltaTime;
                    player.setY(player.getY() + velocityY * deltaTime);

                    // --- 6. หุ้มด้วย FOR LOOP (อีกครั้ง) ---
                    for (Rectangle platform : allPlatforms) { // "สำหรับแท่นทุกอัน..."
                        boolean isOverlappingX = player.getX() + player.getWidth() > platform.getX() &&
                                player.getX() < platform.getX() + platform.getWidth();

                        if (isOverlappingX) {
                            // เคส 3: "ตกลงมาเหยียบ"
                            if (velocityY > 0 &&
                                    oldY + player.getHeight() <= platform.getY() &&
                                    player.getY() + player.getHeight() >= platform.getY()) {
                                player.setY(platform.getY() - player.getHeight()); // ยืนบนแท่น
                                velocityY = 0;
                                velocityX = 0; // หยุด X ด้วย
                                onGround = true;
                                break; // --- 8. สำคัญ: "เหยียบ" แล้ว หยุดเช็คอันอื่น ---
                            }
                            // เคส 4: "ลอยขึ้นไปชน"
                            else if (velocityY < 0 &&
                                    oldY >= platform.getY() + platform.getHeight() &&
                                    player.getY() <= platform.getY() + platform.getHeight()) {
                                player.setY(platform.getY() + platform.getHeight()); // ยันหัวกลับลงมา
                                velocityY = 0;
                                break; // --- 8. สำคัญ: "หัวชน" แล้ว หยุดเช็คอันอื่น ---
                            }
                        }
                    } // --- สิ้นสุด For Loop (แกน Y) ---
                } // สิ้นสุด if (!onGround)

                // --- 2. ตรวจสอบการชนกับ "พื้นดิน" (ถ้ายังไม่เจออะไร) ---
                if (!onGround && player.getY() >= GROUND_LEVEL) {
                    player.setY(GROUND_LEVEL);
                    velocityY = 0;
                    velocityX = 0;
                    onGround = true;
                }

                // --- 3. ป้องกันทะลุขอบจอ ---
                if (player.getX() < 0) {
                    player.setX(0);
                    velocityX = 0;
                }
                if (player.getX() > WINDOW_WIDTH - player.getWidth()) {
                    player.setX(WINDOW_WIDTH - player.getWidth());
                    velocityX = 0;
                }
            }
        };

        gameLoop.start();
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}