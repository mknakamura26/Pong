import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.*;
import java.util.Random;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

class Pair{
    public double x;
    public double y;

    public Pair(double initX, double initY){
        x = initX;
        y = initY;
    }

    public Pair add(Pair toAdd){
        return new Pair(x + toAdd.x, y + toAdd.y);
    }

    public Pair divide(double denom){
        return new Pair(x / denom, y / denom);
    }

    public Pair times(double val){
        return new Pair(x * val, y * val);
    }

    public void flipX(){
        x = -x;
    }

    public void flipY(){
        y = -y;
    }
}


class Ball{
    Pair position;
    Pair velocity;
    Pair acceleration;
    double radius;
    double dampening;
    public Ball() {
        /*
        generate random number between 0 and 1, if it's 0, make ball go to right side. Otherwise, go to left side.
         */
        Random rand = new Random();
        position = new Pair(Pong.WIDTH/2, Pong.HEIGHT/2);
        int r = rand.nextInt(2);
        if (r == 0){
            velocity = new Pair(400, 0);
        }
        else {
            velocity = new Pair(-400, 0);
        }
        acceleration = new Pair(0.0, 0.0);
        radius = 30;
        dampening = 1.3;
    }

    public void update(World w, double time){
        position = position.add(velocity.times(time));
        velocity = velocity.add(acceleration.times(time));
        bounce(w);
    }

    public void setPosition(Pair p){
        position = p;
    }
    public void setVelocity(Pair v){
        velocity = v;
    }
    public void setAcceleration(Pair a){
        acceleration = a;
    }
    public Pair getPosition(){
        return position;
    }
    public Pair getVelocity(){
        return velocity;
    }

    public void drawBall(Graphics g){
        g.setColor(Color.WHITE);
        g.fillArc((int)(position.x- radius), (int)(position.y- radius), 2*(int) radius, 2*(int) radius, 0, 360);
    }

    private void bounce(World w){
        Boolean bounced = false;
        if (position.x - radius < 0){
            velocity.flipX();
            position.x = radius;
            bounced = true;
        }
        else if (position.x + radius > w.width){
            velocity.flipX();
            position.x = w.width - radius;
            bounced = true;
        }
        if (position.y - radius < 0){
            velocity.flipY();
            position.y = radius;
            bounced = true;
        }
        else if(position.y + radius > w.height){
            velocity.flipY();
            position.y = w.height - radius;
            bounced = true;
        }
        if (bounced){
            velocity = velocity.divide(dampening);
        }
    }
}



class Paddle{
    Pair position;
    Pair velocity;
    Pair acceleration;
    int width;
    int height;

    public Paddle(int posx, int posy){
        position = new Pair(posx, posy);
        velocity = new Pair(0, 0);
        acceleration = new Pair(0.0, 0.0);
        width = 25;
        height = 125;
    }
    public void drawPaddle(Graphics g, World w){
        g.setColor(Color.RED);
        g.fillRect((int) position.x, (int) position.y, width, height);
    }


    public void update(World w, double time) {
        position = position.add(velocity.times(time));
        velocity = velocity.add(acceleration.times(time));
    }
    public void setVelocity(Pair v){
        velocity = v;
    }
    public Pair getPosition(){
        return position;
    }

    public boolean restrictUpper(){

        // This method returns true if the paddle is about to go beyond the screen in the upward direction.

        if (getPosition().y <= 0){
            return true;
        }
        else return false;
    }

    public boolean restrictLower(){

        // Return false if paddle about to go out of screen in downward direction.

        if ( getPosition().y + height >= Pong.HEIGHT){
            return true;
        }
        else return false;
    }

}

class World {
    int height;
    int width;
    Ball ball;
    public Paddle leftpaddle;
    public Paddle rightpaddle;

    public World(int initWidth, int initHeight){
        width = initWidth;
        height = initHeight;
        ball = new Ball();
        leftpaddle = new Paddle(50, 300);
        rightpaddle = new Paddle(Pong.WIDTH - 75, 300);
    }

