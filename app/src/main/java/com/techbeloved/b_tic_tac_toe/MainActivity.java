package com.techbeloved.b_tic_tac_toe;

import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final int THREE_SQUARE_BOARD = 3;
    private static final int FIVE_SQUARE_BOARD = 5;
    private static final int DEFAULT_BOARD_SIZE = THREE_SQUARE_BOARD;

    private static final String X_PLAYER = "❌";
    private static final String O_PLAYER = "⭕";
    private static final String INITIAL_GAME_STATUS = "Start game or select player";

    private static final String WINNER_MSG = "WINNER!";
    private static final String DRAW_MSG = "DRAW!";
    private static final String GAME_OVER = "Game Over!";

    // Activity state tags used when resuming activity as a result of rotation or some other scenerio
    private static final String STATE_FIRST_PLAYER = "UserPlayer";
    private static final String STATE_MACHINE_PLAYER = "MachinePlayer";
    private static final String STATE_O_SCORE = "OScore";
    private static final String STATE_X_SCORE = "XScore";
    private static final String STATE_GAME_BOARD = "GameBoard";
    private static final String STATE_BOARD_SIZE = "BoardSize";
    private static final String STATE_CURRENT_PLAYER = "CurrentPlayer";
    private static final String STATE_GAME_STATUS = "GameStatus";
    private static final String STATE_GAME_STARTED = "GameStarted";
    private static final String STATE_CURRENT_LAYOUT = "CurrentLayout";
    private static final String STATE_GAME_WINNER = "GameWinner";
    private static final String STATE_GAME_OVER = "GameOver";
    private static final String STATE_SECOND_PLAYER = "SecondPlayer";
    private static final String STATE_PLAY_A_FRIEND = "PlayAFriend";

    // Declare the rand generator only once
    // SecureRandom ensures unpredictability
    private final SecureRandom random = new SecureRandom();
    // Define variables
    private String mFirstPlayer, mMachinePlayer, mSecondPlayer;
    private int mOScore = 0, mXScore = 0;
    private String[][] mGameBoard;
    private int mBoardSize;
    private String mCurrentPlayer;
    private boolean mGameStarted = false;
    private boolean mPlayAFriend = false;
    // Contains prefix of the cells either five_cell_ or three_cell_ depending on size of board
    private String mCellPrefix;
    // View flipper. Swaps board layout accordingly
    private ViewFlipper mLayoutFlipper;

    private TextView mGameStatusTextView;
    private String mGameStatus = INITIAL_GAME_STATUS;

    private Button mOPlayerButton, mXPlayerButton;
    // Set user player as X when clicked
    private final View.OnClickListener mXPlayerButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setClickable(false);
            disableGameButtons();

            // Set appropriate values for machine player and first player
            mFirstPlayer = X_PLAYER;
            mCurrentPlayer = mFirstPlayer;
            mSecondPlayer = O_PLAYER;
            if (!mPlayAFriend) {
                mMachinePlayer = mSecondPlayer;
            }

            // X always plays first
            setGameStatus("X - turn");
            mGameStarted = true;
        }
    };
    // Set user player as O when clicked
    private final View.OnClickListener mOPlayerButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setClickable(false);
            disableGameButtons();

            // Set first player to O_PLAYER
            mFirstPlayer = O_PLAYER;

            // Set Second player to X_PLAYER
            // and call on machine to play immediately
            mSecondPlayer = X_PLAYER;
            mCurrentPlayer = mSecondPlayer;
            setGameStatus("X - turn");
            mGameStarted = true;
            if (!mPlayAFriend) {
                mMachinePlayer = mSecondPlayer;
                machinePlay();
            }
        }
    };
    /**
     * onClickListener for the reset button
     */
    private final View.OnClickListener mResetButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            resetGameBoard();
        }
    };
    // OnClickListener for the game restart button
    private final View.OnClickListener mRestartButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            restartGame();
        }
    };
    private RadioGroup mGridChoiceRadioGroup, mGameModeRadioGroup;
    private int mCurrentLayout;
    private String mGameWinner, mGameOverStatus;
    /**
     * Whenever a cell is clicked, record the cell and do other things like update the cell
     * Check for win or game over
     */
    private final View.OnClickListener mCellOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Do not accept anymore clicks
            disableClickOnCells();

            // Disable ability to select player once game's started
            if (mOPlayerButton.isClickable() || mXPlayerButton.isClickable()) {
                disableGameButtons();
            }

            // Retrieve view as TextView
            TextView cellView = (TextView) v;

            // Update the cell to show the user character {either X or O}
            cellView.setText(mCurrentPlayer);

            // Get cell information
            final int[] cellPlayed = (int[]) cellView.getTag();

            // First update game board
            mGameBoard[cellPlayed[0]][cellPlayed[1]] = mCurrentPlayer;

            // Analyse the board: checking for win, end of game, etc
            final Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    analyseBoard(cellPlayed);
                }
            }, 200);
        }
    };

    // Listener to detect when grid size is changed by way of checking the radio buttons
    private final RadioGroup.OnCheckedChangeListener mGridChoiceOnChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // Five-square radio is selected
            if (checkedId == R.id.five_square_radio_button) {
                setBoardSize(FIVE_SQUARE_BOARD);

                // Change the view
                mLayoutFlipper.setDisplayedChild(mLayoutFlipper
                        .indexOfChild(findViewById(R.id.five_square_layout)));

                // Re - initialize the game board
                initializeGameBoard();
            }
            // Three-square radio button is checked
            else {
                setBoardSize(THREE_SQUARE_BOARD);

                // Change the view
                mLayoutFlipper.setDisplayedChild(mLayoutFlipper
                        .indexOfChild(findViewById(R.id.three_square_layout)));

                // Re - initialize the game board
                initializeGameBoard();
            }
        }
    };
    private final RadioGroup.OnCheckedChangeListener mGameModeChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.play_friend_radio_button) {
                mPlayAFriend = true;
                initializeGameBoard();
            } else {
                mPlayAFriend = false;
                initializeGameBoard();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize board
        if (savedInstanceState == null) {
            initializeGame();
            initializeGameBoard();
        }

    }

    /**
     * Refreshes the game board on event of Activity resuming after pause for example, when screen is rotated
     * All the state variables are used to refresh the UI accordingly
     */
    private void refreshGameBoard() {
        mXPlayerButton = findViewById(R.id.xPlayerBtn);
        mXPlayerButton.setOnClickListener(mXPlayerButtonOnClickListener);

        mOPlayerButton = findViewById(R.id.oPlayerBtn);
        mOPlayerButton.setOnClickListener(mOPlayerButtonOnClickListener);

        Button resetGameButton = findViewById(R.id.resetBtn);
        resetGameButton.setOnClickListener(mResetButtonOnClickListener);

        Button restartGameButton = findViewById(R.id.restartBtn);
        restartGameButton.setOnClickListener(mRestartButtonOnClickListener);

        // Configure Radio buttons to change layout from 3 square to 5 square and vice versa
        // By default board is 3 x 3
        mGridChoiceRadioGroup = findViewById(R.id.grid_choice_radio_group);
        mGridChoiceRadioGroup.setOnCheckedChangeListener(mGridChoiceOnChangeListener);

        // Configure game mode radio buttons. To select between single player or play a friend mode
        mGameModeRadioGroup = findViewById(R.id.game_mode_radio_group);
        mGameModeRadioGroup.setOnCheckedChangeListener(mGameModeChangeListener);

        // Configure layout flipper
        mLayoutFlipper = findViewById(R.id.layout_flipper);

        // Set the game status
        mGameStatusTextView = findViewById(R.id.game_status_textview);
        mGameStatusTextView.setText(mGameStatus);

        if (mGameStarted) {
            disableGameButtons();
        }

        // Refresh the game board enabling the empty cells for clicks
        // Get layout resources
        Resources resources = getResources();
        String packageName = getPackageName();

        //Prefix for cell IDs. Default is three square
        if (mBoardSize == 5) mCellPrefix = "five_cell_";
        else mCellPrefix = "three_cell_";

        for (int i = 0; i < mBoardSize; i++) {
            for (int j = 0; j < mBoardSize; j++) {

                // Retrieve the TextView ID using the name. Cells have ids cell_00, cell_01, and so on
                int resId = resources.getIdentifier(mCellPrefix + i + j, "id", packageName);
                TextView gameCell = findViewById(resId);

                // Update the cell
                gameCell.setText(mGameBoard[i][j]);

                // Store the cell location in the view's tag for easy retrieval of the
                // cell information next time
                int[] cell = {i, j};
                gameCell.setTag(cell);

                // Set the click listener on empty cell
                if (mGameBoard[i][j] == null) {
                    gameCell.setOnClickListener(mCellOnClickListener);
                }
            }
        }

        // Update the current layout
        mLayoutFlipper.setDisplayedChild(mCurrentLayout);

        // Update Game over message if game over
        TextView winnerCharTextView = findViewById(R.id.winner_char_text_view);
        TextView winnerMsgTextView = findViewById(R.id.winner_msg_text_view);

        winnerCharTextView.setText(mGameWinner);
        winnerMsgTextView.setText(mGameOverStatus);

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

        // Update the scoreboard
        updateScoreBoard();
    }

    /**
     * Saves the game state when activity is being stopped. For example when rotating screen
     *
     * @param outState is the {@link Bundle} containing the key-value pairs of the game state we
     *                 wish to save
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        // Retrieves the current layout and saves it
        mCurrentLayout = mLayoutFlipper.getDisplayedChild();

        outState.putInt(STATE_BOARD_SIZE, mBoardSize);
        outState.putInt(STATE_O_SCORE, mOScore);
        outState.putInt(STATE_X_SCORE, mXScore);
        outState.putString(STATE_CURRENT_PLAYER, mCurrentPlayer);
        outState.putString(STATE_MACHINE_PLAYER, mMachinePlayer);
        outState.putString(STATE_FIRST_PLAYER, mFirstPlayer);
        outState.putString(STATE_SECOND_PLAYER, mSecondPlayer);
        outState.putString(STATE_GAME_STATUS, mGameStatus);
        outState.putBoolean(STATE_GAME_STARTED, mGameStarted);
        outState.putSerializable(STATE_GAME_BOARD, mGameBoard);
        outState.putInt(STATE_CURRENT_LAYOUT, mCurrentLayout);
        outState.putString(STATE_GAME_WINNER, mGameWinner);
        outState.putString(STATE_GAME_OVER, mGameOverStatus);
        outState.putBoolean(STATE_PLAY_A_FRIEND, mPlayAFriend);

        super.onSaveInstanceState(outState);
    }

    /**
     * Restores the game state after the activity is resumed. E.g. after screen rotation
     *
     * @param savedInstanceState is the {@link Bundle} contianing the key-value pairs of the game
     *                           state we wish to restore
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mBoardSize = savedInstanceState.getInt(STATE_BOARD_SIZE);
        mOScore = savedInstanceState.getInt(STATE_O_SCORE);
        mXScore = savedInstanceState.getInt(STATE_X_SCORE);
        mCurrentPlayer = savedInstanceState.getString(STATE_CURRENT_PLAYER);
        mMachinePlayer = savedInstanceState.getString(STATE_MACHINE_PLAYER);
        mFirstPlayer = savedInstanceState.getString(STATE_FIRST_PLAYER);
        mSecondPlayer = savedInstanceState.getString(STATE_SECOND_PLAYER);
        mGameStatus = savedInstanceState.getString(STATE_GAME_STATUS);
        mGameStarted = savedInstanceState.getBoolean(STATE_GAME_STARTED);
        mGameBoard = (String[][]) savedInstanceState.getSerializable(STATE_GAME_BOARD);
        mCurrentLayout = savedInstanceState.getInt(STATE_CURRENT_LAYOUT);
        mGameWinner = savedInstanceState.getString(STATE_GAME_WINNER);
        mGameOverStatus = savedInstanceState.getString(STATE_GAME_OVER);
        mPlayAFriend = savedInstanceState.getBoolean(STATE_PLAY_A_FRIEND);
        refreshGameBoard();
    }

    /**
     * Prepares the game Board for a new game
     * Loops through all the cells of the board, setting click listeners to all of them
     */
    private void initializeGameBoard() {
        // Initialize the mGameBoard with the mBoardSize
        mGameBoard = new String[mBoardSize][mBoardSize];

        // Set the game status
        mGameStatus = INITIAL_GAME_STATUS;
        mGameStatusTextView = findViewById(R.id.game_status_textview);
        mGameStatusTextView.setText(mGameStatus);

        // Set the default user player to "X" and default machine player to "O"
        mFirstPlayer = X_PLAYER;
        mCurrentPlayer = mFirstPlayer;
        if (mPlayAFriend) {
            mSecondPlayer = O_PLAYER;
        } else {
            mMachinePlayer = O_PLAYER;
        }

        // Get layout resources
        Resources resources = getResources();
        String packageName = getPackageName();

        //Prefix for cell IDs. Default is three square
        if (mBoardSize == 5) mCellPrefix = "five_cell_";
        else mCellPrefix = "three_cell_";

        for (int i = 0; i < mBoardSize; i++) {
            for (int j = 0; j < mBoardSize; j++) {

                // Retrieve the TextView ID using the name. Cells have ids cell_00, cell_01, and so on
                int resId = resources.getIdentifier(mCellPrefix + i + j, "id", packageName);
                TextView gameCell = findViewById(resId);

                // Set all cells to blank
                mGameBoard[i][j] = null;
                gameCell.setText(null);

                // Store the cell location in the view's tag for easy retrieval of the
                // cell information next time
                int[] cell = {i, j};
                gameCell.setTag(cell);

                // Set the click listener
                gameCell.setOnClickListener(mCellOnClickListener);
            }
        }

        // Use the layout flipper to set the correct layout
        // This is useful when coming out from end of game. That is starting a new game by
        // clicking on the result display text view
        if (mBoardSize == 3) {
            mLayoutFlipper.setDisplayedChild(mLayoutFlipper
                    .indexOfChild(findViewById(R.id.three_square_layout)));
        } else {
            mLayoutFlipper.setDisplayedChild(mLayoutFlipper
                    .indexOfChild(findViewById(R.id.five_square_layout)));
        }

        // Finally, enable game buttons
        enableGameButtons();

    }

    /**
     * This is the beginning of game activity
     * Setup the board and some of the click listeners
     */
    private void initializeGame() {

        // Set the board size to default size
        setBoardSize(DEFAULT_BOARD_SIZE);

        mXPlayerButton = findViewById(R.id.xPlayerBtn);
        mXPlayerButton.setOnClickListener(mXPlayerButtonOnClickListener);

        mOPlayerButton = findViewById(R.id.oPlayerBtn);
        mOPlayerButton.setOnClickListener(mOPlayerButtonOnClickListener);

        Button resetGameButton = findViewById(R.id.resetBtn);
        resetGameButton.setOnClickListener(mResetButtonOnClickListener);

        Button restartGameButton = findViewById(R.id.restartBtn);
        restartGameButton.setOnClickListener(mRestartButtonOnClickListener);

        // Configure buttons to change layout from 3 square to 5 square and vice versa
        // By default board is 3 x 3 so disable the button
        mGridChoiceRadioGroup = findViewById(R.id.grid_choice_radio_group);
        mGridChoiceRadioGroup.setOnCheckedChangeListener(mGridChoiceOnChangeListener);

        // Configure game mode radio buttons
        mGameModeRadioGroup = findViewById(R.id.game_mode_radio_group);
        mGameModeRadioGroup.setOnCheckedChangeListener(mGameModeChangeListener);

        // Configure layout flipper
        mLayoutFlipper = findViewById(R.id.layout_flipper);
//        mLayoutFlipper.showNext();
    }


    /**
     * Analyses the game board checking for winning row, column or diagonal
     *
     * @param cellPlayed: specifies the cell played in form of {rowId, colId}
     */
    private void analyseBoard(int[] cellPlayed) {
        final Handler handler = new Handler();
        // Checks for a win
        if (isRowWin(cellPlayed) || isColumnWin(cellPlayed) || isDiagonalWin(cellPlayed)) {
            if (mCurrentPlayer.equals(X_PLAYER)) {
                mXScore++;
            } else if (mCurrentPlayer.equals(O_PLAYER)) {
                mOScore++;
            }
            // Display the result after a little delay

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateScoreBoard();
                    displayResult(WINNER_MSG, mCurrentPlayer);
                }
            }, 200);

        } else if (boardIsFilled()) {
            displayResult(DRAW_MSG, X_PLAYER + O_PLAYER);
        } else {

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    nextTurn();
                }
            }, 200);
        }

    }

    /**
     * Determines who plays next: whether it's first player, second player (two player mode) or machine
     * After each player's turn, enable clicks on empty cells only for the next player
     */
    private void nextTurn() {
        if (Objects.equals(mCurrentPlayer, mFirstPlayer)) {
            // Friends turn to play
            if (mPlayAFriend) {
                mCurrentPlayer = mSecondPlayer;
                setGameStatus(mCurrentPlayer + " - turn");
                enableClickOnEmptyCells();
            } else {
                machinePlay();
            }
        } else {
            mCurrentPlayer = mFirstPlayer;  // Then wait for user click
            setGameStatus(mCurrentPlayer + " - turn");
            enableClickOnEmptyCells();
        }
    }

    /**
     * Checks whether all cell has been played
     *
     * @return true if all cell is played or false otherwise
     */
    private boolean boardIsFilled() {
        for (int i = 0; i < mBoardSize; i++) {
            for (int j = 0; j < mBoardSize; j++) {
                if (mGameBoard[i][j] == null)
                    return false;
            }
        }
        return true;
    }

    private void displayResult(String status, String player) {
        // New game not started
        mGameStarted = false;
        // Display game status
        setGameStatus(GAME_OVER);
        // Get the textviews for the congratulatory winner message
        TextView winnerCharTextView = findViewById(R.id.winner_char_text_view);
        TextView winnerMsgTextView = findViewById(R.id.winner_msg_text_view);

        mGameWinner = player;
        mGameOverStatus = status;
        winnerCharTextView.setText(player);
        winnerMsgTextView.setText(status);

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
        mLayoutFlipper.setDisplayedChild(mLayoutFlipper
                .indexOfChild(findViewById(R.id.result_layout)));
    }

    /**
     * Updates the scoreBoard for the current player. This is called when the current player wins the game
     * The scores are displayed on the respective player buttons - to the right
     */
    private void updateScoreBoard() {
        if (mXScore != 0) mXPlayerButton.setText(getScoreAsString(mXScore));
        if (mOScore != 0) mOPlayerButton.setText(getScoreAsString(mOScore));
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
        // Example, if the cell played is {1, 1}, the entire row is the second array in the mGameBoard
        String[] row = mGameBoard[cellPlayed[0]];
        return itemsAreIdentical(row);
    }

    /**
     * Checks whether the column specified by the cell is a win
     *
     * @param cellPlayed: specifies the cell played as an array of two digits representing the row and column
     * @return true or false, win or not
     */
    private boolean isColumnWin(int[] cellPlayed) {
        // Get the entire column specified by the cell
        int colId = cellPlayed[1];

        // Initialize the column according to mBoardSize
        String[] column = new String[mBoardSize];

        // Get all the items in the specified column
        for (int i = 0; i < mBoardSize; i++) {
            column[i] = mGameBoard[i][colId];
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
     * the second diagonal or 3 if not on a diagonal
     */
    private int whichDiagonal(int[] cellPlayed) {
        int rowId = cellPlayed[0], colId = cellPlayed[1];
        // Check for middle cell
        if (rowId == colId && rowId + colId == mBoardSize - 1) {
            return 0;
        }
        // First diagonal
        else if (rowId == colId) {
            return 1;
        }
        // Second diagonal
        else if (rowId + colId == mBoardSize - 1) {
            return 2;
        } else return 3;
    }

    /**
     * Retrieves the first diagonal and
     *
     * @return an array of all the cells in the diagonal
     */
    private String[] getFirstDiagonal() {
        String[] diagonal = new String[mBoardSize];
        for (int i = 0; i < mBoardSize; i++) {
            diagonal[i] = mGameBoard[i][i];
        }
        return diagonal;
    }

    /**
     * Retrieves the second diagonal and
     *
     * @return an array of all the cells in the diagonal
     */
    private String[] getSecondDiagonal() {
        String[] diagonal = new String[mBoardSize];
        for (int j = 0, k = mBoardSize - 1; j < mBoardSize; j++, k--) {
            diagonal[j] = mGameBoard[j][k];
        }
        return diagonal;
    }

    /**
     * Checks that all items in an array are equal or identical
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
        mGameStatus = msg;
        mGameStatusTextView.setText(mGameStatus);
    }

    /**
     * Setting of the board size is handled  here
     *
     * @param boardSize: either THREE_SQUARE_BOARD (3) or FIVE_SQUARE_BOARD (5)
     */
    private void setBoardSize(int boardSize) {
        this.mBoardSize = boardSize;
    }

    /**
     * Resets the game including the scoreboard and every other thing
     */
    private void resetGameBoard() {
        mXScore = 0;
        mOScore = 0;
        mXPlayerButton.setText(R.string.score_placeholder);
        mOPlayerButton.setText(R.string.score_placeholder);
        initializeGameBoard();
        enableGameButtons();

    }

    /**
     * Restarts the current game, only resetting the board and other variables and not the score board
     */
    private void restartGame() {
        initializeGameBoard();
        enableGameButtons();
    }

    /**
     * Enable buttons. This is called when restarting or resetting a game
     */
    private void enableGameButtons() {
        mXPlayerButton.setClickable(true);
        mOPlayerButton.setClickable(true);
    }

    /**
     * Disable all game buttons once game play's started
     */
    private void disableGameButtons() {
        mXPlayerButton.setClickable(false);
        mOPlayerButton.setClickable(false);
    }

    /**
     * Iterates through the cells of the game board retrieving the empty cells
     *
     * @return an {@link ArrayList} of the empty cells
     */
    private ArrayList<Integer[]> getEmptyCells() {
        ArrayList<Integer[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < mBoardSize; i++) {
            for (int j = 0; j < mBoardSize; j++) {
                if (mGameBoard[i][j] == null) {
                    Integer[] emptyCell = new Integer[2];
                    emptyCell[0] = i;
                    emptyCell[1] = j;
                    emptyCells.add(emptyCell);
                }
            }
        }
        return emptyCells;
    }

    /**
     * Disables clicks on all cells of the game board. This is called when it's machine's turn to play
     * To avoid unnecessary trigger by human fingers and also after user plays to avoid multiple play
     */
    private void disableClickOnCells() {
        // Get layout resources
        Resources resources = getResources();
        String packageName = getPackageName();

        for (int i = 0; i < mBoardSize; i++) {
            for (int j = 0; j < mBoardSize; j++) {
                // Retrieve the TextView ID using the name. Cells have ids cell_00, cell_01, and so on
                int resId = resources.getIdentifier(mCellPrefix + i + j, "id", packageName);
                TextView gameCell = findViewById(resId);

                // Disable click
                gameCell.setClickable(false);
            }
        }
    }

    /**
     * Enables clicks on empty cells. Called when it's is human turn to play
     */
    private void enableClickOnEmptyCells() {
        // Get layout resources
        Resources resources = getResources();
        String packageName = getPackageName();

        for (int i = 0; i < mBoardSize; i++) {
            for (int j = 0; j < mBoardSize; j++) {
                // Retrieve the TextView ID using the name. Cells have ids cell_00, cell_01, and so on
                int resId = resources.getIdentifier(mCellPrefix + i + j, "id", packageName);
                TextView gameCell = findViewById(resId);

                // Enable click
                if (mGameBoard[i][j] == null)
                    gameCell.setClickable(true);
            }
        }
    }

    /**
     * Handles the machine play algorithm
     * Selects a random empty cell and plays
     */
    private void machinePlay() {
        mCurrentPlayer = mMachinePlayer;
        setGameStatus(mCurrentPlayer + " - turn");

        // Get layout resources
        final Resources resources = getResources();
        final String packageName = getPackageName();

        // Disable Click on cells while machine is playing game
        disableClickOnCells();

        // Gets all the empty cells
        ArrayList<Integer[]> emptyCells = getEmptyCells();

        // Get a random cell to play from the empty ones
        int playIndex = random.nextInt(emptyCells.size());
        Integer[] playCell = emptyCells.get(playIndex);

        // The conversion to int individually is necessary as int[] is not compatible with Integer[]
        final int cellRowId = playCell[0];
        final int cellColId = playCell[1];
        final int[] cellPlayed = {cellRowId, cellColId};

        // update the game board
        mGameBoard[cellRowId][cellColId] = mCurrentPlayer;

        // Use handler to play
        final Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Display  after 100ms
                // Retrieve the TextView ID using the name. Cells have ids cell_00, cell_01, and so on
                int resId = resources.getIdentifier(mCellPrefix + cellRowId + cellColId,
                        "id", packageName);
                TextView gameCell = findViewById(resId);

                // Update the game board text View
                gameCell.setText(mCurrentPlayer);
                // TODO: animate this display

                // Analyse the board: checking for win, end of game, etc
                analyseBoard(cellPlayed);
            }
        }, 300);

    }
}
