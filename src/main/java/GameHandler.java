import java.awt.*;
import javax.swing.*;
import java.io.*; //버퍼사용

public class GameHandler { //게임의 logic을 담당
    private final int SCREEN_WIDTH = 30;
    private final int SCREEN_HEIGHT = 15;
    private final int FIELD_WIDTH = 7, FIELD_HEIGHT = 6;
    private final int LEFT_PADDING = 3; //왼쪽 띄우기 정도
    //private final int PADDING = 3; //위쪽 공간 띄우기 용도
    private final int blank = 2; //필드에서 공백으로 2줄 설정

    private char stone[]; //흰돌, 검은 돌 저장
    private int num; //흰돌, 검은 돌 구별
    private int temp; //짝수 홀수로 돌 순서 판단
    private int currentX, currentY; //처음 돌 위치
    private int currentScore[], previousScore[]; //점수 저장

    private boolean isGameOver;

    private JTextArea textArea;
    private String[][] buffer;
    private String[][] field;


    public GameHandler(JTextArea ta) {
        textArea = ta; //JFrame에 있는 textArea
        field = new String[FIELD_WIDTH][blank + FIELD_HEIGHT];
        buffer = new String[SCREEN_WIDTH][SCREEN_HEIGHT];
        stone = new char[2];
        currentScore = new int[2];
        previousScore = new int[2];

        stone[0] = '●';
        stone[1] = '○';

        currentScore[0] = 0;
        currentScore[1] = 0;

        previousScore[0] = 0;
        previousScore[1] = 0;

        initData();

        try { //파일이 있으면 파일을 읽어온다.
            BufferedReader in = new BufferedReader(new FileReader("previousScore.txt"));
            previousScore[0] = Integer.parseInt(in.readLine());
            previousScore[1] = Integer.parseInt(in.readLine());
            in.close();
        } catch (FileNotFoundException e) { //파일이 없을 경우 점수를 0으로 설정한다.
            previousScore[0] = 0;
            previousScore[1] = 0;
        } catch (IOException e) { //파일 오류시
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void initData() {
        //돌 놓는 판 구역에 기호 할당
        for (int x = 0; x < FIELD_WIDTH; x++) {
            for (int y = 0; y < blank + FIELD_HEIGHT; y++) {
                field[x][y] = " ";
            }
        }
        for (int x = 0; x < FIELD_WIDTH; x++) {
            for (int y = blank; y < blank + FIELD_HEIGHT; y++) {
                field[x][y] = "┼─";
            }
        }

        for(int x = 0; x < FIELD_WIDTH; x++) {
            field[x][blank] = "┬─";
            field[x][blank + FIELD_HEIGHT-1] = "┴─";
        }

        for(int y = blank; y < blank + FIELD_HEIGHT; y++) {
            field[0][y] = "├─";
            field[FIELD_WIDTH-1][y] = "┤";
        }

        field[0][blank] = "┌─";
        field[FIELD_WIDTH-1][blank] = "┐";
        field[0][blank + FIELD_HEIGHT-1] = "└─";
        field[FIELD_WIDTH-1][blank + FIELD_HEIGHT-1] = "┘";

        temp = 1; //짝수 홀수로 돌 판단

        currentX = 3*2; //돌 처음 위치 값 지정 , 4번째줄 함수 상으로 3번째
        //내가 원하는 n번째 줄 -> LEFT_PADDING + n*2...
        currentY = 1;

        isGameOver = false;
        clearBuffer();

        //파일 읽는 부분의 위치 수정, restart할 떄마다 자동으로 현재 점수와 동기화됨
        //restart()로 초기화 할 경우에는 파이 읽기를 안하는 방향으로?

    }

    public void gameTiming() {
        // Game tick
        try{
            Thread.sleep(50);
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void clearBuffer() {
        for(int y = 0; y < SCREEN_HEIGHT; y++) {
            for(int x = 0; x < SCREEN_WIDTH; x++) {
                buffer[x][y] = " ";
            }
        }
    }

    private void drawToBuffer (int px, int py, String c) {//보드판 출력
        buffer[px + LEFT_PADDING][py] = c;
    }

    private void drawToBuffer (int px, int py, char c) {//돌 출력 용도의 버퍼
        buffer[px+ LEFT_PADDING][py] = String.valueOf(c); //char를 String로 형변환
    }

    public void drawGameOver() {
        currentScore[num] += 1; //해당 턴의 돌의 점수를 +1 한다.
        drawToBuffer(8, 9,  "╔════════════════╗");
        drawToBuffer(8, 10, "║     " + stone[num] + " WIN!     ║");
        drawToBuffer(8, 11, "║                ║");
        drawToBuffer(8, 12, "║  AGAIN? (Y/N)  ║");
        drawToBuffer(8, 13, "╚════════════════╝");

        render();
        BufferedWriter out;

        try { //점수 저장
            out = new BufferedWriter(new FileWriter("previousScore.txt"));
            out.write(String.valueOf(currentScore[0])); //현재 검은돌, 흰돌 점수 저장
            out.newLine();
            out.write(String.valueOf(currentScore[1]));
            out.close();
        } catch (IOException e) { //파일 오류의 경우
            e.printStackTrace();
        }
    }

    public boolean isGameOver() {

        num = (temp % 2 == 0) ? 0 : 1;
        //key pressed down 할때 temp++ 해주면서 다시 isGameOver()검사할 때 찍힌 돌 말고 다른 돌을 검사하기 때문에
        //num값을 기존과 반대로 했다가 검사 끝나면 다시 돌려놓는다.
        // 가로(→) 방향 체크
        for (int x = 0; x < 4; x++) { //0 ~ 3 까지 체크 , 3일때 마지막 줄에 닿는다. [0][1][2]/[3][4][5][6]
            for(int y = blank; y < blank + FIELD_HEIGHT; y++) {//blank ~ blank + FIELD_HEIGHT-1 (2~7)
                if(field[x][y].equals(stone[num] + "─") && field[x+1][y].equals(stone[num] + "─") && field[x+2][y].equals(stone[num] + "─") &&
                        (field[x+3][y].equals(stone[num] + "─") || field[x+3][y].equals(stone[num])))
                    return true;
            }
        }
        // 세로(↓) 방향 체크
        for (int x = 0; x < FIELD_WIDTH; x++) { //6번째 세로줄과 0~5번째 세로줄
            for(int y = blank; y < blank + 3; y++) {// +3 = 7, blank ~ 4
                if((field[x][y].equals(stone[num] + "─") && field[x][y+1].equals(stone[num] + "─") && field[x][y+2].equals(stone[num] + "─") && field[x][y+3].equals(stone[num] + "─"))||
                        (field[x][y].equals(stone[num]) && field[x][y+1].equals(stone[num]) && field[x][y+2].equals(stone[num]) && field[x][y+3].equals(stone[num])))
                    return true;
            }
        }
        // 대각선(↘) 방향 체크
        for (int x = 0; x < 4; x++)
            for(int y = blank; y < blank + 3; y++) {//[0][0] [1][1] [2][2] [3][3]
                if(field[x][y].equals(stone[num]) && field[x+1][y+1].equals(stone[num] + "─") &&
                        field[x+2][y+2].equals(stone[num] + "─") && field[x+3][y+3].equals(stone[num] + "─")|| //0번째 줄 부터 시작하는 (↘)
                        field[x][y].equals(stone[num] + "─") && field[x+1][y+1].equals(stone[num] + "─") && field[x+2][y+2].equals(stone[num] + "─") &&
                                (field[x+3][y+3].equals(stone[num] + "─") || field[x+3][y+3].equals(stone[num])))//1 ~ 3번째 줄부터 시작하는 경우
                    return true;
            }
        // 대각선(↙) 방향 체크
        for (int x = 3; x < FIELD_HEIGHT; x++) { //3 ~ 6 범위
            for (int y = blank; y < blank + 3; y++) {//[6][0], [5][1], [4][2], [3][3] / [3][2], [2][3], [1][4], [0][5]
                if(field[x][y].equals(stone[num] + "─") && field[x-1][y+1].equals(stone[num] + "─") && field[x-2][y+2].equals(stone[num] + "─") &&
                        (field[x-3][y+3].equals(stone[num] + "─") || field[x-3][y+3].equals(stone[num]))||//3 ~ 5번째 줄 대각선(↙)
                        field[x][y].equals(stone[num]) && field[x-1][y+1].equals(stone[num] + "─") &&
                                field[x-2][y+2].equals(stone[num] + "─") && field[x-3][y+3].equals(stone[num] + "─")) //6번째 줄 대각선(↙)
                    return true;
            }
        }
        num = (temp % 2 == 0) ? 1 : 0;

        return isGameOver;
    }

    private boolean stoneRange(int cx) {
        if(cx >= 0 && cx <= 6*2){ //0번째부터 6번째줄까지 이동가능하게
            return true;
        }
        return false;
    }

    public void moveLeft() {
        if(stoneRange(currentX - 2)) {
            drawToBuffer(currentX , currentY ," ");
            currentX -=2;
        }
    }
    public void moveRight() {
        if(stoneRange(currentX + 2)) {
            drawToBuffer(currentX , currentY ," ");
            currentX +=2;
        }
    }
    public void moveDown() { //해당 x좌표에 돌이 있는지 없는지 판단한다. y값으로 반복문 돌면서 판단한다.
        for(int y = blank + FIELD_HEIGHT - 1; y >= blank - 1; y--) {
            if(!field[currentX/2][y].equals("●") && !field[currentX/2][y].equals("●─") &&
                    !field[currentX/2][y].equals("○") && !field[currentX/2][y].equals("○─")) {
                field[currentX/2][y] = currentX/2 != 6 ? stone[num] + "─" : String.valueOf(stone[num]);
                break;
            }
        }
        temp++;
    }
    /*
    public void inputKey(){
        if(GetAsyncKeyState(VK_LEFT) & (1 << 15))
    }
     */
    public void drawAll() {
        //보드 판 출력
        for (int x = 0; x < FIELD_WIDTH; x++) {
            for (int y = 0; y < blank + FIELD_HEIGHT; y++) {
                drawToBuffer(x, y, field[x][y]);
            }
        }

        drawToBuffer(currentX , currentY , stone[num]);
        //디버깅해서 돌이 이동하고 이전에 위치했던 공간에 들어있는것을 지우는 것으로
        drawCurrentScore();
        drawPreviousScore();

        render();
    }
    private void drawCurrentScore() {

        drawToBuffer(FIELD_WIDTH + 3, 2, "┌────CURRENT────┐");
        drawToBuffer(FIELD_WIDTH + 3, 3, "│   "+ "●: " + currentScore[0] + " ○: " + currentScore[1] + "   │");
        drawToBuffer(FIELD_WIDTH + 3, 4, "└───────────────┘");

    }
    private void drawPreviousScore() {

        drawToBuffer(FIELD_WIDTH + 3, 5, "┌────PREVIOUS───┐");
        drawToBuffer(FIELD_WIDTH + 3, 6, "│   "+ "●: " + previousScore[0] + " ○: " + previousScore[1] + "   │");
        drawToBuffer(FIELD_WIDTH + 3, 7, "└───────────────┘");
    }
    private void render() { //buffer에 있는 내용을 textArea에 setText()해야한다.
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < SCREEN_HEIGHT; y++) {
            for (int x = 0; x < SCREEN_WIDTH; x++) {
                sb.append(buffer[x][y]);
            }
            sb.append("\n");
        }
        textArea.setText(sb.toString());
    }
}