    public void drawWorld(Graphics g){
        ball.drawBall(g);
        leftpaddle.drawPaddle(g, this);
        rightpaddle.drawPaddle(g, this);
    }

    public void updateBall(double time){
        ball.update(this, time);
    }
    public void updatePaddle(double time){
        leftpaddle.update(this, time);
        rightpaddle.update(this, time);
    }
}




public class Pong extends JPanel implements KeyListener{
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 700;
    public static final int FPS = 60;
    public World w;
    public static int playerleft;
    public static int playerright;
    public static double rightY1;
    public static double leftY1;
    public boolean leftkeypressed;
    public boolean rightkeypressed;

    public Pong(){
        w = new World(WIDTH, HEIGHT);
        addKeyListener(this);
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        Thread mainThread = new Thread(new Pong.Runner());
        mainThread.start();
        playerleft = 0;
        playerright = 0;
        rightY1 = 250;
        leftY1 = 250;
    }

    public boolean restart(){
        // If the ball's position greater than width, give left player a point.
        // if ball's position less than 0, give right player point.

        if (w.ball.getPosition().x >= Pong.WIDTH - w.ball.radius){
            playerleft++;
            w.ball.setPosition(new Pair(Pong.WIDTH/2, Pong.HEIGHT/2));
            w.ball.setVelocity(new Pair(-400, 0));
            w.ball.setAcceleration(new Pair(0,0));
            return true;
        }
        if (w.ball.getPosition().x <= w.ball.radius){
            playerright++;
            w.ball.setPosition(new Pair(Pong.WIDTH/2, Pong.HEIGHT/2));
            w.ball.setVelocity(new Pair(400, 0));
            w.ball.setAcceleration(new Pair(0,0));
            return true;
        }
        return false;
    }

    class Runner implements Runnable{
        public void run() {

            while(true){

                if (w.ball.getPosition().x != WIDTH || w.ball.getPosition().x != 0) {
                    hit(w);
                }

                speedup();

                w.updateBall(1.0 / (double)FPS);
                w.updatePaddle(1.0 / (double)FPS);

                repaint();

                try{
                    Thread.sleep(1000/FPS);
                }
                catch(InterruptedException e){}
            }
        }
    }


    public void keyPressed(KeyEvent k) {
        char c = k.getKeyChar();

        // LEFT PADDLE MOVEMENT

        if (c == 'r' || c == 'v') {
            if (c == 'r') {
                Pair p = new Pair(0, -leftY1);
                w.leftpaddle.setVelocity(p);

                if (w.leftpaddle.restrictUpper()){
                    System.out.println("LEFT RESTRICT");
                    w.leftpaddle.setVelocity(new Pair(0,0));
                }

            }
            if (c == 'v') {
                Pair p = new Pair(0, leftY1);
                w.leftpaddle.setVelocity(p);

                if (w.leftpaddle.restrictLower()){
                    w.leftpaddle.setVelocity(new Pair(0,0));
                }
            }
            leftkeypressed = true;
        }



        // RIGHT PADDLE MOVEMENT

        if (c == 'u' || c == 'n') {

            if (c == 'u') {
                Pair p = new Pair(0, -rightY1);
                w.rightpaddle.setVelocity(p);

                if (w.rightpaddle.restrictUpper()){
                    System.out.println("LEFT RESTRICT");
                    w.rightpaddle.setVelocity(new Pair(0,0));
                }
            }
            if (c == 'n') {
                Pair p = new Pair(0, rightY1);
                w.rightpaddle.setVelocity(p);

                if (w.rightpaddle.restrictLower()){
                    System.out.println("LEFT RESTRICT");
                    w.rightpaddle.setVelocity(new Pair(0,0));
                }
            }
            rightkeypressed = true;
        }
    }

