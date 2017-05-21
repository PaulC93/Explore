package com.paul.explore.gui;

import com.paul.explore.model.Map;
import com.paul.explore.sim.VirtualBot;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import static com.paul.explore.model.BotConstants.SENSOR_ROTATION_RADIUS;
import static com.paul.explore.model.GeometryHelper.round;
import static java.awt.Color.*;

/**
 * Provides a map with what the bot sees
 * Note this class use the virtualBot to simulate the movement of the bot and relies on the connection
 * with the real bot only to get the distances until obstacles to mark the free area, so if the real bot encounters
 * a problem and doesn't move as expected the map will be incorrect.
 */
public class MapView extends JFrame
{
    private VirtualBot virtualBot;
    private Map map;

    public MapView(VirtualBot virtualBot)
    {
        this.virtualBot = virtualBot;
        map = virtualBot.getMap();
        setUpUI();
        virtualBot.registerRepaintCallback(this::repaintMe);
    }

    private void setUpUI()
    {
        final int MARGIN = 10;
        final int VERTICAL_DELTA = 50;
        final int HORIZONTAL_DELTA = 25;
        JPanel worldPanel = new WorldPanel();
        int mapWidth = map.getWidth();
        int mapHeight = map.getHeight();
        int DOUBLE_MARGIN = MARGIN * 2;
        this.setPreferredSize(new Dimension(mapWidth + DOUBLE_MARGIN + HORIZONTAL_DELTA, mapHeight + DOUBLE_MARGIN + VERTICAL_DELTA));
        this.setLayout(null);
        JPanel pane = new JPanel();
        pane.setSize(this.getSize());
        pane.setBackground(BLACK);
        this.setContentPane(pane);

        worldPanel.setSize(mapWidth, mapHeight);
        worldPanel.setBounds(MARGIN, -MARGIN, mapWidth, mapHeight);
        worldPanel.setBackground(DARK_GRAY);

        pane.add(worldPanel);
        pane.setLayout(null);
        this.pack();

        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void repaintMe()
    {
        this.repaint();
        try
        {
            Thread.sleep(10);
        } catch (InterruptedException e)
        {
            //e.printStackTrace();
        }
    }

    private class WorldPanel extends JPanel
    {

        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            //draw map
            for (int y = 0; y < map.getHeight(); y++)
            {
                for (int x = 0; x < map.getWidth(); x++)
                {
                    if (map.isFree(x, y))
                    {
                        drawPoint(g, new Point(x, y), Color.WHITE);
                    }
                    if (map.isVisited(x, y))
                    {
                        drawPoint(g, new Point(x, y), Color.YELLOW);
                    }
                    if (map.isObstacle(x, y))
                    {
                        drawPoint(g, new Point(x, y), Color.BLACK);
                    }
                    if (map.isObservedAsObstacle(x, y) && !map.isObstacle(x, y))
                    {
                        drawPoint(g, new Point(x, y), Color.BLUE);
                    }
                    if (map.isObstacle(x, y) && map.isObservedAsObstacle(x, y))
                    {
                        drawPoint(g, new Point(x, y), Color.MAGENTA);
                    }
                }
                drawBot(g2);
            }
        }

        private void drawBot(Graphics2D g2)
        {
            g2.setColor(RED);
            g2.fill(virtualBot.getContour());

            Point2D sensorRotationPoint = new Point2D.Double(virtualBot.getSensorRotationPoint().getX(), virtualBot.getSensorRotationPoint().getY());
            int sensorRotationDiameter = 2 * SENSOR_ROTATION_RADIUS;
            g2.setColor(BLUE);
            int x = round(sensorRotationPoint.getX() - SENSOR_ROTATION_RADIUS);
            int y = round(sensorRotationPoint.getY() - SENSOR_ROTATION_RADIUS);
            g2.fillOval(x, y, sensorRotationDiameter, sensorRotationDiameter);
        }

        private void drawPoint(Graphics g, Point point, Color color)
        {
            g.setColor(color);
            g.drawLine(point.x, point.y, point.x, point.y);
        }

        @Override
        public void paint(Graphics g)
        {
            BufferedImage im = new BufferedImage(this.getWidth(), this.getHeight(),
                    BufferedImage.TYPE_3BYTE_BGR);
            // Paint normally but on the image
            super.paint(im.getGraphics());

            // Reverse the image
            AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
            tx.translate(0, -im.getHeight());
            AffineTransformOp op = new AffineTransformOp(tx,
                    AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            im = op.filter(im, null);

            // Draw the reversed image on the screen
            g.drawImage(im, 0, 0, null);
        }
    }
}