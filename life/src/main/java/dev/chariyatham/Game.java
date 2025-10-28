package dev.chariyatham;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * นี่คือ Class หลักของเกม ทำหน้าที่เป็น "ผู้จัดการ"
 * คอยควบคุม Game Loop, Camera, และการแสดงผล (UI)
 */
public class Game extends Application {

    // --- ตัวแปรหลัก ---
    private Player player;
    private List<Platform> allPlatforms;
    private Text chargeLevelText;
    private long lastUpdateNanos = 0;
    private Pane root; // "โลก" ทั้งใบ (Pane ที่เลื่อนได้)
    private Pane mainPane; // "หน้าต่าง" ที่ครอบโลกและ UI (Pane ที่อยู่กับที่)
    private AnimationTimer gameLoop; // ตัวควบคุม Game Loop
    private Text victoryText; // ป้าย "ชนะ"

    // --- ค่าคงที่ของ "โลก" และ "หน้าต่าง" ---
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600; // นี่คือขนาด "หน้าต่าง"

    // "โลก" ของเราจะสูง 1800px
    private static final int WORLD_HEIGHT = 1800;
    // (Player สูง 80 ตามค่าคงที่ใน Player.java)
    private static final double GROUND_LEVEL = WORLD_HEIGHT - 80;

    // --- ค่าสำหรับ "กล้อง" ---
    private static final double CAMERA_THRESHOLD_Y_TOP = WINDOW_HEIGHT / 3.0; // ขอบบน (ที่ 200)
    private static final double CAMERA_THRESHOLD_Y_BOTTOM = WINDOW_HEIGHT * 0.75; // ขอบล่าง (ที่ 450)


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("LIFE:D - Jump King (บทที่ 10 - สี่เหลี่ยม)");

        // 1. สร้าง "โลก"
        root = new Pane();
        root.setStyle("-fx-background-color: #3C486B;");
        root.setPrefSize(WINDOW_WIDTH, WORLD_HEIGHT); // กำหนดให้ "โลก" สูง 1800px

        // 2. สร้าง "ผู้เล่น" (ตอนนี้ยังเป็น Rectangle) วางไว้ล่างสุดของโลก
        player = new Player(370, GROUND_LEVEL);

        // 3. สร้าง "UI" (ข้อความ)
        chargeLevelText = new Text("Level: 0");
        chargeLevelText.setFont(Font.font("Arial", 24));
        chargeLevelText.setFill(Color.WHITE);

        victoryText = new Text("YOU WIN!");
        victoryText.setFont(Font.font("Arial", 80));
        victoryText.setFill(Color.GOLD);
        victoryText.setVisible(false); // ซ่อนไว้ก่อน

        // 4. สร้าง "แท่น" ทั้งหมด (สร้างด่านแบบ Manual)
        allPlatforms = new ArrayList<>();

        // --- ออกแบบด่าน ---
        allPlatforms.add(new Platform(200, 1600, 150, 20, Color.GREEN));
        allPlatforms.add(new Platform(450, 1500, 200, 20, Color.GREEN));
        allPlatforms.add(new Platform(350, 1400, 50, 20, Color.RED)); // แท่นแดง

        // แท่นลื่น (ฟ้า)
        Platform icePlatform = new Platform(100, 1200, 150, 20, Color.CYAN);
        icePlatform.setAsSlippery(); // <<< สั่งให้มัน "ลื่น"
        allPlatforms.add(icePlatform);

        // แท่นธรรมดา (เขียว)
        allPlatforms.add(new Platform(500, 1000, 150, 20, Color.GREEN));
        allPlatforms.add(new Platform(300, 800, 150, 20, Color.GREEN));
        allPlatforms.add(new Platform(100, 600, 150, 20, Color.GREEN));
        allPlatforms.add(new Platform(400, 400, 150, 20, Color.GREEN));

        // แท่นชนะ (ทอง)
        Platform victoryPlatform = new Platform(250, 200, 150, 20, Color.GOLD);
        victoryPlatform.setAsVictoryPlatform(); // <<< สั่งให้เป็นแท่นชนะ
        allPlatforms.add(victoryPlatform);