    public void speedup(){
        // This method increases the speed of the ball according to its sign as the game goes on.
        if (w.ball.getVelocity().x < 0){
            w.ball.velocity.x -= 1;
        }
        if (w.ball.getVelocity().x > 0){
            w.ball.velocity.x += 1;
        }
        if (w.ball.getVelocity().y < 0){
            w.ball.velocity.y -= 1;
        }
        if (w.ball.getVelocity().y > 0){
            w.ball.velocity.y += 1;
        }
    }



    public void keyReleased(KeyEvent e) {
        if (leftkeypressed){
            w.leftpaddle.setVelocity(new Pair(0,0));
            leftkeypressed = false;
        }
        if (rightkeypressed){
            w.rightpaddle.setVelocity(new Pair(0,0));
            rightkeypressed = false;
        }
    }

    public void keyTyped(KeyEvent e) {

    }

    public void addNotify() {
        super.addNotify();
        requestFocus();
    }


    public void paintComponent(Graphics g){
        super.paintComponent(g);

        g.setColor(Color.green);

        if (restart()) {
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }
        g.fillRect(0, 0, WIDTH, HEIGHT);
        w.drawWorld(g);

        g.setColor(Color.WHITE);
        g.drawLine(Pong.WIDTH/2, 0, Pong.WIDTH/2, Pong.HEIGHT);

        g.setColor(Color.BLACK);
        g.drawString("Player 1 score: " + playerleft, 200, 100);
        g.drawString("Player 2 score: " + playerright, 725, 100);
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("Pong");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Pong mainInstance = new Pong();
        frame.setContentPane(mainInstance);
        frame.pack();
        frame.setVisible(true);
    }

    public static boolean hit(World w) {

        // Calculating where the ball contacted the paddle on the left, finding the corresponding angle of where it should go afterwards.

        double leftcontact = (w.leftpaddle.getPosition().y + (w.leftpaddle.height / 2)) - (w.ball.getPosition().y);
        double left = leftcontact / (w.leftpaddle.height / 2);
        double leftangle = left * (5 * Math.PI / 12);

        // same for right paddle.

        double rightcontact = (w.rightpaddle.getPosition().y + (w.rightpaddle.height / 2)) - (w.ball.getPosition().y);
        double right = rightcontact / (w.rightpaddle.height / 2);
        double rightangle = right * (5 * Math.PI / 12);

        // LEFT PADDLE

        // The velocity is the square root of y velocity squared + x velocity squared.

        double vinitial = Math.sqrt((Math.pow(w.ball.getVelocity().x, 2)) + (Math.pow(w.ball.getVelocity().y, 2)));

        if (w.ball.getPosition().x - w.ball.radius <= w.leftpaddle.getPosition().x + (w.leftpaddle.width) &&
                w.ball.getPosition().y + (w.ball.radius) >= w.leftpaddle.getPosition().y &&
                w.ball.getPosition().y - w.ball.radius <= w.leftpaddle.getPosition().y + (w.leftpaddle.height)) {

            // If the ball goes past the x position of the paddle and hits on the paddle's y position, change the velocity.
            w.ball.setVelocity(new Pair((Math.cos(leftangle) * vinitial), -vinitial * Math.sin(leftangle)));

            // For x velocity, the cosine of a right triangle is adjacent / hypotenuse. The hypotenuse is vinitial and adjacent is x velocity.
            // Since I am trying to find x velocity, I multiplied the vinitial, since I want ball to bounce off paddle, by the cosine of the angle
            // that was found by the variable leftangle.

        }


        // Same for right paddle.

        if (w.ball.getPosition().x + w.ball.radius >= w.rightpaddle.getPosition().x &&
                w.ball.getPosition().y + (w.ball.radius) >= w.rightpaddle.getPosition().y &&
                w.ball.getPosition().y - (w.ball.radius) <= w.rightpaddle.getPosition().y + (w.rightpaddle.height)) {
            w.ball.setVelocity(new Pair((Math.cos(rightangle) * -vinitial), -vinitial * Math.sin(rightangle)));
        }

        return true;
    }
}