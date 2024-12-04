package com.example.tetris;

public class Point {
    int i, j;
    double x, y;

    Point(int I, int J){
        this.i = I;
        this.j = J;
    }

    Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    static Point add(Point p1, Point p2) {
        return new Point(p1.i + p2.i, p1.j + p2.j);
    }

    static Point sub(Point p1, Point p2){return new Point(p1.x-p2.x, p1.y-p2.y);}
    static Point div(Point p1, double d){
        return new Point(p1.x/d, p1.y/d);
    }

    static double norm(Point p){return p.x*p.x+p.y*p.y;}
    static double modulo(Point p){return Math.sqrt(norm(p));}
    static Point unitary(Point p){return Point.div(p, Point.modulo(p));}
    static double dotProd(Point p1, Point p2){return p1.x*p2.x+p1.y*p2.y;}

}
