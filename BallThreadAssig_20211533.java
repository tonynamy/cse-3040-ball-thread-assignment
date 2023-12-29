import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BallThreadAssig_20211533 extends Frame {

    public static void main(String[] args) {
        new BallThreadAssig_20211533();
    }

    public BallThreadAssig_20211533() {

        super("BallThreadAssig_20211533");

        this.setLayout(new BorderLayout());
        Balls balls = new Balls();
        this.add(balls);
        this.setSize(600, 600);
        this.setVisible(true);
        WindowDestroyer listener = new WindowDestroyer(); // window destroy button
        this.addWindowListener(listener);

        new Thread(new BounceEngine(balls)).start();
    }

    public static int random(int maxRange) {
        return (int) Math.round((Math.random() * maxRange));
    }

    public class Balls extends Panel {

        private ArrayList<Ball> ballsUp;
        private ArrayList<Ball> newBalls = new ArrayList<>();

        public Balls() {
            ballsUp = new ArrayList<Ball>();

            int wC = 600 / 2;
            int hC = 600 / 2;

            double radius = 16;
            double speed = 5;
            double rp = 20;

            for(int i=0;i<360;i+=72) {
                double r = Math.toRadians(i);

                int x = (int) (wC + (rp * Math.cos(r)));
                int y = (int) (hC + (rp * Math.sin(r)));

                int vx = (int) (speed * Math.cos(r));
                int vy = (int) (speed * Math.sin(r));

                ballsUp.add(new Ball() {{
                    setLocation(new Point(x, y));
                    setSpeed(new Point(vx, vy));
                    setSize(new Dimension((int) radius, (int) radius));
                }});

            }

            setPreferredSize(new Dimension(400,400));

            setVisible(true);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            for (Ball ball : ballsUp) {
                ball.paint(g);
            }
            g.dispose();
        }


        public List<Ball> getBalls() {
            return ballsUp;
        }

        public void addNewBalls() {
            ballsUp.clear();
            ballsUp.addAll(newBalls.stream().filter(Ball::isAlive).toList());
            newBalls.clear();
        }

        public void addNewBall(Ball ball) {
            newBalls.add(ball);
        }
    }

    public class BounceEngine implements Runnable {

        private Balls parent;

        public BounceEngine(Balls parent) {
            this.parent = parent;
        }

        @Override
        public void run() {

            int width = getParent().getWidth();
            int height = getParent().getHeight();

            int phase = 0;

            while (getParent().isVisible()) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        getParent().repaint();
                        for (Ball ball : getParent().getBalls()) {
                            move(ball);
                        }
                        getParent().addNewBalls();
                    }
                });


                // Some small delay...
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }

            }

        }

        public Balls getParent() {
            return parent;
        }

        public void move(Ball ball) {

            Point p = ball.getLocation();
            Point speed = ball.getSpeed();
            Dimension size = ball.getSize();

            int vx = speed.x;
            int vy = speed.y;

            int x = p.x;
            int y = p.y;

            synchronized (getParent().getBalls()) {
                List<Ball> balls = getParent().getBalls();
                for(Ball other : balls) {

                    if(ball.equals(other)) continue;

                    Point p1 = ball.getLocation();
                    Point v1 = ball.getSpeed();
                    Dimension s1 = ball.getSize();

                    Point p2 = other.getLocation();
                    Point v2 = other.getSpeed();
                    Dimension s2 = other.getSize();

                    boolean isCollide = isCollide(ball, other);
                    if(isCollide) {
                        vx *= -1;
                        vy *= -1;

                        ball.setAlive(false);

                        int newRadius = s1.width/2;

                        double speedScalar = Math.sqrt(vx*vx + vy*vy) * Math.cos(Math.PI / 4);
                        double angle = Math.atan2(vy, vx);

                        int v1x = (int) (speedScalar * Math.cos(angle + Math.PI / 4));
                        int v1y = (int) (speedScalar * Math.sin(angle + Math.PI / 4));

                        int v2x = (int) (speedScalar * Math.cos(angle - Math.PI / 4));
                        int v2y = (int) (speedScalar * Math.sin(angle - Math.PI / 4));

                        Ball newBall1 = new Ball();

                        newBall1.setSpeed(new Point(v1x, v1y));
                        newBall1.setLocation(new Point(x+v1x*10, y+v1y*10));
                        newBall1.setSize(new Dimension(newRadius, newRadius));

                        Ball newBall2 = new Ball();

                        newBall2.setSpeed(new Point(v2x, v2y));
                        newBall2.setLocation(new Point(x+v2x*10, y+v2y*10));
                        newBall2.setSize(new Dimension(newRadius, newRadius));

                        getParent().addNewBall(newBall1);
                        getParent().addNewBall(newBall2);

                        return;
                    }

                    if (x + vx < 0 || x + size.width + vx > getParent().getWidth()) {
                        vx *= -1;
                    }
                    if (y + vy < 0 || y + size.height + vy > getParent().getHeight()) {
                        vy *= -1;
                    }

                    x += vx;
                    y += vy;


                }

            }

            int finalVx = vx;
            int finalX = x;
            int finalVy = vy;
            int finalY = y;

            getParent().addNewBall(new Ball(){{
                setSpeed(new Point(finalVx, finalVy));
                setLocation(new Point(finalX, finalY));
                setSize(ball.getSize());
            }});
        }

        public Boolean isCollide(Ball a, Ball b) {

            Point p1 = a.getLocation();
            Point v1 = a.getSpeed();
            Dimension s1 = a.getSize();

            Point p2 = b.getLocation();
            Point v2 = b.getSpeed();
            Dimension s2 = b.getSize();

            return (p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y) <= (s1.width/2 + s2.width/2)*(s1.width/2 + s2.width/2);
        }
    }

    public class Ball {

        private Color color;
        private Point location;
        private Dimension size;
        private Point speed;
        private boolean isAlive = true;

        public boolean isAlive() {
            if(getSize().width <= 1 || (getSpeed().x == 0 && getSpeed().y == 0)) setAlive(false);
            return isAlive;
        }

        public void setSize(Dimension size) {
            this.size = size;
        }

        public void setAlive(boolean alive) {
            isAlive = alive;
        }

        public Ball() {

            setColor(new Color(0, 0, 0));

        }

        public Dimension getSize() {
            return size;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void setLocation(Point location) {
            this.location = location;
        }

        public Color getColor() {
            return color;
        }

        public Point getLocation() {
            return location;
        }

        public Point getSpeed() {
            return speed;
        }

        public void setSpeed(Point speed) {
            this.speed = speed;
        }

        protected void paint(Graphics g2d) {

            Point p = getLocation();
            if (p != null) {
                g2d.setColor(getColor());
                Dimension size = getSize();
                g2d.fillOval(p.x, p.y, size.width, size.height);
            }

        }
    }
    public class WindowDestroyer extends WindowAdapter {

        public void windowClosing(WindowEvent e) {
            System.exit(0); // 시스템 종료
        }
    }
}