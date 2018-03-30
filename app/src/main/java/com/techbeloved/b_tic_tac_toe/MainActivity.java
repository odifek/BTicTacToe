package com.techbeloved.b_tic_tac_toe;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final int THREE_SQUARE_BOARD = 3;
    private static final int FIVE_SQUARE_BOARD = 5;
    private static final int DEFAULT_BOARD_SIZE = THREE_SQUARE_BOARD;

    private static final String O_CHAR = "O";
    private static final String X_CHAR = "X";
    private static final String X_PLAYER = "X";
    private static final String O_PLAYER = "O";
    private static final String INITIAL_GAME_STATUS = "Start game or select player";
    private static final String DEFAULT_USER_CHAR = X_CHAR;

    private static final String WINNER_MSG = "WINNER!";
    private static final String DRAW_MSG = "DRAW!";
    // Define variables and constants
    private int oScore = 0;
    private int xScore = 0;
    private String[][] gameBoard;
    private int boardSize;
    private boolean userPlaysFirst = true;
    private String userChar, machineChar;
    private String currentPlayer;

    // View flipper
    private ViewFlipper layoutFlipper;
    // Views
    private TextView gameStatusTextView;
    // Set user player as O when clicked
    View.OnClickListener oPlayerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setClickable(false);
            userPlaysFirst = false;
            // TODO: move this to the right place
            currentPlayer = O_PLAYER; // This is temporary

            // Change user play character to "O"
            userChar = O_CHAR;

            setGameStatus("'x' turn");
        }
    };
    // Set user player as X when clicked
    View.OnClickListener xPlayerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setClickable(false);
            userPlaysFirst = true;
            // TODO: move this to the right place
            currentPlayer = X_PLAYER;

            // Change user play character to "O"
            userChar = X_CHAR;

            setGameStatus("'x' turn");
        }
    };
    private Button oPlayerButton, xPlayerButton;
    /**
     * Whenever a cell is clicked, record the cell and do other things like update the cell
     * Check for win or game over
     */
    View.OnClickListener cellOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Do not accept anymore clicks
            v.setClickable(false);

            // Disable ability to select player once game's started
            if (oPlayerButton.isClickable() || xPlayerButton.isClickable()) {
                disableGameButtons();
            }

            // Retrieve view as TextView
            TextView cellView = (TextView) v;

            // Update the cell to show the user character {either X or O}
            cellView.setText(userChar);

            // Get cell information
            int[] cellPlayed = (int[]) cellView.getTag();

            // First update game board
            gameBoard[cellPlayed[0]][cellPlayed[1]] = userChar;

            // Analyse the board: checking for win, end of game, etc
            analyseBoard(cellPlayed);
        }
    };

    /**
     * onClickListener for the reset button
     */
    View.OnClickListener resetButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            resetGameBoard();
        }
    };
    // OnClickListener for the game restart button
    View.OnClickListener restartButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            restartGame();
        }
    };

    private Button threeSquareButton, fiveSquareButton;
    // Sets the board size to five square. This is only active initially
    View.OnClickListener fiveSquareOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setBoardSize(FIVE_SQUARE_BOARD);

            // Change the view
