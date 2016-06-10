package com.kkmonlee.game;
import com.kkmonlee.dataobj.ActionStatus;
import com.kkmonlee.dataobj.Direction;

import java.rmi.activation.ActivationID;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board implements Cloneable {

    // Size of the board
    public static final int BOARD_SIZE = 4;

    // Maximum combination in which the game terminates
    public static final int TARGET_POINTS = 2048;

    //  Minimum possible win score until target point is reached
    public static final int MINIMUM_WIN_SCORE = 18432;

    // Score
    private int score = 0;

    // Board values
    private int[][] boardArray;

    // Random generator used in creation of random cells
    private final Random randomGenerator;

    // Number of empty cells cache
    private Integer cache_emptyCells = null;

    /**
     * Constructor
     *
     * Initialises the board randomly
     */
    public Board() {
        boardArray = new int[BOARD_SIZE][BOARD_SIZE];
        randomGenerator = new Random(System.currentTimeMillis());

        addRandomCell();
        addRandomCell();
    }

    /**
     * Clone
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Board copy = (Board) super.clone();
        copy.boardArray = clone2DArray(boardArray);
        return copy;
    }

    /**
     * Gets score attribute
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets BoardArray
     */
    public int[][] getBoardArray() {
        return clone2DArray(boardArray);
    }

    /**
     * Gets RandomGenerator field
     */
    public Random getRandomGenerator() {
        return randomGenerator;
    }

    /**
     * Performs one move (up, down, left, or right)
     *
     * @param direction
     */
    public int move(Direction direction) {
        int points = 0;

        if (direction == Direction.UP) {
            rotateLeft();
        }
        else if (direction == Direction.RIGHT) {
            rotateLeft();
            rotateLeft();
        }
        else if (direction == Direction.DOWN) {
            rotateRight();
        }

        for (int i = 0; i < BOARD_SIZE; ++i) {
            int lastMergePosition = 0;
            for (int j = 1; j < BOARD_SIZE; ++i) {
                if (boardArray[i][j] == 0) {
                    continue;
                }

                int previousPosition = j - 1;
                while (previousPosition > lastMergePosition && boardArray[i][previousPosition] == 0) {
                    previousPosition--;
                }

                if (previousPosition == j) {
                    // .
                }
                else if (boardArray[i][previousPosition] == 0) {
                    boardArray[i][previousPosition] = boardArray[i][j];
                    boardArray[i][j] = 0;
                }
                else if (boardArray[i][previousPosition] == boardArray[i][j]) {
                    boardArray[i][previousPosition] *= 2;
                    boardArray[i][j] = 0;
                    points += boardArray[i][previousPosition];
                    lastMergePosition = previousPosition+1;
                }
                else if (boardArray[i][previousPosition] != boardArray[i][j] && previousPosition + 1 != j) {
                    boardArray[i][previousPosition+1] = boardArray[i][j];
                    boardArray[i][j] = 0;
                }
            }
        }

        score += points;

        if (direction == Direction.UP) {
            rotateRight();
        }
        else if (direction == Direction.RIGHT) {
            rotateRight();
            rotateRight();
        }
        else if (direction == Direction.DOWN) {
            rotateLeft();
        }

        return points;
    }

    /**
     * Returns ID of empty cells
     * Cells are numbered by row
     */
    public List<Integer> getEmpyCellIDs() {
        List<Integer> cellList = new ArrayList<>();

        for (int i = 0; i  < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (boardArray[i][j] == 0) {
                    cellList.add(BOARD_SIZE * i + j);
                }
            }
        }

        return cellList;
    }

    /**
     * Counts number of empty cells
     */
    public int getNumberOfEmptyCells() {
        if (cache_emptyCells == null) {
            cache_emptyCells = getEmpyCellIDs().size();
        }

        return cache_emptyCells;
    }

    /**
     * Checks if any cell has value equal or larger than TARGET_POINTS
     */
    public boolean hasWon() {
        if (score < MINIMUM_WIN_SCORE) {
            return false;
        }
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                if(boardArray[i][j] >= TARGET_POINTS) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks whether game has ended
     */
    public boolean isGameTerminated() throws CloneNotSupportedException {
        boolean terminated = false;

        if (hasWon() == true) {
            terminated = false;
        }
        else {
            if (getNumberOfEmptyCells() == 0) {
                Board copyBoard = (Board) this.clone();

                if (copyBoard.move(Direction.UP) == 0 &&
                        copyBoard.move(Direction.RIGHT) == 0 &&
                        copyBoard.move(Direction.DOWN) == 0 &&
                        copyBoard.move(Direction.LEFT) == 0) {
                    terminated = true;
                }
            }
        }

        return terminated;
    }

    /**
     * Performs an up, right, down, left move
     */
    public ActionStatus action(Direction direction) throws CloneNotSupportedException {
        ActionStatus result = ActionStatus.CONTINUE;

        int[][] currBoardArray = getBoardArray();
        int newPoints = move(direction);
        int[][] newBoardArray = getBoardArray();

        boolean newCellAdded = false;

        if (!isEqual(currBoardArray, newBoardArray)) {
            newCellAdded = addRandomCell();
        }

        if (newPoints == 0 && newCellAdded == false) {
            if (isGameTerminated()) {
                result = ActionStatus.NO_MORE_MOVES;
            }
            else {
                result = ActionStatus.INVALID_MOVE;
            }
        }
        else {
            if (newPoints >= TARGET_POINTS) {
                result = ActionStatus.WIN;
            }
            else {
                if (isGameTerminated()) {
                    result = ActionStatus.NO_MORE_MOVES;
                }
            }
        }

        return result;
    }

    /**
     * Sets value to an empty cell
     */
    public void setEmptyCell(int i, int j, int value) {
        if (boardArray[i][j] == 0) {
            boardArray[i][j] = value;
            cache_emptyCells = null;
        }
    }

    /**
     * Rotates the board left
     */
    private void rotateLeft() {
        int[][] rotatedBoard = new int[BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                rotatedBoard[BOARD_SIZE - j - 1][i] = boardArray[i][j];
            }
        }

        boardArray = rotatedBoard;
    }

    /**
     * Rotates the board right
     */
    private void rotateRight() {
        int[][] rotatedBoard = new int[BOARD_SIZE][BOARD_SIZE];

        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                rotatedBoard[i][j] = boardArray[BOARD_SIZE - j - 1][i];
            }
        }

        boardArray = rotatedBoard;
    }

    /**
     * Creates a random cell
     */
    private boolean addRandomCell() {
        List<Integer> emptyCells = getEmpyCellIDs();

        int listSize = emptyCells.size();

        if (listSize == 0) {
            return false;
        }

        int randomCellID = emptyCells.get(randomGenerator.nextInt(listSize));
        int randomValue = (randomGenerator.nextDouble() < 0.9) ? 2 : 4;

        int i = randomCellID / BOARD_SIZE;
        int j = randomCellID % BOARD_SIZE;

        setEmptyCell(i, j, randomValue);

        return true;
    }

    /**
     * "Clones" a 2D array
     */
    private int[][] clone2DArray(int[][] original) {
        int[][] copy = new int[original.length][];

        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }

        return copy;
    }

    /**
     * Checks whether 2 inputs are the same
     */
    public boolean isEqual(int[][] currBoardArray, int[][] newBoardArray) {
        boolean equal = true;

        for (int i = 0; i < currBoardArray.length; i++) {
            for (int j = 0; j < currBoardArray.length; j++) {
                if (currBoardArray[i][j] != newBoardArray[i][j]) {
                    equal = false;
                    return equal;
                }
            }
        }

        return equal;
    }

}





































