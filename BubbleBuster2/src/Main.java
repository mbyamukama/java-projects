import java.awt.*;
import javax.swing.*; // The package where the components are
import java.awt.event.*; // Used for event listeners and events
import java.awt.Graphics;

import java.util.ArrayList;
import java.lang.Math;
import java.util.*;
import javax.swing.Timer;

public class Main {
    static Graphics gr;
    static ArrayList<Bubble> bubbles = new ArrayList<Bubble>();
    static boolean gameIsReady = false;
    static int round = 1, round1Time = 15, timeLeft = 0, neighborhood = 50;
    static Timer timer = null;

    //called when the conditions for a game over are met
    static void gameOver() {
        bubbles.clear();
        JOptionPane.showMessageDialog(null,
                "Game over!");
        closeFrame("playField");
    }

    static void closeFrame(String frameName) {
        Frame[] allFrames = Frame.getFrames(); // get all open frames
        for (Frame frame : allFrames) {
            if (frame.getName().equals(frameName)) //find the one called playField. See line 34
                frame.dispose(); //destroy of it
        }
    }

    private static void repositionGlobal(JFrame field, int numBubbles) {
        while (bubbles.size() < numBubbles) {
            int x = getRandomCoordinate(field.getWidth());
            int y = getRandomCoordinate(field.getHeight());
            Bubble newBubble = new Bubble(gr, x, y, neighborhood);
            boolean frameClearanceTestResult = frameClearanceTest(x, y, Bubble.diameter, field.getWidth(), field.getHeight());
            boolean bubbleClearanceTestResult = true;
            if (bubbles.size() > 0) {
                for (Bubble b : bubbles) { //for each bubble in the bubbles list
                    bubbleClearanceTestResult = bubbleClearanceTest(newBubble.x, newBubble.y, b.x, b.y, b.diameter);
                    if (!bubbleClearanceTestResult)
                        break; // no further tests if you fail
                }
            }
            if (frameClearanceTestResult && bubbleClearanceTestResult) //both have passed for
            {
                bubbles.add(newBubble);
                newBubble.draw();
            }
        }
    }

    private static void repositionLocal() {
        for (Bubble bubble : bubbles) {
            bubble.hop();
        }
    }

    //to ensure two bubbles don't collide, the distance between their centers must be greater than the diameter. distance btn two points = sqrt [(x1-x2)^2 + (y1-y2)^2]
    public static boolean bubbleClearanceTest(int x1, int y1, int x2, int y2, int minDistance) {
        double clearance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        return clearance >= minDistance;
    }

    // a bubble at (x,y) with diameter d will be contained in a frame if x-d/2 and y-d/2 are greater than 0
    //AND if (x + d / 2) and (y + d / 2) are less than the frameWidth and frameHeight respectively
    public static boolean frameClearanceTest(int x, int y, int r, int frameWidth, int frameHeight) {
        return (x - r) > 0 && (y - r) > 0 && (x + r) < frameWidth && (y + r) < frameHeight;
    }

    //generates a random x coordinate between 0 and frameDimensionm
    public static int getRandomCoordinate(int frameDimension) {
        return Bubble.getRadius() + new Random().nextInt(frameDimension - Bubble.getRadius());
    }

