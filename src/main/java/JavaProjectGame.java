import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class JavaProjectGame extends JFrame implements KeyListener{
    private GameHandler handler;
    private JTextArea textArea = new JTextArea();

    public JavaProjectGame() {
        setTitle("Four In A Line Game");
        setSize(480, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //창을 닫았을때 main Thread도 닫기 위해
        setLocationRelativeTo(null); // this will center your app
        textArea.setFont(new Font("Courier New", Font.PLAIN, 20));
        textArea.addKeyListener(this);
        add(textArea); // textArea를 JFrame에 추가
        textArea.setEditable(false); //키보드로 textArea를 수정할 수 없도록 설정
        setVisible(true); //JFrame을 눈에 보이게 하기 위해서

        handler = new GameHandler(textArea); //GameHandler내에서 textArea를 접근하기 위해 레퍼런스를 인자로 받아온다.
        new Thread(new GameThread()).start(); // JFrame과 game loop이 별도의 thread에서 실행되도록 한다.
    }

    public static void main(String[] args) {
        new JavaProjectGame();
    }

    public void restart() {
        handler.initData();
        new Thread(new GameThread()).start();
    }

    class GameThread implements Runnable{ //Runnable interface 상속
        @Override
        public void run() {
            // game loop
            while (!handler.isGameOver()) {
                // 1. Game timing ================================
                handler.gameTiming();
                // 3. Game logic ==================================
                //handler.gameLogic();
                // 4. Render output ==============================
                handler.drawAll();
            }
            //game over

            handler.drawGameOver();
        }
    }
    // 2. Get Input ======================================
    //implements KeyListener <- 인터페이스를 상속했기 때문에 implements & override를 해야한다.
    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_RIGHT: // Right key pressed
                handler.moveRight();
                break;
            case KeyEvent.VK_LEFT: // Left key pressed
                handler.moveLeft();
                break;
            case KeyEvent.VK_DOWN: // Down key pressed
                handler.moveDown();
                //handler.isGameOver();
                break;
            case KeyEvent.VK_Y: // Y key pressed
                if(handler.isGameOver())
                    restart();
                break;
            case KeyEvent.VK_N: // N key pressed
                if(handler.isGameOver())
                    System.exit(0);
        }
    }
    @Override
    public void keyTyped
            (KeyEvent e) {
    }
    @Override
    public void keyReleased
            (KeyEvent e) {
    }

}