//            layoutFlipper.showNext();
            layoutFlipper.setDisplayedChild(layoutFlipper
                    .indexOfChild(findViewById(R.id.five_square_layout)));

            // Enable the other button and disable this one
            threeSquareButton.setClickable(true);
            v.setClickable(false);
            // Re - initialize the game board
            initializeGameBoard();

        }
    };

    // Sets the board size to three square. This is only active initially
    View.OnClickListener threeSquareOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setBoardSize(THREE_SQUARE_BOARD);

            // Change the view
            layoutFlipper.setDisplayedChild(layoutFlipper
                    .indexOfChild(findViewById(R.id.three_square_layout)));

            // Enable the other button and disable this one
            fiveSquareButton.setClickable(true);
            v.setClickable(false);
            // Re - initialize the game board
            initializeGameBoard();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize board
        initializeGame();
        initializeGameBoard();
    }

    /**
     * Loops through all the cells of the board, setting click listeners to all of them
     */
    private void initializeGameBoard() {
        // Initialize the gameBoard with the boardSize
        gameBoard = new String[boardSize][boardSize];

        // Set the game status
        gameStatusTextView = findViewById(R.id.game_status_textview);
        gameStatusTextView.setText(INITIAL_GAME_STATUS);

        // Set the default user character to "X"
        userChar = DEFAULT_USER_CHAR;
        currentPlayer = X_PLAYER;

        // Get layout resources
        Resources resources = getResources();
        String packageName = getPackageName();

        //Prefix for cell IDs. Default is three square
        String cellPrefix = "three_cell_";
        if (boardSize == 5)
            cellPrefix = "five_cell_";

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {

                // Retrieve the TextView ID using the name. Cells have ids cell_00, cell_01, and so on
                int resId = resources.getIdentifier(cellPrefix + i + j, "id", packageName);
                TextView gameCell = findViewById(resId);

                // Set all cells to blank
                gameBoard[i][j] = null;
                gameCell.setText(null);

                // Store the cell location in the view's tag for easy retrieval of the
                // cell information next time
                int[] cell = {i, j};
                gameCell.setTag(cell);

                // Set the click listener
                gameCell.setOnClickListener(cellOnClickListener);
            }
        }

        // Use the layout flipper to set the correct layout
        // This is useful when coming out from end of game. That is starting a new game by
        // clicking on the result display text view
        if (boardSize == 3)
            layoutFlipper.setDisplayedChild(layoutFlipper
                    .indexOfChild(findViewById(R.id.three_square_layout)));
        else
            layoutFlipper.setDisplayedChild(layoutFlipper
                    .indexOfChild(findViewById(R.id.five_square_layout)));

    }

    /**
     * This is the beginning pf game activity
     * Setup the board and some of the click listeners
     */
    private void initializeGame() {

        // Set the board size to default size
        setBoardSize(DEFAULT_BOARD_SIZE);

        xPlayerButton = findViewById(R.id.xPlayerBtn);
        xPlayerButton.setOnClickListener(xPlayerOnClickListener);

        oPlayerButton = findViewById(R.id.oPlayerBtn);
        oPlayerButton.setOnClickListener(oPlayerOnClickListener);

        Button resetGameButton = findViewById(R.id.resetBtn);
        resetGameButton.setOnClickListener(resetButtonOnClickListener);

        Button restartGameButton = findViewById(R.id.restartBtn);
        restartGameButton.setOnClickListener(restartButtonOnClickListener);

        // Configure buttons to change layout from 3 square to 5 square and vice versa
        // By default board is 3 x 3 so disable the button
        threeSquareButton = findViewById(R.id.threeSquareBtn);
        threeSquareButton.setOnClickListener(threeSquareOnClickListener);
        threeSquareButton.setClickable(false);

        fiveSquareButton = findViewById(R.id.fiveSquareBtn);
        fiveSquareButton.setOnClickListener(fiveSquareOnClickListener);

        // Configure layout flipper
        layoutFlipper = findViewById(R.id.layout_flipper);
//        layoutFlipper.showNext();
    }

    /**
     * Analyses the game board checking for winning row, column or diagonal
     *
     * @param cellPlayed: specifies the cell played in form of {rowId, colId}
     */
    private void analyseBoard(int[] cellPlayed) {
        // Checks for a win
        if (isRowWin(cellPlayed) || isColumnWin(cellPlayed) || isDiagonalWin(cellPlayed)) {
            updateScoreBoard();

            // TODO: display winner message here
            displayResult(WINNER_MSG, currentPlayer);

        } else if (boardIsFilled()) {
            displayResult(DRAW_MSG, X_PLAYER + O_PLAYER);
        }


        // TODO: check for column win, diagonal win, end of game, draw
    }

    /**
     * Checks whether all cell has been played
     *
     * @return true if all cell is played or false otherwise
     */
    private boolean boardIsFilled() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; i < boardSize; j++) {
                if (gameBoard[i][j] == null)
                    return false;
            }
        }
        return true;
    }

    private void displayResult(String status, String player) {
        // Get the textviews for the congratulatory winner message
        TextView winnerCharTextView = findViewById(R.id.winner_char_text_view);
        TextView winnerMsgTextView = findViewById(R.id.winner_msg_text_view);

        winnerCharTextView.setText(currentPlayer);
        winnerMsgTextView.setText(R.string.winner_msg);

        // set up click listeners
        winnerCharTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame();
            }
        });
        winnerMsgTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame();
            }
        });

        // Display the result layout
        layoutFlipper.setDisplayedChild(layoutFlipper
                .indexOfChild(findViewById(R.id.result_layout)));
    }


    /**
     * Used to update the scoreBoard
     */
    private void updateScoreBoard() {
        // TODO: need to fix this to update score according to the current player
        if (currentPlayer.equals(X_PLAYER)) {
            xScore++;
            xPlayerButton.setText(getScoreAsString(xScore));
        } else if (currentPlayer.equals(O_PLAYER)) {
            oScore++;
            oPlayerButton.setText(getScoreAsString(oScore));
        }
    }

    /**
     * Utility function that converts the score to String so that it can be displayed on textView
     *
     * @param score is an integer value
     * @return A String
     */
    private String getScoreAsString(int score) {
        return "" + score;
    }

    /**
     * Checks whether the row specified by the cell is a win
     *
     * @param cellPlayed: specifies the cell played as an array of two digits representing the row and column
     * @return true or false, win or not
     */
    private boolean isRowWin(int[] cellPlayed) {
        // Get the entire row specified by the cell
        // Example, if the cell played is {1, 1}, the entire row is the second array in the gameBoard
        String[] row = gameBoard[cellPlayed[0]];
        return itemsAreIdentical(row);
    }

    private boolean isColumnWin(int[] cellPlayed) {
        // Get the entire column specified by the cell
        int colId = cellPlayed[1];

        // Initialize the column according to boardSize
        String[] column = new String[boardSize];

        // Get all the items in the specified column
        for (int i = 0; i < boardSize; i++) {
            column[i] = gameBoard[i][colId];
        }

        return itemsAreIdentical(column);
    }

    /**
     * Checks whether the diagonal the cell belongs to is a win
     *
     * @param cellPlayed: specifies the cell played, in the form of array {rowID, colID}
     * @return true or false, is diagonal win or not
     */
    private boolean isDiagonalWin(int[] cellPlayed) {
        final int MIDDLE_CELL = 0, FIRST_DIAGONAL = 1, SECOND_DIAGONAL = 2;
        String[] firstDiagonal = getFirstDiagonal();
        String[] secondDiagonal = getSecondDiagonal();

        switch (whichDiagonal(cellPlayed)) {

            // On middle cell, check both diagonals
            case MIDDLE_CELL:
                if (itemsAreIdentical(firstDiagonal))
                    return true;
                else if (itemsAreIdentical(secondDiagonal))
                    return true;
                break;

            // on the first diagonal, check
            case FIRST_DIAGONAL:
                if (itemsAreIdentical(firstDiagonal))
                    return true;
                break;

            // on the second diagonal, check
            case SECOND_DIAGONAL:
                if (itemsAreIdentical(secondDiagonal))
                    return true;
                break;
        }
        return false;
    }

    /**
     * Utility function that checks which diagonal a given cell belongs to.
     * There are two diagonals
     *
     * @param cellPlayed represents the cell played in the form of {rowID, colID}
     * @return 0 if cell is the middle cell, 1 if cell is in the first diagonal or 2 if cell is in
     * the second diagonal
     */
    private int whichDiagonal(int[] cellPlayed) {
        int rowId = cellPlayed[0], colId = cellPlayed[1];
        // Check for middle cell
        if (rowId == colId && rowId + colId == boardSize - 1) {
            return 0;
        }
        // First diagonal
        else if (rowId == colId) {
            return 1;
        }
        // Second diagonal
        else if (rowId + colId == boardSize - 1) {
            return 2;
        } else return 3;
    }


    /**
     * Retrieves the first diagonal and
     *
     * @return an array of all the cells in the diagonal
     */
    private String[] getFirstDiagonal() {
        String[] diagonal = new String[boardSize];
        for (int i = 0; i < boardSize; i++) {
            diagonal[i] = gameBoard[i][i];
        }
        return diagonal;
    }

    /**
     * Retrieves the second diagonal and
     *
     * @return an array of all the cells in the diagonal
     */
    private String[] getSecondDiagonal() {
        String[] diagonal = new String[boardSize];
        for (int j = 0, k = boardSize - 1; j < boardSize; j++, k--) {
            diagonal[j] = gameBoard[j][k];
        }
        return diagonal;
    }


    /**
     * Utility function to check that all items in an array are equal or identical
     *
     * @param items: the array of strings to be checked
     * @return true or false, items are identical or they are not
     */
    private boolean itemsAreIdentical(String[] items) {
        String firstItem = items[0];
        if (firstItem == null)
            return false;
        for (int i = 1; i < items.length; i++) {
            if (items[i] == null || !items[i].equals(firstItem))
                return false;
        }
        return true;
    }


    // Setting of the game status, such who's turn to play and other information
    private void setGameStatus(String msg) {
        gameStatusTextView.setText(msg);
    }

    /**
     * Setting of the board size is handled publicly here
     *
     * @param boardSize: either THREE_SQUARE_BOARD (3) or FIVE_SQUARE_BOARD (5)
     */
    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    /**
     * Resets the game including the scoreboard and every other thing
     */
    private void resetGameBoard() {
        // TODO: should reset the game whenever the button is pressed
        // Rest scoreboard
        xScore = 0;
        oScore = 0;
        xPlayerButton.setText(R.string.score_placeholder);
        oPlayerButton.setText(R.string.score_placeholder);
        initializeGameBoard();
        enableGameButtons();

    }

    /**
     * Restarts the current game, only resetting the board and other variables and not the score board
     */
    private void restartGame() {
        // TODO: Should restart the current game
        initializeGameBoard();
        enableGameButtons();
    }

    /**
     * Enable buttons. This is called when restarting or resetting a game
     */
    private void enableGameButtons() {
        xPlayerButton.setClickable(true);
        oPlayerButton.setClickable(true);

        // Enable the right button in case the player wants to change the layout
        if (boardSize == THREE_SQUARE_BOARD)
            fiveSquareButton.setClickable(true);
        else
            threeSquareButton.setClickable(true);
    }

    /**
     * Disable all game buttons once game play's started
     */
    private void disableGameButtons() {
        xPlayerButton.setClickable(false);
        oPlayerButton.setClickable(false);
        threeSquareButton.setClickable(false);
        fiveSquareButton.setClickable(false);
    }

    // TODO: Implement a machine play algorithm
    private ArrayList<Integer[]> getEmptyCells() {
        ArrayList<Integer[]> emptyCells = new ArrayList<>();
        Integer[] emptyCell = new Integer[2];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (gameBoard[i][j] == null) {
                    emptyCell[0] = i;
                    emptyCell[1] = j;
                    emptyCells.add(emptyCell);
                }
            }
        }
        return emptyCells;
    }
}



