/**
 * ES234317-Algorithm and Data Structures
 * Semester Ganjil, 2024/2025
 * Group Capstone Project
 * Group #4
 * 1 - 5026231012 - Zihni Aryanto Putra Buana
 * 2 - 5026231085 - Firmansyah Adi Prasetyo
 * 3 - 5026231174 - Muhamamd Razan Parisya Putra
 */


package sudoku.src;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class GameBoardPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final int CELL_SIZE = 60;
    public static final int BOARD_WIDTH  = CELL_SIZE * SudokuConstants.GRID_SIZE;
    public static final int BOARD_HEIGHT = CELL_SIZE * SudokuConstants.GRID_SIZE;

    private Cell[][] cells = new Cell[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
    private Puzzle puzzle = new Puzzle();
    private Timer timer;
    private JLabel fastestTimeLabel;
    private JLabel currentTimeLabel;
    private long fastestTime = Long.MAX_VALUE; // Inisialisasi dengan nilai maksimum
    private long currentTime = 0;
    private String difficultyLevel = "Easy";
    private JPanel numberInput; // Deklarasikan variabel numberInput
    private long startTime; // Waktu mulai permainan

    public GameBoardPanel(Timer timer) {
        super.setLayout(new BorderLayout());

        // Create top panel for scores
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(33, 37, 49));
        JPanel topRightPanel = new JPanel(new BorderLayout());
        topRightPanel.setBackground(new Color(33, 37, 49));
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(33, 37, 49));

        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Create fastest time label
        fastestTimeLabel = new JLabel("Fastest Time: N/A");
        fastestTimeLabel.setFont(new Font("Figtree", Font.PLAIN, 14));
        fastestTimeLabel.setForeground(Color.WHITE);
        topRightPanel.add(fastestTimeLabel, BorderLayout.EAST);

        // Create current time label
        currentTimeLabel = new JLabel("Current Time: 0 seconds");
        currentTimeLabel.setFont(new Font("Figtree", Font.PLAIN, 14));
        currentTimeLabel.setForeground(Color.WHITE);
        topRightPanel.add(currentTimeLabel, BorderLayout.WEST);

        topPanel.add(topRightPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Create number input panel
        numberInput = new JPanel(); // Inisialisasi variabel numberInput
        bottomPanel.add(numberInput, BorderLayout.NORTH);

        // Initialize the cells
        JPanel gridPanel = new JPanel(new GridLayout(SudokuConstants.GRID_SIZE, SudokuConstants.GRID_SIZE));
        gridPanel.setBackground(new Color(33, 37, 49));
        for (int row = 0; row < SudokuConstants.GRID_SIZE; ++row) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; ++col) {
                cells[row][col] = new Cell(row, col);
                gridPanel.add(cells[row][col]);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        CellInputListener listener = new CellInputListener();
        for (int row = 0; row < SudokuConstants.GRID_SIZE; ++row) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; ++col) {
                if (cells[row][col].isEnabled()) {
                    cells[row][col].addActionListener(listener);
                }
            }
        }

        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        this.timer = timer;
        this.startTime = System.currentTimeMillis(); // Set waktu mulai permainan
    }

    public void newGame() {
        puzzle.newPuzzle(this.difficultyLevel);

        // Initialize all the 9x9 cells, based on the puzzle.
        for (int row = 0; row < SudokuConstants.GRID_SIZE; ++row) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; ++col) {
                cells[row][col].newGame(puzzle.numbers[row][col], puzzle.isGiven[row][col]);
            }
        }

        // Reset timer
        this.startTime = System.currentTimeMillis();
        timer.start();
    }

    public void resetGame() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j].status == CellStatus.CORRECT_GUESS || cells[i][j].status == CellStatus.WRONG_GUESS) {
                    cells[i][j].setText("");
                    cells[i][j].status = CellStatus.TO_GUESS;
                    cells[i][j].paint();
                    cells[i][j].setEnabled(true);
                }
                if (cells[i][j].status == CellStatus.CORRECT_GUESS || cells[i][j].status == CellStatus.GIVEN) {
                    cells[i][j].setEnabled(true);
                    timer.start();
                    if (cells[i][j].status == CellStatus.GIVEN) {
                        cells[i][j].setEnabled(false);
                    }
                }
            }
        }
    }

    public boolean isSolved() {
        for (int row = 0; row < SudokuConstants.GRID_SIZE; ++row) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; ++col) {
                if (cells[row][col].status == CellStatus.TO_GUESS || cells[row][col].status == CellStatus.WRONG_GUESS) {
                    return false;
                }
                // Tambahkan pengecekan apakah angka di cell valid
                int numberIn = Integer.parseInt(cells[row][col].getText());
                if (!isValid(row, col, numberIn)) {
                    return false;
                }
            }
        }
        return true;
    }

    private class CellInputListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Get a reference of the JTextField that triggers this action event
            Cell sourceCell = (Cell) e.getSource();

            if (sourceCell.status == CellStatus.WRONG_GUESS || sourceCell.status == CellStatus.TO_GUESS) {
                try {
                    int numberIn = Integer.parseInt(sourceCell.getText());
                    System.out.println("You entered " + numberIn);

                    if (isValid(sourceCell.getRow(), sourceCell.getCol(), numberIn)) {
                        sourceCell.status = CellStatus.CORRECT_GUESS;
                        sourceCell.setForeground(Color.GREEN); // Ubah warna teks menjadi hijau jika valid
                    } else {
                        sourceCell.status = CellStatus.WRONG_GUESS;
                        sourceCell.setForeground(Color.RED); // Ubah warna teks menjadi merah jika tidak valid
                    }
                    sourceCell.paint(); // re-paint this cell based on its status

                    if (isSolved()) {
                        timer.stop();
                        long endTime = System.currentTimeMillis();
                        long timeTaken = (endTime - startTime) / 1000; // Waktu dalam detik
                        JOptionPane.showMessageDialog(null, "Congratulations! You have solved the game in " + timeTaken + " seconds!");
                        updateTimes(timeTaken);
                    } else if (isAllCellsFilled()) {
                        JOptionPane.showMessageDialog(null, "Try again! Some cells are incorrect.");
                    }
                } catch (NumberFormatException ex) {
                    sourceCell.setForeground(Color.RED); // Ubah warna teks menjadi merah jika input bukan angka
                    sourceCell.status = CellStatus.WRONG_GUESS;
                }
            }
        }
    }

    private boolean isAllCellsFilled() {
        for (int row = 0; row < SudokuConstants.GRID_SIZE; ++row) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; ++col) {
                if (cells[row][col].getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setDifficulty(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getDifficulty() {
        return difficultyLevel;
    }

    private void updateTimes(long timeTaken) {
        // Perbarui fastestTime jika waktu penyelesaian saat ini lebih pendek
        if (timeTaken < fastestTime) {
            fastestTime = timeTaken;
            fastestTimeLabel.setText("Fastest Time: " + fastestTime + " seconds");
        }
        currentTime = timeTaken;
        currentTimeLabel.setText("Current Time: " + currentTime + " seconds");
    }

    public void solveGame() {
        // Logika untuk menyelesaikan permainan secara otomatis
        solveSudoku();
        timer.stop();
        long endTime = System.currentTimeMillis();
        long timeTaken = (endTime - startTime) / 1000; // Waktu dalam detik
        JOptionPane.showMessageDialog(null, "Game solved automatically in " + timeTaken + " seconds!");
        updateTimes(timeTaken);
    }

    private boolean solveSudoku() {
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                if (cells[row][col].getText().isEmpty()) {
                    for (int num = 1; num <= SudokuConstants.GRID_SIZE; num++) {
                        if (isValid(row, col, num)) {
                            cells[row][col].setText(String.valueOf(num));
                            cells[row][col].status = CellStatus.CORRECT_GUESS;
                            if (solveSudoku()) {
                                return true;
                            } else {
                                cells[row][col].setText("");
                                cells[row][col].status = CellStatus.TO_GUESS;
                            }
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValid(int row, int col, int num) {
        // Periksa baris dan kolom
        for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
            if (cells[row][i].getText().equals(String.valueOf(num)) || cells[i][col].getText().equals(String.valueOf(num))) {
                return false;
            }
        }
        // Periksa subgrid 3x3
        int boxRowStart = (row / 3) * 3;
        int boxColStart = (col / 3) * 3;
        for (int r = boxRowStart; r < boxRowStart + 3; r++) {
            for (int c = boxColStart; c < boxColStart + 3; c++) {
                if (cells[r][c].getText().equals(String.valueOf(num))) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public void giveHint() {
        Random rand = new Random();
        int maxAttempts = 100; // Batasan jumlah percobaan
        int attempts = 0;
        while (attempts < maxAttempts) {
            int row = rand.nextInt(SudokuConstants.GRID_SIZE);
            int col = rand.nextInt(SudokuConstants.GRID_SIZE);
            if (cells[row][col].getText().isEmpty()) {
                for (int num = 1; num <= SudokuConstants.GRID_SIZE; num++) {
                    if (isValid(row, col, num)) {
                        cells[row][col].setText(String.valueOf(num));
                        cells[row][col].status = CellStatus.CORRECT_GUESS;
                        return;
                    }
                }
            }
            attempts++;
        }
        JOptionPane.showMessageDialog(null, "No valid cell found for hint.");
    }
}