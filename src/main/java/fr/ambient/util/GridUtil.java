package fr.ambient.util;

import lombok.Getter;

@Getter
public class GridUtil {
    private float width;
    private float height;
    private float spacing;
    private int elements;
    private float offsetX;
    private float offsetY;
    private int index = 0;

    public GridUtil(float width, float height, float spacing, int elements) {
        if (elements <= 0) {
            throw new IllegalArgumentException("Elements per row must be greater than 0");
        }
        this.width = width;
        this.height = height;
        this.spacing = spacing;
        this.elements = elements;
        this.offsetX = 0;
        this.offsetY = 0;
    }

    /**
     * Increment the position of the next element on the grid.
     */
    public void increment() {
        offsetX += width + spacing;
        index++;
        if (index % elements == 0) {
            resetRow();
        }
    }

    /**
     * Get the current position on the grid (without incrementing).
     */
    public float[] getCurrentPosition() {
        return new float[]{offsetX, offsetY};
    }

    /**
     * Reset the grid's position to the starting point (top-left corner).
     */
    public void reset() {
        this.offsetX = 0;
        this.offsetY = 0;
        this.index = 0;
    }

    /**
     * Change the grid dimensions dynamically.
     * @param newWidth new width of elements
     * @param newHeight new height of elements
     */
    public void updateDimensions(float newWidth, float newHeight) {
        this.width = newWidth;
        this.height = newHeight;
        reset();
    }

    /**
     * Change the number of elements in each row dynamically.
     * @param newElementsInRow new number of elements per row
     */
    public void updateElementsInRow(int newElementsInRow) {
        if (newElementsInRow <= 0) {
            throw new IllegalArgumentException("Elements per row must be greater than 0");
        }
        this.elements = newElementsInRow;
        reset();
    }

    /**
     * Resets the row when a row is complete.
     */
    private void resetRow() {
        offsetX = 0;
        offsetY += height + spacing;
        index = 0;
    }

    /**
     * Add support for variable element sizes.
     * @param elementWidth the width of the next element
     * @param elementHeight the height of the next element
     */
    public void incrementWithVariableSize(float elementWidth, float elementHeight) {
        offsetX += elementWidth + spacing;
        index++;
        if (index % elements == 0) {
            offsetX = 0;
            offsetY += elementHeight + spacing;
            index = 0;
        }
    }
}
