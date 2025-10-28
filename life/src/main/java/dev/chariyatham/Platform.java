package dev.chariyatham;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * นี่คือพิมพ์เขียวสำหรับแท่น
 * มัน "สืบทอด" คุณสมบัติทุกอย่างจาก Rectangle
 * แต่เราเพิ่ม "สถานะ" (isVictory, isSlippery) เข้าไปเอง
 */
public class Platform extends Rectangle {

    // --- สถานะของแท่น ---
    private boolean isVictoryPlatform = false;
    private boolean isSlippery = false;

    /**
     * Constructor (ตัวสร้าง)
     */
    public Platform(double x, double y, double width, double height, Color color) {
        // เรียก Constructor ของแม่ (Rectangle)
        super(x, y, width, height);

        // ตั้งค่าสี
        this.setFill(color);
    }

    // --- "ปุ่ม" ให้ Game.java กดเพื่อตั้งค่า ---

    public void setAsVictoryPlatform() {
        this.isVictoryPlatform = true;
    }

    public void setAsSlippery() {
        this.isSlippery = true;
    }

    // --- "หน้าปัด" ให้ Player.java อ่านค่า ---

    public boolean isVictoryPlatform() {
        return this.isVictoryPlatform;
    }

    public boolean isSlippery() {
        return this.isSlippery;
    }
}