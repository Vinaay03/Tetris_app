package com.example.tetris;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Tetris extends AppCompatActivity {
    DrawView dv;
    TetrisState ts;
    int rows, columns;
    int gameBgColor;

    Button pauseBtn;
    boolean savedGame;
    Button leftBtn;
    Button rightBtn;
    Button rotateBtn;
    Button downBtn;
    Button dropBtn;

    Button newGameBtn;
    Button goSavedGameBtn;

    Button restartBtn;

    TextView scoreText;

    Runnable runnable;
    Handler handler;
    boolean isGameLayout;
    int cycles;
    int maxCycles;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(((DataManager) getApplication()).userWantsToSave() && !ts.gameOver() && isGameLayout) {
            String s = stateToString();
            try {
                ((DataManager) getApplication()).saveGameState(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if(((DataManager) getApplication()).userWantsToSave() && !ts.gameOver() && isGameLayout) {
            String s = stateToString();
            try {
                ((DataManager) getApplication()).saveGameState(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    /**
     * Devuelve un string que contiene toda la información del juego actual
     * @return
     */
    private String stateToString() {
        String state = "";

        //  AÑADIMOS LOS COLORES DE TODOS LOS BLOQUES DEL TABLERO EXCEPTO LOS DE LA FIGURA ACTIVA
        for(int i = 0; i < ts.rows; ++i){
            for (int j = 0; j < ts.columns; j++) {
                if(!isActivePosition(new Point(i, j))) {    //  no es bloque de figura activa
                    state += String.valueOf(blockColorAt(new Point(i, j)));
                }
                else{
                    state += "-1";
                }

                if(j != ts.columns-1)
                    state += " ";
            }
            if(i != ts.rows-1)
                state += "\n";
        }

        //  RECOPILAMOS LAS POSICIONES DE LOS BLOQUES QUE CONFORMAN A LA FIGURA ACTIVA (se utiliza abajo)
        String savedGameActiveFigPos = "";
        BoardBlock[] activeBlocks = ts.getActiveFigure().figBlocks;
        for (int i = 0; i < activeBlocks.length; i++) {
            savedGameActiveFigPos += activeBlocks[i].position.j + "x" + activeBlocks[i].position.i;
            if(i != activeBlocks.length-1){
                savedGameActiveFigPos += " ";
            }
        }

        //  AÑADIMOS EL TIPO DE LA FIGURA ALMACENADA PARA INTERCAMBIAR Y EL INDICADOR DE FIGURA YA INTERCAMBIADA
        if(ts.getStoredFigure() != null){
            state += "\nStoredFigureType=" + ts.getStoredFigure().figType;
            state += "\nAlreadyStored=" + ts.alreadyStored;
        }

        //  AÑADIMOS EL TIPO DE LA FIGURA ACTIVA, POSICIONES DE LA FIGURA ACTIVA, LA PUNTUACIÓN Y LAS DIMENSIONES DEL TABLERO
        state += "\nActiveFigureType=" + ts.getActiveFigure().figType;
        state += "\nActiveFigurePos=" + savedGameActiveFigPos;
        state += "\nGameScore=" + String.valueOf(ts.getScore());
        state += "\nRows=" + String.valueOf(ts.rows) + " Columns=" + String.valueOf(ts.columns);

        return state;
    }

    /**
     * Genera un nuevo juego a partir del estado pasado como parametro
     * @param s
     */
    private void stringToState(String s) {
        //  SEPARAMOS CADA LINEA DEL STRING QUE REPRESENTA EL ESTADO DEL JUEGO GUARDADO
        String[] lines = s.split("\n");

        //  OBTENEMOS LAS DIMENSIONES DEL TABLERO DE JUEGO GUARDADO
        String[] boardSize = lines[lines.length-1].split(" ");
        String r = boardSize[0].substring("Rows=".length());
        String c = boardSize[1].substring("Columns=".length());
        int savedGameRows = Integer.parseInt(r);
        int savedGameColumns = Integer.parseInt(c);

        //  INICIALIZAMOS EL NUEVO ESTADO DE JUEGO EN BASE AL JUEGO GUARDADO
        ts = new TetrisState(savedGameRows, savedGameColumns);

        /*for(int i = 0; i < lines.length; ++i){
            System.out.println(String.valueOf(i) + " " + lines[i]);
        }*/

        //  LAS savedGameRows PRIMERAS LINEAS INDICAN LOS COLORES DE CADA POSICIÓN DEL TABLERO DEL JUEGO GUARDADO

        //  DESDE LA LINEA EN LA QUE EMPIEZAN LOS DATOS DEL JUEGO GUARDADO:
        for(int i = savedGameRows; i <lines.length; ++i){
            //  OBTENEMOS EL TIPO DE LA FIGURA ALMACENADA PARA INTERCAMBIAR
            if(lines[i].contains("StoredFigureType=")){
                String storedFigType = lines[i].substring("StoredFigureType=".length());
                TetrisFigure.FigureType storedFigureType = stringToFigType(storedFigType);
                ts.storedFig = new TetrisFigure(storedFigureType, ts, true);
            }
            //  OBTENEMOS EL INDICADOR DE FIGURA YA ALMACENADA
            else if(lines[i].contains("AlreadyStored=")){
                String storedGameAlreadyStoredFig = lines[i].substring("AlreadyStored=".length());
                ts.alreadyStored = Boolean.parseBoolean(storedGameAlreadyStoredFig);
            }
            //  OBTENEMOS EL TIPO DE LA FIGURA ACTIVA
            else if(lines[i].contains("ActiveFigureType=")){
                String activeFigType = lines[i].substring("ActiveFigureType=".length());
                TetrisFigure.FigureType activeFigureType = stringToFigType(activeFigType);
                ts.activeFig = new TetrisFigure(activeFigureType, ts, true);
            }
            //  OBTENEMOS LAS POSICIONES DE LOS BLOQUES QUE CONFORMAN A LA FIGURA ACTIVA
            else if(lines[i].contains("ActiveFigurePos=")){
                String[] blocksPositions = lines[i].substring("ActiveFigurePos=".length()).split(" ");
                for (int j = 0; j < blocksPositions.length; j++) {
                    String[] blockPos = blocksPositions[j].split("x");
                    int xPos = Integer.parseInt(blockPos[0]);
                    int yPos = Integer.parseInt(blockPos[1]);
                    ts.activeFig.figBlocks[j].position.j = xPos;
                    ts.activeFig.figBlocks[j].position.i = yPos;
                }
            }
            //  OBTENEMOS LA PUNTUACIÓN DEL JUEGO QUE SE HA GUARDADO
            else if(lines[i].contains("GameScore=")){
                ts.score = Integer.parseInt(lines[i].substring("GameScore=".length()));
            }
        }


        //  LAS savedGameRows PRIMERAS LINEAS INDICAN LOS COLORES DE CADA POSICIÓN DEL TABLERO DEL JUEGO GUARDADO
        //  ASIGNAMOS LOS COLORES DE CADA POSICIÓN DEL TABLERO AL TABLERO DEL NUEVO JUEGO
        //System.out.println("=============SAVED BOARD COLORS=========");
        for(int i = 0; i < savedGameRows; ++i){
            //System.out.print(String.valueOf(i) + " ");
            String[] blocksColors = lines[i].split(" ");
            for (int j = 0; j < blocksColors.length; j++) {
                int blockColor = Integer.parseInt(blocksColors[j]);
                ts.board[i][j].color = blockColor;
                if(blockColor != -1 && !isActivePosition(new Point(i, j))){
                    ts.board[i][j].state = BoardBlock.BlockState.FILLED;
                }
                //System.out.print(blocksColors[j] + " ");
            }
            //System.out.println();
        }
        //System.out.println("=================================");

        ts.pauseGame(true);
        dv = new DrawView(this, ts);
        dv.setBackgroundColor(gameBgColor);
        setBoardTouchEvent(dv);
    }

    /**
     * Devuelve el color de la posición indicada
     * @param p
     * @return
     */
    private int blockColorAt(Point p){
        for(BoardBlock b: ts.getActiveFigure().figBlocks){
            if(b.position.j == p.j && b.position.i == p.i){
                return b.color;
            }
        }
        return ts.getBoardBlockAt(p).color;
    }

    /**
     * Devuelve true si la posición indicada pertenece a algun bloque de la figura actica
     * @param p
     * @return
     */
    private boolean isActivePosition(Point p){
        for(BoardBlock b: ts.getActiveFigure().figBlocks){
            if(b.position.j == p.j && b.position.i == p.i){
                return true;
            }
        }
        return false;
    }

    private TetrisFigure.FigureType stringToFigType(String s) {
        TetrisFigure.FigureType figType = TetrisFigure.FigureType.SQUARE_SHAPED;
        switch (s){
            case "SQUARE_SHAPED":
                figType = TetrisFigure.FigureType.SQUARE_SHAPED;
                break;
            case "LINE_SHAPED":
                figType = TetrisFigure.FigureType.LINE_SHAPED;
                break;
            case "T_SHAPED":
                figType = TetrisFigure.FigureType.T_SHAPED;
                break;
            case "L_SHAPED":
                figType = TetrisFigure.FigureType.L_SHAPED;
                break;
            case "INV_L_SHAPED":
                figType = TetrisFigure.FigureType.INV_L_SHAPED;
                break;
            case "Z_SHAPED":
                figType = TetrisFigure.FigureType.Z_SHAPED;
                break;
            case "INV_Z_SHAPED":
                figType = TetrisFigure.FigureType.INV_Z_SHAPED;
                break;
        }
        return figType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rows = 16;
        columns = 10;
        gameBgColor = Color.rgb(200, 200, 200);
        ts = new TetrisState(rows, columns);
        dv = new DrawView(this, ts);
        dv.setBackgroundColor(gameBgColor);
        setBoardTouchEvent(dv);

        //  SI EL USUARIO QUIERE GUARDAR PARTIDAS
        if(((DataManager)getApplication()).userWantsToSave()){
            //  ASIGNAMOS UN MANAGER POR DEFECTO SI NO HA SELECCIONADO UNO
            if(!((DataManager)getApplication()).tetrisManagerSelected()) {
                ((DataManager) getApplication()).setTetrisManager(0);
            }

            //  SI HAY UN JUEGO GUARDADO
            if (((DataManager) getApplication()).hasGameState()) {
                System.out.println("EXISTE UN ESTADO GUARDADO");
                setContentView(R.layout.activity_ask_to_recover);
                isGameLayout = false;

                newGameBtn = ((Button) findViewById(R.id._newGame));
                newGameBtn.setText("No");
                newGameBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            ((DataManager) getApplication()).deleteGameState();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        startGame();
                    }
                });

                goSavedGameBtn = ((Button) findViewById(R.id._continueGame));
                goSavedGameBtn.setText("Yes");
                goSavedGameBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String state = "";
                        try {
                            state = ((DataManager) getApplication()).getGameState();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        stringToState(state);
                        savedGame = true;
                        startGame();
                    }
                });
            }
            //  SI NO HAY UN JUEGO GUARDADO
            else{
                startGame();
            }
        }
        //  SI EL USUARIO NO QUIERE GUARDAR PARTIDAS
        else{
            startGame();
        }
    }

    private void startGame(){
        setContentView(R.layout.activity_tetris);
        isGameLayout = true;

        setGameButtons();

        long delay = 1;
        maxCycles = 30;
        cycles = 0;
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if(!ts.gamePaused()) {
                    if (!ts.gameOver()) {
                        if(cycles < maxCycles){
                            ++cycles;
                        }
                        //  TRAS cycles*delay UNIDADES DE TIEMPO, SE MOVERÁ LA FIGURA ACTIVA 1 POSICIÓN ABAJO
                        else {
                            boolean canMoveDown = ts.moveActiveFigureDown();
                            if (!canMoveDown) {
                                ts.paintActiveFigure();
                                ts.removeLines();
                                setNewScore();
                                ts.addNewFigure();
                            }
                            cycles = 0;
                        }
                        dv.invalidate();
                    }
                    //  SI SE PIERDE LA PARTIDA
                    else{
                        try {
                            //  SI NO HAY UN BEST SCORE GUARDADO, GUARDAMOS EL DEL JUEGO ACTUAL
                            if(!((DataManager)getApplication()).hasBestScore()) {
                                ((DataManager) getApplication()).saveBestScore(String.valueOf(ts.getScore()));
                            }
                            //  SI HAY UN BEST SCORE GUARDADO, LO COMPARAMOS CON EL SCORE DEL JUEGO ACTUAL PARA ACTUALIZAR ESE VALOR
                            else{
                                int bestScoreSaved = Integer.valueOf(((DataManager) getApplication()).getBestScore());
                                if (bestScoreSaved < ts.getScore()) {
                                    ((DataManager) getApplication()).saveBestScore(String.valueOf(ts.getScore()));
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //  UNA VEZ SE HA PERDIDO LA PARTIDA SE ELIMINA EL ULTIMO ESTADO DE JUEGO GUARDADO
                        if(((DataManager)getApplication()).userWantsToSave()){
                            if(((DataManager)getApplication()).tetrisManagerSelected()){
                                if(((DataManager)getApplication()).hasGameState()){
                                    try {
                                        ((DataManager)getApplication()).deleteGameState();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        //  MOSTRAMOS EL BOTON DE RESTART NEW GAME
                        ((FrameLayout) findViewById(R.id.board_lay)).removeView(dv);
                        if (restartBtn.getParent() == null) {
                            ((FrameLayout) findViewById(R.id.board_lay)).removeView(restartBtn);
                            ((FrameLayout) findViewById(R.id.board_lay)).addView(restartBtn);
                        }

                    }
                }
                handler.postDelayed(runnable, delay);
            }
        };
        handler.postDelayed(runnable, delay);
        ((FrameLayout)findViewById(R.id.board_lay)).addView(dv);
    }


    int lastColmn = -1;
    int lastRow = -1;
    boolean validToMove = false;
    Point d1;
    Point d2;
    int angleRegion = -1;
    boolean firstTime2Fingers = true;
    @SuppressLint("ClickableViewAccessibility")
    private void setBoardTouchEvent(DrawView dv) {
        dv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getPointerCount() == 1){
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();

                    //  Al presionar la casilla de guardar una figura
                    boolean insideBoardStored = (x >= dv.minXStored && x <= dv.maxXStored && y >= (dv.minYStored) && y <= (dv.maxYStored));
                    if(insideBoardStored && motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                        ts.storeActiveFigure();
                        return true;
                    }

                    //  Al presionar encima del tablero de juego
                    boolean insideBoard = (x >= dv.minX && x <= dv.maxX && y >= dv.minY && y <= (dv.minY + dv.maxY));
                    if(insideBoard && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        validToMove = true;
                    }
                    else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        lastColmn = -1;
                        lastRow = -1;
                        validToMove = false;
                    }
                    if(validToMove) {
                        //  Mover hacia los lados
                        int currentColumn = Math.round(x-dv.minX)/dv.blockWidth;
                        if(lastColmn != -1) {
                            if (lastColmn < currentColumn) {
                                ts.moveActiveFigureRight();
                            } else if (lastColmn > currentColumn) {
                                ts.moveActiveFigureLeft();
                            }
                        }
                        lastColmn = currentColumn;

                        //  Mover hacia abajo
                        int currentRow = Math.round(y-dv.minY)/dv.blockWidth;
                        if(lastRow != -1) {
                            if (lastRow < currentRow) {
                                ts.moveActiveFigureDown();
                            }
                        }
                        lastRow = currentRow;

                        return true;
                    }
                }
                else if(motionEvent.getPointerCount() == 2){
                    if (firstTime2Fingers) {
                        // dos dedos han tocado la pantalla
                        d1 = new Point(motionEvent.getX(0), motionEvent.getY(0));     //  Finger 1
                        d2 = new Point(motionEvent.getX(1), motionEvent.getY(1));     //  Finger 2
                        firstTime2Fingers = false;
                    }
                    else if (motionEvent.getAction() == MotionEvent.ACTION_POINTER_UP) {
                        // dos dedos han dejado de tocar la pantalla
                        d1 = null;
                        d2 = null;
                        angleRegion = -1;
                        firstTime2Fingers = true;
                    }
                    else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                        // dos dedos están moviéndose sobre la pantalla
                        Point d1_ = new Point(motionEvent.getX(0), motionEvent.getY(0));     //  Finger 1'
                        Point d2_ = new Point(motionEvent.getX(1), motionEvent.getY(1));     //  Finger 2'

                        Point p1 = Point.sub(d2, d1);
                        Point p2 = Point.sub(d2_, d1_);

                        double angleDeg = Math.asin(Point.dotProd(Point.unitary(p2),Point.unitary(p1)));
                        angleDeg = Math.abs(Math.toDegrees(angleDeg)-90);
                        angleDeg = Math.round(angleDeg);

                        if(angleDeg > 5 && angleDeg <= 25 && angleRegion != 1){
                            if(angleRegion < 1) {
                                angleRegion = 1;
                                ts.rotateActiveFigure();
                            }
                            else{
                                --angleRegion;
                            }
                        }
                        else if(angleDeg > 25 && angleDeg <= 45 && angleRegion != 2){
                            if(angleRegion < 2) {
                                angleRegion = 2;
                                ts.rotateActiveFigure();
                            }
                            else{
                                --angleRegion;
                            }
                        }
                        else if(angleDeg > 45 && angleDeg <= 65 && angleRegion != 3){
                            if(angleRegion < 3) {
                                angleRegion = 3;
                                ts.rotateActiveFigure();
                            }
                            else{
                                --angleRegion;
                            }
                        }
                        else if(angleDeg > 65 && angleDeg <= 85 && angleRegion != 4){
                            if(angleRegion < 4) {
                                angleRegion = 4;
                                ts.rotateActiveFigure();
                            }
                            else{
                                --angleRegion;
                            }
                        }
                        System.out.println("Angle: " + angleDeg + " Region: " + angleRegion);

                    }

                    // startorientation in [0,1,2,3]
                    // p1=Point.sub(d2,d1);
                    // p2=Point.sub(d2',d1')
                    // a=Math.asin(Point.prodVec(Point.unitary(p1),Point.unitary(p2)))
                    // if (Math.PI/4<=a && a<=Math.PI*3/4) orientation=(startorientation+1)%4
                    // else if (Math.PI*3/4<a) orientation=(startorientation+2)%4
                    // else if (Math.PI/4<=-a && -a<=Math.PI*3/4) orientation=(startorientation+3)%4
                    // else if (Math.PI*3/4<-a) orientation=(startorientation+2)%4
                }
                return false;
            }
        });
    }

    private void setNewScore() {
        scoreText.setText("Score: " + ts.getScore());
    }

    private void setGameButtons(){
        int btnTextColor = Color.WHITE;

        pauseBtn = ((Button)findViewById(R.id._pause));
        if(!savedGame) {
            pauseBtn.setText("Pause");
        }
        else{
            pauseBtn.setText("Resume");
        }
        pauseBtn.setTextColor(btnTextColor);

        leftBtn = ((Button)findViewById(R.id._left));
        leftBtn.setText("L");
        leftBtn.setTextColor(btnTextColor);

        rightBtn = ((Button)findViewById(R.id._right));
        rightBtn.setText("R");
        rightBtn.setTextColor(btnTextColor);

        rotateBtn = ((Button)findViewById(R.id._rotate));
        rotateBtn.setText("⟳");
        rotateBtn.setTextColor(btnTextColor);

        downBtn = ((Button)findViewById(R.id._down));
        downBtn.setText("↓");
        downBtn.setTextColor(btnTextColor);

        dropBtn = ((Button)findViewById(R.id._drop));
        dropBtn.setText("DROP");
        dropBtn.setTextColor(btnTextColor);

        restartBtn = new Button(this);
        restartBtn.setText("Restart game");
        restartBtn.setTextColor(Color.BLACK);

        scoreText = ((TextView)findViewById(R.id._score));
        scoreText.setTextColor(Color.BLACK);
        setNewScore();

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ts.pauseGame(!ts.gamePaused());
                if(ts.gamePaused()){
                    pauseBtn.setText("Resume");
                }
                else{
                    pauseBtn.setText("Pause");
                }
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ts.moveActiveFigureLeft();
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ts.moveActiveFigureRight();
            }
        });

        rotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ts.rotateActiveFigure();
            }
        });

        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ts.moveActiveFigureDown();
            }
        });

        dropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ts.dropDownActiveFigure();
            }
        });

        Context context = this;
        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FrameLayout)findViewById(R.id.board_lay)).removeView(restartBtn);
                ts = new TetrisState(rows, columns);
                dv = new DrawView(context, ts);
                dv.setBackgroundColor(gameBgColor);
                setBoardTouchEvent(dv);
                ((FrameLayout)findViewById(R.id.board_lay)).addView(dv);
                setNewScore();
            }
        });
    }
}