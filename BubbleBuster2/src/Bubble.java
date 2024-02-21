import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Bubble {
    public int x, originalX;
    public int y, originalY;
    public static int diameter = 40;
    private Graphics g;
    private int neighborhood;

    public Bubble(Graphics gr, int x, int y, int neighborhood) {
        g = gr;
        g.setColor(Color.red);
        this.x = x;
        this.y = y;
        originalX = x;
        originalY = y;
        this.neighborhood = neighborhood;
    }

    public static int getRadius() {
        return diameter / 2;
    }

    public void draw() {
        g.fillOval(x - this.getRadius(), y - this.getRadius(), diameter, diameter);
    }

    public void erase() {
        g.clearRect(x - getRadius(), y - getRadius(), diameter, diameter);
    }

    //a hop involves: changing the bubble center, redrawing the bubble
    public void hop() {
        //get the range of the bounding box and get random coordinates in that range
        this.erase();
        int dx = -1 * neighborhood + new Random().nextInt(neighborhood);
        int dy = -1 * neighborhood + new Random().nextInt(neighborhood);
        x = originalX + dx;
        y = originalY + dy;
        this.draw();
    }
}
