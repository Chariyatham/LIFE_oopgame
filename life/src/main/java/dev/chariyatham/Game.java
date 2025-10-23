package dev.chariyatham;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Game extends Application {

    // --- ส่วนที่ 1: ค่าคงที่สำหรับปรับแต่งเกม (หน่วยต่อวินาที) ---
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final double MOVE_SPEED = 250;      // ความเร็วในการเดิน (pixels/วินาที)
    private static final double GRAVITY = 1000;     // แรงโน้มถ่วง (pixels/วินาที²)
    private static final double JUMP_POWER = -500;  // พลังกระโดดเริ่มต้น (pixels/วินาที)

    // --- ส่วนที่ 2: ตัวแปรสำหรับเก็บสถานะต่างๆ ของเกม ---
    private Rectangle player;
    private double velocityY = 0;
    private boolean onGround = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private long lastUpdateNanos = 0; // สำหรับคำนวณ Delta Time

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("LIFE:D - บทที่ 5: แก่นของเกมแพลตฟอร์มเมอร์");

        Pane root = new Pane();
        root.setStyle("-fx-background-color: #3C486B;");

        player = new Rectangle(60, 80, Color.ORANGE);
        player.setX(WINDOW_WIDTH / 2.0 - 30);
        player.setY(0);

        root.getChildren().add(player);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // --- ส่วนที่ 3: ระบบควบคุมแบบ "สวิตช์ไฟ" ---
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:  isMovingLeft = true; break;
                case RIGHT: isMovingRight = true; break;
                case SPACE:
                    if (onGround) {
                        velocityY = JUMP_POWER;
                        onGround = false;
                    }
                    break;
            }
        });

        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case LEFT:  isMovingLeft = false; break;
                case RIGHT: isMovingRight = false; break;
            }
        });

        // --- ส่วนที่ 4: Game Loop หัวใจของเกม ---
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // 4.1 คำนวณ Delta Time
                if (lastUpdateNanos == 0) {
                    lastUpdateNanos = now;
                    return;
                }
                double deltaTime = (now - lastUpdateNanos) / 1_000_000_000.0;
                lastUpdateNanos = now;

                // 4.2 อัปเดตการเคลื่อนที่ (เช็คสวิตช์ไฟ)
                if (isMovingLeft && player.getX() > 0) {
                    player.setX(player.getX() - MOVE_SPEED * deltaTime);
                }
                if (isMovingRight && player.getX() < WINDOW_WIDTH - player.getWidth()) {
                    player.setX(player.getX() + MOVE_SPEED * deltaTime);
                }

                // 4.3 อัปเดตฟิสิกส์
                velocityY += GRAVITY * deltaTime;
                player.setY(player.getY() + velocityY * deltaTime);

                // 4.4 ตรวจสอบการชนพื้น
                double groundLevel = WINDOW_HEIGHT - player.getHeight();
                if (player.getY() >= groundLevel) {
                    player.setY(groundLevel);
                    velocityY = 0;
                    onGround = true;
                }
            }
        };

        gameLoop.start(); // เริ่มต้นการทำงานของ "หัวใจ"

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}