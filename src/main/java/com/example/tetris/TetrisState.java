package com.example.tetris;

import android.util.Log;

import java.util.ArrayList;

public class TetrisState {
    BoardBlock[][] board;
    BoardBlock[][] boardStored;
    int rows;
    int columns;
    ArrayList<TetrisFigure> figuresList;
    TetrisFigure activeFig;
    TetrisFigure storedFig;
    boolean alreadyStored;
    boolean paused;
    boolean gameOver;
    int score;

    TetrisState(int r, int c){
        rows = r;
        columns = c;
        board = new BoardBlock[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                board[i][j] = new BoardBlock(i, j);
            }
        }

        int storedBoardRows = 4;
        int storedBoardColumns = 3;
        boardStored = new BoardBlock[storedBoardRows][storedBoardColumns];
        for (int i = 0; i < storedBoardRows; i++) {
            for (int j = 0; j < storedBoardColumns; j++) {
                boardStored[i][j] = new BoardBlock(i, j);
            }
        }

        figuresList = new ArrayList<>();
        activeFig = new TetrisFigure(TetrisFigure.FigureType.getRandomFigure(), this, false);
        figuresList.add(activeFig);
        storedFig = null;
        alreadyStored = false;
        paused = false;
        gameOver = false;
        score = 0;
    }

    public BoardBlock getBoardBlockAt(Point pos) {
        return board[pos.i][pos.j];
    }

    public boolean isConflicting(Point pos) {
        if (pos.j < 0 || pos.j >= columns || pos.i >= rows)
            return true;
        if (pos.j >= 0 && pos.j < columns && pos.i >= 0 && pos.i < rows)
            return getBoardBlockAt(pos).state == BoardBlock.BlockState.FILLED;
        return false;
    }

    public boolean rotatingBlockIsConflicting(Point pos) {
        if(pos.i < 0 || pos.i >= rows)
            return true;
        if (pos.j >= 0 && pos.j < columns && pos.i >= 0 && pos.i < rows)
            return getBoardBlockAt(pos).state == BoardBlock.BlockState.FILLED;
        return false;
    }

    public boolean gamePaused(){
        return paused;
    }

    public void pauseGame(boolean pauseGame){
        paused = pauseGame;
    }

    private boolean canAddNewFigure(TetrisFigure tf){
        if(!paused) {
            for (BoardBlock block : tf.figBlocks) {
                if(block.position.i < 0){
                    continue;
                }
                boolean alreadyFilled = getBoardBlockAt(block.position).state == BoardBlock.BlockState.FILLED;
                if (block.state == BoardBlock.BlockState.FILLED && alreadyFilled) {
                    Log.i("---->", "GAME OVER");
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean validFigureMove(TetrisFigure tf, Point displacement) {
        if(!paused) {
            for (BoardBlock block : tf.figBlocks) {
                if (block.state == BoardBlock.BlockState.FILLED) {
                    Point shifted = Point.add(block.position, displacement);
                    if (isConflicting(shifted)) {
                        return false;
                    }
                    if (displacement.j != 0 && displacement.i == 0) {
                        for (int i = block.position.j + 1; i <= block.position.j + displacement.j; ++i) {
                            if(block.position.i >= 0) {
                                if (getBoardBlockAt(new Point(block.position.i, i)).state == BoardBlock.BlockState.FILLED) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private int calculateShift(TetrisFigure tf) {
        int shift = 0;
        for (BoardBlock block : tf.figBlocks) {
            int auxShift = 0;
            if(block.state == BoardBlock.BlockState.FILLED && (block.position.j < 0 || block.position.j >= columns)) {
                if(block.position.j < 0)
                    auxShift = Math.abs(block.position.j);
                else if(block.position.j >= columns)
                    auxShift = (columns-1) - block.position.j;

                if(shift < Math.abs(auxShift)) {
                    shift = auxShift;
                }
            }
        }
        return shift;
    }

    boolean moveActiveFigureDown() {
        if (validFigureMove(activeFig, new Point(1, 0))) {
            activeFig.moveDown();
            return true;
        } else {
            alreadyStored = false;
            return false;
        }

    }

    boolean moveActiveFigureLeft() {
        if (validFigureMove(activeFig, new Point(0, -1))) {
            activeFig.moveLeft();
            return true;
        } else {
            return false;
        }
    }

    boolean moveActiveFigureRight() {
        if (validFigureMove(activeFig, new Point(0, 1))) {
            activeFig.moveRight();
            return true;
        } else {
            return false;
        }
    }

    boolean rotateActiveFigure() {
        if (paused || activeFig.figType == TetrisFigure.FigureType.SQUARE_SHAPED) {
            return false;
        }
        for(BoardBlock b : activeFig.figBlocks){
            if(b.position.i < 0){
                return false;
            }
        }
        if(activeFig.rotate()) {
            int shift = calculateShift(activeFig);
            activeFig.setShift(shift);
        }
        return true;
    }

    boolean dropDownActiveFigure() {
        boolean canGoDown = moveActiveFigureDown();
        boolean ret = canGoDown;
        while (canGoDown){
            canGoDown = moveActiveFigureDown();
        }
        return ret;
    }

    void paintActiveFigure() {
        for (BoardBlock block : activeFig.figBlocks) {
            if (block.state == BoardBlock.BlockState.EMPTY)
                continue;
            if(block.position.i >= 0) {
                getBoardBlockAt(block.position).set(block);
            }
        }
    }

    public TetrisFigure getActiveFigure(){
        return activeFig;
    }

    public void storeActiveFigure(){
        if(!alreadyStored && !paused) {
            if (storedFig == null) {
                storedFig = new TetrisFigure(activeFig.figType, activeFig.ts, true);
                addNewFigure();
            } else {
                TetrisFigure auxFigure = new TetrisFigure(activeFig.figType, activeFig.ts, true);
                activeFig = new TetrisFigure(storedFig.figType, storedFig.ts, false);
                storedFig = auxFigure;
            }
            alreadyStored = true;
        }
    }

    public TetrisFigure getStoredFigure(){
        return storedFig;
    }


    public void addNewFigure() {
        TetrisFigure auxFigure = new TetrisFigure(TetrisFigure.FigureType.getRandomFigure(), this, false);
        if(canAddNewFigure(auxFigure)) {
            activeFig = auxFigure;
            figuresList.add(activeFig);
        }
        else{
            gameOver = true;
        }
    }

    public void removeLines(){
        //  Iteramos todas las lineas del tablero
        for(int i = rows-1; i >= 0; --i){
            if(uniformRow(i, BoardBlock.BlockState.FILLED)){    //  Linea completa
                //  Eliminar linea completada
                for(int j = 0; j < columns; ++j) {
                    board[i][j].removeBlock(new Point(i, j));
                }

                //  Bajar bloques
                for(int i2 = i; i2 > 0; --i2){
                    for(int j = 0; j < columns; ++j) {
                        board[i2][j].set(board[i2-1][j]);
                    }
                }

                //  SIEMPRE que haya bloques en la fila más alta, la limpiamos
                if(!uniformRow(0, BoardBlock.BlockState.EMPTY)){
                    for(int j = 0; j < columns; ++j) {
                        board[0][j].removeBlock(new Point(0, j));
                    }
                }
                ++i;
                ++score;
            }
        }
    }

    /**
     * Comprueba si todos los bloques de una fila "row" se encuentran en el mismo estado "state"
     * @param row
     * @param state
     * @return
     */
    boolean uniformRow(int row, BoardBlock.BlockState state){
        for(int j = 0; j < columns; ++j){
            if(getBoardBlockAt(new Point(row, j)).state == BoardBlock.BlockState.EMPTY) {
                return false;
            }
        }
        return true;
    }

    public boolean gameOver() {
        return gameOver;
    }

    public int getScore(){
        return score;
    }
}