    public static JFrame createPlayField(int numBubbles) {
        JFrame field = new JFrame("Field of Play");
        field.setName("playField");
        field.setLayout(null);
        field.setLocationRelativeTo(null);
        field.setSize(800, 500);
        round = 1;
        gameIsReady = false;
        timeLeft = round1Time;

        field.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
//get a graphics object to draw. NB: you can only do this when the frame is open
                gr = field.getGraphics();
                timer = new Timer(1000, timerEvent -> {
                    timeLeft = timeLeft - 1;
                    field.setTitle("Field of Play    Round:" + round + " Time: " + timeLeft);
                    if (round >= 2 && timeLeft % 2 == 0) repositionLocal();
                    if (timeLeft == 0) gameOver();
                });
            }
        });
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //get where we have clicked
                int clickedX = e.getX();
                int clickedY = e.getY();
                if (gameIsReady) //any clicks when the game is ready should lead to a bubble being burst, AND removes from the bubble list
                {
                    timer.start();
                    //now find the bubble in this area. A point is inside a circle if the distance between the point and the center of the circle is less than the radius
                    for (int i = 0; i < bubbles.size(); i++) {
                        Bubble currentBubble = bubbles.get(i);
                        double d = Math.sqrt(Math.pow(clickedX - currentBubble.x, 2) + Math.pow(clickedY - currentBubble.y, 2));
                        if (d < currentBubble.diameter * 0.5) //this point is inside!
                        {
                            bubbles.remove(currentBubble);
                            currentBubble.erase();
                            break;
                        }
                    }
                    if (bubbles.size() == 0) //round over!
                    {
                        gameIsReady = false;
                        timer.stop(); //stop the timer
                        round++;
                        neighborhood += 18;
                        timeLeft = round1Time - round + 1;// round 1=15, 2=14, 3=13 etc so timeLeft = 15-round+1;
                        if (round > 10)
                            gameOver();
                    }
                }
                //this block is what gets executed during initialization of the playfield when for rounds 2-10
                if (bubbles.size() == 0 && !gameIsReady && round >= 2 && round <= 10) {
                    //random draw
                    repositionGlobal(field, numBubbles);
                    gameIsReady = true; //set game is ready to start
                    timer.start();
                }
                if (bubbles.size() < numBubbles && !gameIsReady && round == 1) {
                    // check clearance. should be greater than bubble diameter for all points
                    Bubble newBubble = new Bubble(gr, clickedX, clickedY, neighborhood);

                    // a bubble pending addition must pass the clearance test for the Frame and
                    // other bubbles
                    boolean frameClearanceTestResult = frameClearanceTest(newBubble.x, newBubble.y, newBubble.diameter, field.getWidth(),
                            field.getHeight());
                    if (!frameClearanceTestResult) {
                        JOptionPane.showMessageDialog(field,
                                "The selected position doesn't have sufficient clearance from the frame border");
                    }

                    boolean bubbleClearanceTestResult = true;
                    if (bubbles.size() > 0) {
                        for (Bubble b : bubbles) { //for each bubble in the bubbles list
                            bubbleClearanceTestResult = bubbleClearanceTest(newBubble.x, newBubble.y, b.x, b.y, b.diameter);
                            if (!bubbleClearanceTestResult)
                                break; // no further tests if you fail
                        }
                    }
                    if (!bubbleClearanceTestResult) {
                        JOptionPane.showMessageDialog(null,
                                "The selected position doesn't have sufficient clearance from a neighboring bubble");
                    }

                    if (frameClearanceTestResult && bubbleClearanceTestResult) {
                        bubbles.add(newBubble); // add to list of bubbles
                        newBubble.draw();
                    }
                }
                if (bubbles.size() == numBubbles) {
                    gameIsReady = true;
                }

            }
        });
        return field;
    }


    public static void main(String[] args) {

        JFrame frame1 = new JFrame("BubbleBuster");
        frame1.setLayout(null);
        frame1.setSize(500, 300);
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame1.setLocationRelativeTo(null);

        JSlider slider = new JSlider();
        slider.setBounds(50, 50, 400, 50);
        slider.setMinimum(4);
        slider.setMaximum(6);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        JButton btnStart = new JButton("Start");
        btnStart.setBounds(50, 200, 80, 20);

        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sliderValue = slider.getValue();
                JFrame playField = createPlayField(sliderValue);
                playField.setVisible(true);
            }
        });

        JButton btnRestart = new JButton("Restart");
        btnRestart.setBounds(380, 200, 80, 20);
        btnRestart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeFrame("playField");
                timer.stop();
                bubbles.clear(); //empty the current list of bubbles
                int sliderValue = slider.getValue(); //get updated slider value if any
                JFrame playField = createPlayField(sliderValue); //create and show a new playField
                playField.setVisible(true);
            }
        });

        frame1.getContentPane().add(btnStart);
        frame1.getContentPane().add(btnRestart);
        frame1.getContentPane().add(slider);
        frame1.setVisible(true);
    }
}