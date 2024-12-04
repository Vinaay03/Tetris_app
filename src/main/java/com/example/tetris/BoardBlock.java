package com.example.tetris;

public class BoardBlock {
    int color;
    enum BlockState {EMPTY, FILLED};
    Point position;
    BlockState state;

    BoardBlock(int row, int col){
        color = -1;
        position = new Point(row, col);
        state = BlockState.EMPTY;
    }

    BoardBlock(int Color, Point pos, BlockState s) {
        color = Color;
        position = pos;
        state = s;
    }

    void set(BoardBlock b) {
        color = b.color;
        position.i = b.position.i;
        position.j = b.position.j;
        state = b.state;
    }

    void removeBlock(Point pos){
        color = -1;
        position.j = pos.j;
        position.i = pos.i;
        state = BlockState.EMPTY;
    }
}
