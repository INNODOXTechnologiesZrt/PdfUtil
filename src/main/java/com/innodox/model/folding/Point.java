package com.innodox.model.folding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Point {
    private float xCoordinate;
    private float yCoordinate;
}
