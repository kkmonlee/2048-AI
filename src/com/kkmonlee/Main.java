package com.kkmonlee;

import com.kkmonlee.ai.AISolver;
import com.kkmonlee.dataobj.ActionStatus;
import com.kkmonlee.dataobj.Direction;
import com.kkmonlee.game.Board;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("Welcome.");
        System.out.println("======================");
        System.out.println();
        while(true) {
            printMenu();
            int choice;
            try {
                Scanner sc = new Scanner (System.in);
                choice = sc.nextInt();
                switch (choice) {
                    case 1:  playGame();
                        break;
                    case 2:  calculateAccuracy();
                        break;
                    case 3:  help();
                        break;
                    case 4:  return;
                    default: throw new Exception();
                }
            }
            catch(Exception e) {
                System.out.println("Wrong choice");
            }
        }

    }

    public static void help() {

    }
    /**
     * Prints main menu
     */
    public static void printMenu() {
        System.out.println();
        System.out.println("Choices:");
        System.out.println("1. Play the 2048 Game");
        System.out.println("2. Estimate the Accuracy of AI Solver");
        System.out.println("3. Help");
        System.out.println("4. Quit");
        System.out.println();
        System.out.println("Enter a number from 1-4:");
    }

    /**
     * Estimates the accuracy of the AI solver by running multiple games
     *
     * @throws CloneNotSupportedException
     */
    public static void calculateAccuracy() throws CloneNotSupportedException {
        int wins = 0;
        int total = 10;

        System.out.println("Running " + total + " games to estimate the accuracy: ");

        for (int i = 0; i < total; i++) {
            int hintDepth = 7;
            Board theGame = new Board();

            Direction hint = AISolver.findBestMove(theGame, hintDepth);
            ActionStatus result = ActionStatus.CONTINUE;

            while (result == ActionStatus.CONTINUE || result == ActionStatus.INVALID_MOVE) {
                result = theGame.action(hint);

                if (result == ActionStatus.CONTINUE || result == ActionStatus.INVALID_MOVE) {
                    hint = AISolver.findBestMove(theGame, hintDepth);
                }
            }

            if (result == ActionStatus.WIN) {
                wins++;
                System.out.println("Game " + (i + 1) + " - won.");
            }
            else {
                System.out.println("Game " + (i + 1) + " - lost.");
            }
        }

        System.out.println(wins + " wins out of " + total + " games.");
    }

    /**
     * Method which allows playing the game
     *
     * @throws CloneNotSupportedException
     */
    public static void playGame() throws CloneNotSupportedException {
        System.out.println("Play the 2048 Game!");
        System.out.println("Use 8 for UP, 6 for RIGHT, 2 for DOWN and 4 for LEFT. Type a to play automatically and q to exit. Press enter to submit your choice.");

        int hintDepth =  7;
        Board theGame = new Board();
        Direction hint = AISolver.findBestMove(theGame, hintDepth);
        printBoard(theGame.getBoardArray(), theGame.getScore(), hint);

        try {
            InputStreamReader unbuffered = new InputStreamReader(System.in, "UTF-8");
            char inputChar;

            ActionStatus result = ActionStatus.CONTINUE;

            while (result == ActionStatus.CONTINUE || result == ActionStatus.INVALID_MOVE) {
                inputChar = (char) unbuffered.read();

                if (inputChar == '\n' || inputChar == '\r') {
                    continue;
                }
                else if(inputChar=='8') {
                    result=theGame.action(Direction.UP);
                }
                else if(inputChar=='6') {
                    result=theGame.action(Direction.RIGHT);
                }
                else if(inputChar=='2') {
                    result=theGame.action(Direction.DOWN);
                }
                else if(inputChar=='4') {
                    result=theGame.action(Direction.LEFT);
                }
                else if(inputChar=='a') {
                    result=theGame.action(hint);
                }
                else if(inputChar=='q') {
                    System.out.println("Game ended, user quit.");
                    break;
                }
                else {
                    System.out.println("Invalid key! Use 8 for UP, 6 for RIGHT, 2 for DOWN and 4 for LEFT. Type a to play automatically and q to exit. Press enter to submit your choice.");
                    continue;
                }

                if(result==ActionStatus.CONTINUE || result==ActionStatus.INVALID_MOVE ) {
                    hint = AISolver.findBestMove(theGame, hintDepth);
                }
                else {
                    hint = null;
                }
                printBoard(theGame.getBoardArray(), theGame.getScore(), hint);

                if(result!=ActionStatus.CONTINUE) {
                    System.out.println(result.getDescription());
                }
            }
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public static void printBoard(int[][] boardArray, int score, Direction hint) {
        System.out.println("-------------------------");
        System.out.println("Score:\t" + String.valueOf(score));
        System.out.println();
        System.out.println("Hint:\t" + hint);
        System.out.println();

        for(int i = 0; i < boardArray.length; i++) {
            for(int j = 0; j < boardArray[i].length; j++) {
                System.out.print(boardArray[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println("-------------------------");
    }


}
