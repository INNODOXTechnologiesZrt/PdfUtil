package com.innodox.model.folding;

public class Point {
    private float xCoordinate;
    private float yCoordinate;

    public Point(float xCoordinate, float yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public static PointBuilder builder() {
        return new PointBuilder();
    }

    public float getXCoordinate() {
        return this.xCoordinate;
    }

    public float getYCoordinate() {
        return this.yCoordinate;
    }

    public static class PointBuilder {
        private float xCoordinate;
        private float yCoordinate;

        PointBuilder() {
        }

        public PointBuilder xCoordinate(float xCoordinate) {
            this.xCoordinate = xCoordinate;
            return this;
        }

        public PointBuilder yCoordinate(float yCoordinate) {
            this.yCoordinate = yCoordinate;
            return this;
        }

        public Point build() {
            return new Point(this.xCoordinate, this.yCoordinate);
        }

        public String toString() {
            return "Point.PointBuilder(xCoordinate=" + this.xCoordinate + ", yCoordinate=" + this.yCoordinate + ")";
        }
    }
}
