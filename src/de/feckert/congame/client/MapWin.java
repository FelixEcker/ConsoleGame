package de.feckert.congame.client;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

public class MapWin extends JFrame implements Runnable {
    private Canvas cvs;

    public MapWin(String title) {
        super(title);

        setSize(Client.world.width*16, Client.world.height*16);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.cvs = new Canvas();
        cvs.setSize(getSize());

        add(cvs);
        setVisible(true);
    }

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        while (Client.world.map == null) {}

        while (true) {
            BufferStrategy bs = cvs.getBufferStrategy();
            if (bs == null) {
                cvs.createBufferStrategy(3);
                continue;
            }

            Graphics g = bs.getDrawGraphics();
            g.clearRect(0, 0, getWidth(), getHeight());

            /*

			'█', Ansi.GREEN,
			'▓', Ansi.GREEN,
			'▒', Ansi.GREEN,
			'~', Ansi.WHITE+Ansi.BLUE_BACKGROUND,
			'^', Ansi.WHITE+Ansi.BLACK_BACKGROUND
             */

            for (int y = 0; y < Client.world.height; y++) {
                for (int x = 0; x < Client.world.width; x++) {
                    if (Client.world.isFieldCP(x, y)) {
                        int g1 = 200*Client.world.capturePoint(x, y).owner;
                        int b = 200*Client.world.capturePoint(x, y).owner;
                        g.setColor(new Color(125, Math.min(g1, 255), Math.min(b, 255)));
                        g.fillRect(x, y, 1, 1);
                        continue;
                    }
                    switch (Client.world.map[y][x]) {
                        case '█' -> g.setColor(new Color(0, 255, 0));
                        case '▓' -> g.setColor(new Color(100, 255, 100));
                        case '▒' -> g.setColor(new Color(150, 255, 150));
                        case '~' -> g.setColor(new Color(0, 0, 255));
                        case '^' -> g.setColor(new Color(33, 33, 33));
                    }
                    g.fillRect(x, y, 1, 1);
                }
            }

            g.dispose();
            bs.show();
        }
    }
}
