import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    //images
    Image backgroundImg;
    Image birdImg;
    Image tuboSuperiorImg;
    Image tuboInferiorImg;

    //Passaro classe
    int birdX = boardWidth/8;
    int birdY = boardWidth/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    //tubo classe
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  //escala de 1/6
    int pipeHeight = 512;
    
    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    //Lógica do Jogo
    Bird bird;
    int velocityX = -4; //velocidade para mover os tubos para a esquerda (simula o pássaro movendo para a direita)
    int velocityY = 0; //velocidade para mover o pássaro para cima/baixo.
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.blue);
        setFocusable(true);
        addKeyListener(this);

        //carregar imagens
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        tuboSuperiorImg = new ImageIcon(getClass().getResource("./tubosuperior.png")).getImage();
        tuboInferiorImg = new ImageIcon(getClass().getResource("./tuboinferior.png")).getImage();

        //bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        //temporizador para posicionar tubos
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              // Code to be executed
              placePipes();
            }
        });
        placePipeTimer.start();
        
		//game timer
		gameLoop = new Timer(1000/60, this); //tempo para iniciar o temporizador, milissegundos entre frames 
        gameLoop.start();
	}
    
    void placePipes() {
        //(0-1) * pipeHeight/2.
        // 0 -> -128 (pipeHeight/4)
        // 1 -> -128 - 256 (pipeHeight/4 - pipeHeight/2) = -3/4 pipeHeight
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;
    
        Pipe topPipe = new Pipe(tuboSuperiorImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);
    
        Pipe bottomPipe = new Pipe(tuboInferiorImg);
        bottomPipe.y = topPipe.y  + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }
    
    
    @Override
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public void draw(Graphics g) {
        //background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        //bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        //pontuação
        g.setColor(Color.white);

        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        }
        else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
        
	}

    public void move() {
        //bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); //aplicar gravidade à posição atual do passaro.y, limitar o passaro.y ao topo da tela

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; //0.5 porque há 2 tubos! então 0.5*2 = 1, 1 para cada conjunto de tubos
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   //o canto superior esquerdo de 'a' não alcança o canto superior direito de 'b'
               a.x + a.width > b.x &&   //o canto superior direito de 'a' ultrapassa o canto superior esquerdo de 'b'
               a.y < b.y + b.height &&  //o canto superior esquerdo de 'a' não alcança o canto inferior esquerdo de 'b'
               a.y + a.height > b.y;    //o canto inferior esquerdo de 'a' ultrapassa o canto superior esquerdo de 'b'
    }

    @Override
    public void actionPerformed(ActionEvent e) { //chamado a cada x milissegundos pelo temporizador gameLoop
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }  

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
             // System.out.println("PULAR!");
            velocityY = -9;

            if (gameOver) {
                //reiniciar o jogo redefinindo as condições
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    //não necessário
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
