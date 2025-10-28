package dev.chariyatham;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color; // ui
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.CycleMethod; 
import javafx.scene.paint.LinearGradient; 
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle; 

import java.util.ArrayList;
import java.util.List;

public class Game extends Application {

    // (ตัวแปร พื้นๆwindown)
    private Player player;
    private List<Platform> allPlatforms;
    private Text chargeLevelText;
    private long lastUpdateNanos = 0;
    private Pane root;
    private Pane mainPane;
    private AnimationTimer gameLoop;
    private Text victoryText;
    private ImageView backgroundView; // Background Image

    // final fixxx
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int WORLD_HEIGHT = 1800;
    private static final double GROUND_LEVEL = WORLD_HEIGHT - 50;
    private static final double CAMERA_THRESHOLD_Y_TOP = WINDOW_HEIGHT / 3.0;
    private static final double CAMERA_THRESHOLD_Y_BOTTOM = WINDOW_HEIGHT * 0.75;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("LIFE:D - Jump King (Platform Textures)");

        // pane
        root = new Pane();
        root.setPrefSize(WINDOW_WIDTH, WORLD_HEIGHT);


        //Background Image
        try {
            Image backgroundImage = new Image(getClass().getResourceAsStream("/background.png"));
            if (!backgroundImage.isError()) {
                backgroundView = new ImageView(backgroundImage);
                backgroundView.setFitWidth(WINDOW_WIDTH);
                backgroundView.setFitHeight(WORLD_HEIGHT);
                backgroundView.setX(0);
                backgroundView.setY(0);
                root.getChildren().add(backgroundView); // เพิ่มพื้นหลังก่อน
            } else {
                throw new RuntimeException("BG Load Error");
            }
        } catch (Exception e) {
            System.err.println("!!! ไม่สามารถโหลด background.png, ใช้สีพื้นหลังแทน");
            root.setStyle("-fx-background-color: #383b42ff;"); // สีสำรอง
        }

        //  สร้าง "ผู้เล่น" และ "แท่น" ---
        player = new Player(370, GROUND_LEVEL); 
        // สร้าง "แท่น" ทั้งหมด
        allPlatforms = new ArrayList<>();

        allPlatforms.add(new Platform(100, 1680, 250, 50, "grass")); 
        allPlatforms.add(new Platform(450, 1600, 250, 20, "grass")); 
        allPlatforms.add(new Platform(30, 1520, 250, 30, "grass")); 


        allPlatforms.add(new Platform(500, 1420, 150, 20, "grass"));
        allPlatforms.add(new Platform(250, 1350, 100, 40, "red"));   
        allPlatforms.add(new Platform(50, 1250, 150, 20, "ice"));  
        allPlatforms.add(new Platform(500, 1180, 150, 30, "grass")); 
        allPlatforms.add(new Platform(150, 1100, 100, 25, "grass")); 
        allPlatforms.add(new Platform(550, 1050, 60, 15, "red")); 

        allPlatforms.add(new Platform(200, 950, 120, 20, "ice"));
        allPlatforms.add(new Platform(600, 850, 100, 20, "grass"));
        allPlatforms.add(new Platform(300, 750, 100, 20, "grass")); 
        allPlatforms.add(new Platform(50, 650, 80, 20, "red")); 
        allPlatforms.add(new Platform(400, 600, 150, 20, "ice"));   

        allPlatforms.add(new Platform(150, 500, 100, 20, "grass")); 
        allPlatforms.add(new Platform(500, 400, 100, 20, "grass")); 
        allPlatforms.add(new Platform(200, 300, 80, 20, "red"));  
        allPlatforms.add(new Platform(450, 180, 150, 20, "win")); 


  

        // สร้าง UI text
        chargeLevelText = new Text("Level: 0");
        chargeLevelText.setFont(Font.font("Arial", 24));
        chargeLevelText.setFill(Color.WHITE);
        victoryText = new Text("YOU WIN!");
        victoryText.setFont(Font.font("Arial", 80));
        victoryText.setFill(Color.GOLD);
        victoryText.setVisible(false);

        //  put ui in root
        root.getChildren().add(player.getShape());
        root.getChildren().addAll(allPlatforms);

        // put root in camera 
        mainPane = new Pane();
        mainPane.getChildren().add(root);
        mainPane.getChildren().addAll(chargeLevelText, victoryText);
        chargeLevelText.setX(WINDOW_WIDTH - 120);
        chargeLevelText.setY(30);

        // show scene
        Scene scene = new Scene(mainPane, WINDOW_WIDTH, WINDOW_HEIGHT);

        // (ตั้งค่าการกดปุ่ม)
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    player.setMoveLeft(true);
                    break;
                case RIGHT:
                    player.setMoveRight(true);
                    break;
                case SPACE:
                    player.startCharging();
                    break;
            }
        });
        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case LEFT:
                    player.setMoveLeft(false);
                    break;
                case RIGHT:
                    player.setMoveRight(false);
                    break;
                case SPACE:
                    player.releaseJump();
                    break;
            }
        });

        // Game Loop
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // (คำนวณ Delta Time)
                if (lastUpdateNanos == 0) {
                    lastUpdateNanos = now;
                    return;
                }
                double deltaTime = (now - lastUpdateNanos) / 1_000_000_000.0;
                lastUpdateNanos = now;

                // อัปเดต Player
                player.update(deltaTime, allPlatforms);

                // อัปเดต UI
                chargeLevelText.setText(player.getChargeLevelText());

                // อัปเดต Camera
                double playerY = player.getY();
                double playerHeight = player.getHeight();
                double cameraY = -root.getTranslateY();
                double newCameraY = cameraY;
                if (playerY < cameraY + CAMERA_THRESHOLD_Y_TOP) {
                    newCameraY = playerY - CAMERA_THRESHOLD_Y_TOP;
                } else if (playerY + playerHeight > cameraY + CAMERA_THRESHOLD_Y_BOTTOM) {
                    newCameraY = (playerY + playerHeight) - CAMERA_THRESHOLD_Y_BOTTOM;
                }
                if (newCameraY < 0) {
                    newCameraY = 0;
                }
                if (newCameraY > WORLD_HEIGHT - WINDOW_HEIGHT) {
                    newCameraY = WORLD_HEIGHT - WINDOW_HEIGHT;
                }
                root.setTranslateY(-newCameraY);

                // เช็คชนะ
                if (player.hasWon()) {
                    victoryText.setX((WINDOW_WIDTH - victoryText.getLayoutBounds().getWidth()) / 2);
                    victoryText.setY(WINDOW_HEIGHT / 2.0);
                    victoryText.setVisible(true);
                    gameLoop.stop();
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