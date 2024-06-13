package com.innodox.model.folding;

public class FoldingLine {

    private Point start;
    private Point end;

    public FoldingLine(float startX, float startY, float endX, float endY) {
        this.start = Point.builder()
                .xCoordinate(startX)
                .yCoordinate(startY)
                .build();
        this.end = Point.builder()
                .xCoordinate(endX)
                .yCoordinate(endY)
                .build();
    }

    public static FoldingLineBuilder builder() {
        return new FoldingLineBuilder();
    }

    public Point getStart() {
        return this.start;
    }

    public Point getEnd() {
        return this.end;
    }

    public static class FoldingLineBuilder {
        private float startX;
        private float startY;
        private float endX;
        private float endY;

        FoldingLineBuilder() {
        }

        public FoldingLineBuilder startX(float startX) {
            this.startX = startX;
            return this;
        }

        public FoldingLineBuilder startY(float startY) {
            this.startY = startY;
            return this;
        }

        public FoldingLineBuilder endX(float endX) {
            this.endX = endX;
            return this;
        }

        public FoldingLineBuilder endY(float endY) {
            this.endY = endY;
            return this;
        }

        public FoldingLine build() {
            return new FoldingLine(this.startX, this.startY, this.endX, this.endY);
        }

        public String toString() {
            return "FoldingLine.FoldingLineBuilder(startX=" + this.startX + ", startY=" + this.startY + ", endX=" + this.endX + ", endY=" + this.endY + ")";
        }
    }
}
