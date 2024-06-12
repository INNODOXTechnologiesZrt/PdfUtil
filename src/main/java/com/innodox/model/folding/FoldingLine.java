package com.innodox.model.folding;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FoldingLine {

    private Point start;
    private Point end;

    @Builder
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
}