        // --- สิ้นสุดการออกแบบด่าน ---

        // 5. ประกอบร่าง "โลก" และ "UI"
        root.getChildren().add(player.getShape()); // เพิ่มผู้เล่นเข้า "โลก"
        root.getChildren().addAll(allPlatforms); // เพิ่มแท่นทั้งหมดเข้า "โลก"

        mainPane = new Pane(); // "หน้าต่าง" ที่ครอบทุกอย่าง
        mainPane.getChildren().add(root); // เอา "โลก" (root) ใส่เข้าไป
        mainPane.getChildren().addAll(chargeLevelText, victoryText); // เอา "UI" ใส่ทับ (UI จะไม่เลื่อนตาม)

        // ตั้งค่าตำแหน่ง UI ให้อยู่มุมบนขวา "ของหน้าต่าง"
        chargeLevelText.setX(WINDOW_WIDTH - 120);
        chargeLevelText.setY(30);

        // 6. สร้าง Scene (จาก "หน้าต่าง" mainPane)
        Scene scene = new Scene(mainPane, WINDOW_WIDTH, WINDOW_HEIGHT);

        // 7. ตั้งค่าการควบคุม (สั่งการ Player)
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:  player.setMoveLeft(true); break;
                case RIGHT: player.setMoveRight(true); break;
                case SPACE: player.startCharging(); break;
            }
        });

        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case LEFT:  player.setMoveLeft(false); break;
                case RIGHT: player.setMoveRight(false); break;
                case SPACE: player.releaseJump(); break;
            }
        });


        // 8. สร้างและเริ่ม Game Loop
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // (คำนวณ Delta Time)
                if (lastUpdateNanos == 0) { lastUpdateNanos = now; return; }
                double deltaTime = (now - lastUpdateNanos) / 1_000_000_000.0;
                lastUpdateNanos = now;

                // --- 1. สั่ง Player ให้อัปเดตตัวเอง (สมองอยู่ที่ Player.update) ---
                player.update(deltaTime, allPlatforms);

                // --- 2. อัปเดต UI (ถาม Player ว่าเลเวลเท่าไหร่) ---
                chargeLevelText.setText(player.getChargeLevelText());

                // --- 3. อัปเดต "กล้อง" ---
                double playerY = player.getY();
                double playerHeight = player.getHeight();
                double cameraY = -root.getTranslateY(); // ตำแหน่ง Y ปัจจุบันของกล้อง (เป็นบวก)

                double newCameraY = cameraY; // ตำแหน่งใหม่ (เริ่มต้นให้เท่าเดิม)

                // เช็คเลื่อน "ขึ้น"
                if (playerY < cameraY + CAMERA_THRESHOLD_Y_TOP) {
                    newCameraY = playerY - CAMERA_THRESHOLD_Y_TOP;
                }
                // เช็คเลื่อน "ลง"
                else if (playerY + playerHeight > cameraY + CAMERA_THRESHOLD_Y_BOTTOM) {
                    newCameraY = (playerY + playerHeight) - CAMERA_THRESHOLD_Y_BOTTOM;
                }

                // จำกัดขอบเขตกล้อง (ไม่ให้ทะลุบน-ล่าง ของ "โลก")
                if (newCameraY < 0) { newCameraY = 0; }
                if (newCameraY > WORLD_HEIGHT - WINDOW_HEIGHT) {
                    newCameraY = WORLD_HEIGHT - WINDOW_HEIGHT;
                }

                // สั่งให้ "โลก" (root) เลื่อน
                root.setTranslateY(-newCameraY);


                // --- 4. เช็คว่าชนะหรือยัง (ถาม Player) ---
                if (player.hasWon()) {
                    // ตั้งค่าตำแหน่งข้อความให้อยู่ "กลางหน้าต่าง"
                    victoryText.setX((WINDOW_WIDTH - victoryText.getLayoutBounds().getWidth()) / 2);
                    victoryText.setY(WINDOW_HEIGHT / 2.0);

                    victoryText.setVisible(true); // โชว์ข้อความ
                    gameLoop.stop(); // <<< สั่งหยุดเกม!
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