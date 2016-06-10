package com.kkmonlee.ai;

import com.kkmonlee.dataobj.Direction;
import com.kkmonlee.dataobj.ActionStatus;
import com.kkmonlee.game.Board;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AISolver {

    // PvE enum class
    private enum Player {
        // Computer
        COMPUTER,

        // User

        USER
    }

    /**
     * Finds the next best move
     *
     * @param theBoard
     * @param depth
     * @return
     * @throws CloneNotSupportedException
     */
    public static Direction findBestMove(Board theBoard, int depth) throws CloneNotSupportedException {
        Map<String, Object> result = alphabeta(theBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, Player.USER);

        return (Direction) result.get("Direction");
    }

    public static Map<String, Object> minimax(Board theBoard, int depth, Player player) throws CloneNotSupportedException {
        Map<String, Object> result = new HashMap<>();

        Direction bestDirection = null;
        int bestScore;

        if (depth == 0 || theBoard.isGameTerminated()) {
            bestScore = heuristicScore(theBoard.getScore(), theBoard.getNumberOfEmptyCells(), calculateClusteringScore(theBoard.getBoardArray()));
        }
        else {
            if (player == Player.USER) {
                bestScore = Integer.MIN_VALUE;

                for (Direction direction : Direction.values()) {
                    Board newBoard = (Board) theBoard.clone();

                    int points = newBoard.move(direction);

                    if (points == 0 && newBoard.isEqual(theBoard.getBoardArray(), newBoard.getBoardArray())) {
                        continue;
                    }

                    Map<String, Object> currentResult = minimax(newBoard, depth - 1, Player.COMPUTER);
                    int currentScore = ((Number) currentResult.get("Score")).intValue();
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestDirection = direction;
                    }
                }
            }
            else {
                bestScore = Integer.MAX_VALUE;

                List<Integer> moves = theBoard.getEmpyCellIDs();
                if (moves.isEmpty()) {
                    bestScore = 0;
                }
                int[] possibleValues = {2, 4};

                int i, j;
                int[][] boardArray;
                for (Integer cellID : moves) {
                    i = cellID / Board.BOARD_SIZE;
                    j = cellID % Board.BOARD_SIZE;

                    for (int value : possibleValues) {
                        Board newBoard = (Board) theBoard.clone();
                        newBoard.setEmptyCell(i, j, value);

                        Map<String, Object> currentResult = minimax(newBoard, depth - 1, Player.USER);
                        int currentScore = ((Number) currentResult.get("Score")).intValue();
                        if (currentScore < bestScore) {
                            bestScore = currentScore;
                        }
                    }
                }
            }
        }

        result.put("Score", bestScore);
        result.put("Direction", bestDirection);

        return result;
    }

    /**
     * Finds the best move using the Alpha-Beta pruning algorithm
     *
     * @param theBoard
     * @param depth
     * @param alpha
     * @param beta
     * @param player
     * @return
     * @throws CloneNotSupportedException
     */
    private static Map<String, Object> alphabeta(Board theBoard, int depth, int alpha, int beta, Player player) throws CloneNotSupportedException {
        Map<String, Object> result = new HashMap<>();

        Direction bestDirection = null;
        int bestScore;

        if(theBoard.isGameTerminated()) {
            if(theBoard.hasWon()) {
                bestScore=Integer.MAX_VALUE; //highest possible score
            }
            else {
                bestScore=Math.min(theBoard.getScore(), 1); //lowest possible score
            }
        }
        else if(depth == 0) {
            bestScore=heuristicScore(theBoard.getScore(),theBoard.getNumberOfEmptyCells(),calculateClusteringScore(theBoard.getBoardArray()));
        }
        else {
            if(player == Player.USER) {
                for(Direction direction : Direction.values()) {
                    Board newBoard = (Board) theBoard.clone();

                    int points=newBoard.move(direction);

                    if(points==0 && newBoard.isEqual(theBoard.getBoardArray(), newBoard.getBoardArray())) {
                        continue;
                    }

                    Map<String, Object> currentResult = alphabeta(newBoard, depth-1, alpha, beta, Player.COMPUTER);
                    int currentScore=((Number)currentResult.get("Score")).intValue();

                    if(currentScore>alpha) { //maximize score
                        alpha=currentScore;
                        bestDirection=direction;
                    }

                    if(beta<=alpha) {
                        break; //beta cutoff
                    }
                }

                bestScore = alpha;
            }
            else {
                List<Integer> moves = theBoard.getEmpyCellIDs();
                int[] possibleValues = {2, 4};

                int i,j;
                abloop: for(Integer cellId : moves) {
                    i = cellId/Board.BOARD_SIZE;
                    j = cellId%Board.BOARD_SIZE;

                    for(int value : possibleValues) {
                        Board newBoard = (Board) theBoard.clone();
                        newBoard.setEmptyCell(i, j, value);

                        Map<String, Object> currentResult = alphabeta(newBoard, depth-1, alpha, beta, Player.USER);
                        int currentScore=((Number)currentResult.get("Score")).intValue();
                        if(currentScore<beta) { //minimize best score
                            beta=currentScore;
                        }

                        if(beta<=alpha) {
                            break abloop; //alpha cutoff
                        }
                    }
                }

                bestScore = beta;

                if(moves.isEmpty()) {
                    bestScore=0;
                }
            }
        }

        result.put("Score", bestScore);
        result.put("Direction", bestDirection);

        return result;
    }

    /**
     * Estimates a heuristic score by taking into account the real score,
     * number of empty cells and clustering score of the board
     *
     * @param actualScore
     * @param numberOfEmptyCells
     * @param clusteringScore
     * @return
     */
    private static int heuristicScore(int actualScore, int numberOfEmptyCells, int clusteringScore) {
        int score = (int) (actualScore + Math.log(actualScore) * numberOfEmptyCells - clusteringScore);
        return Math.max(score, Math.min(actualScore, 1));
    }

    /**
     * Calculates a heuristic variance-like score that measures how
     * clustered the board is
     *
     * @param boardArray
     * @return
     */
    private static int calculateClusteringScore(int[][] boardArray) {
        int clusteringScore = 0;

        int[] neighbours = {-1, 0, 1};

        for (int i = 0; i < boardArray.length; i++) {
            for (int j = 0; j < boardArray.length; j++) {
                if (boardArray[i][j] == 0) {
                    continue;
                }

                int numOfNeighbours = 0;
                int sum = 0;
                for (int k : neighbours) {
                    int x = i + k;
                    if (x < 0 || x >= boardArray.length) {
                        continue;
                    }

                    for (int l : neighbours) {
                        int y = j + l;
                        if (y < 0 || y >= boardArray.length) {
                            continue;
                        }

                        if (boardArray[x][y] > 0) {
                            numOfNeighbours++;
                            sum += Math.abs(boardArray[i][j] - boardArray[x][y]);
                        }
                    }
                }

                clusteringScore += sum / numOfNeighbours;
            }
        }

        return clusteringScore;
    }



}
