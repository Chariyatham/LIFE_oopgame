package dev.chariyatham;


import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern; 
import javafx.scene.shape.Rectangle;

public class Platform extends Rectangle {

    // สถานะ 
    private boolean isVictoryPlatform = false;
    private boolean isSlippery = false;

    // เพิ่มตัวแปร Image สำหรับเก็บ "ลาย"
    // (method loadImage กัน Error)
    private static final Image GRASS_TEXTURE = loadImage("/platform_grass.png");
    private static final Image ICE_TEXTURE = loadImage("/platform_ice.png");
    private static final Image WIN_TEXTURE = loadImage("/platform_win.png");
    private static final Image RED_TEXTURE = loadImage("/platform_red.png"); 

    private static Image loadImage(String path) {
        try {
            // ใช้ getResourceAsStream หาไฟรต้องเจอไม่เจอเตื่อนนิดนึง
            Image img = new Image(Platform.class.getResourceAsStream(path));
            // check error
            if (img.isError()) {
                System.err.println("!!! Error loading platform texture: " + path + " - " + img.getException());
                return null; // คืนค่า null ถ้าโหลดไม่ได้
            }
            // สำเร็จ
            return img;
        } catch (NullPointerException e) {
            //หาไฟล์ไม่เจอ
            System.err.println("!!! Could not find platform texture file: " + path);
            return null;
        } catch (Exception e) {
            // ดักerror
            System.err.println("!!! Unexpected error loading platform texture: " + path);
            e.printStackTrace();
            return null;
        }
    }

    public Platform(double x, double y, double width, double height, String type) { // <<< รับ String type แทน Color
        // เรียก Constructor ของแม่ (Rectangle)
        super(x, y, width, height);

        //เลือก Image ที่จะใช้ และตั้งค่าสถานะ
        Image texture = null;
        Color fallbackColor = Color.MAGENTA; // สีสำรองม่วง

        switch (type.toLowerCase()) {
            case "grass":
                texture = GRASS_TEXTURE;
                fallbackColor = Color.GREEN;
                break;
            case "ice":
                texture = ICE_TEXTURE;
                fallbackColor = Color.CYAN; 
                this.isSlippery = true; 
                break;
            case "win":
                texture = WIN_TEXTURE;
                fallbackColor = Color.GOLD;
                this.isVictoryPlatform = true; 
                break;
            case "red":
                texture = RED_TEXTURE; 
                fallbackColor = Color.RED; 
                break;
            default: 
                System.err.println("!!! Unknown platform type: " + type + " - Using default grass.");
                texture = GRASS_TEXTURE; // ใช้ลายหญ้าเป็นค่าเริ่มต้น
                fallbackColor = Color.GREEN;
                break;
        }

        if (texture != null) {
            // สร้าง ImagePattern:
            // - texture: รูปที่จะใช้
            // - 0, 0: จุดเริ่มต้นลายในรูป (มุมบนซ้าย)
            // - texture.getWidth(), texture.getHeight(): ขนาดของลาย (ใช้ขนาดจริงของรูป)
            // - false, false: ไม่ต้องยืดลายให้พอดีแท่น (ปล่อยให้มันเรียงต่อกันเอง)
            ImagePattern pattern = new ImagePattern(texture, 0, 0, texture.getWidth(), texture.getHeight(), false);
            this.setFill(pattern); // <<< สั่งทาสีด้วยลายรูปภาพ!
        } else {
            // ถ้าโหลดรูปล้มเหลว (texture เป็น null)
            System.err.println(">>> Using fallback color for platform type: " + type);
            this.setFill(fallbackColor); // ใช้สีสำรองแทน
        }
    }

    // --- Getters สำหรับสถานะ (ไม่แก้ไข) ---
    public boolean isVictoryPlatform() {
        return this.isVictoryPlatform;
    }

    public boolean isSlippery() {
        return this.isSlippery;
    }
}