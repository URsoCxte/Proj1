package Project1;

import java.util.*;

/**
 *
 * 6713225 Chatchawal Labkim 6713232 Thanapon Rojanabenjakul 6713234 Thanawat
 * Mekwattanawanit
 */
public class MainApplication {

    class marbleBoard {

        private String[] Board;
        private int num;
        private int emptyIndex;
        private Set<String> doneMarbles = new HashSet<>();

        public marbleBoard(int n) {
            this.num = n;
            this.Board = new String[2 * n + 1];
            initialBoard();
        }

        public void initialBoard() {
            for (int i = 0; i < num; i++) {
                Board[i] = "w" + i;
            }
            emptyIndex = num;
            Board[emptyIndex] = "__";
            for (int i = 0; i < num; i++) {
                Board[num + 1 + i] = "b" + i;
            }
            doneMarbles.clear();
        }

        public void printBoard() {
            for (String n : Board) {
                System.out.printf(n + ' ');
            }
            System.out.println();
        }

        public boolean isGameOver() {
            for (int i = 0; i < num; i++) {
                if (!Board[i].startsWith("b")) {
                    return false;
                }
            }
            for (int i = num + 1; i < Board.length; i++) {
                if (!Board[i].startsWith("w")) {
                    return false;
                }
            }
            return true;
        }

        public String[] getBoard() {
            return Board;
        }

        public int getNum() {
            return num;
        }

        public Set<String> getDoneMarbles() {
            return doneMarbles;
        }

        // Restore board state from saved state
        public void restoreState(String[] savedBoard, Set<String> savedDone) {
            for (int i = 0; i < Board.length; i++) {
                Board[i] = savedBoard[i];
                if (Board[i].equals("__")) {
                    emptyIndex = i;
                }
            }
            doneMarbles.clear();
            doneMarbles.addAll(savedDone);
        }

        public boolean move(String marbleId, boolean silent) {
            int currentIndex = -1;

            for (int i = 0; i < Board.length; i++) {
                if (Board[i].equals(marbleId)) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex == -1) {
                if (!silent) System.out.printf("%19s %s\n", "Not Found", marbleId);
                return false;
            }

            boolean canMove = false;
            String moveType = "";

            if (marbleId.startsWith("w")) {
                if (currentIndex + 1 == emptyIndex) {
                    canMove = true;
                    moveType = "Move right";
                } else if (currentIndex + 2 == emptyIndex && Board[currentIndex + 1].startsWith("b")) {
                    canMove = true;
                    moveType = "Jump right";
                }
            }

            if (marbleId.startsWith("b")) {
                if (currentIndex - 1 == emptyIndex) {
                    canMove = true;
                    moveType = "Move left";
                } else if (currentIndex - 2 == emptyIndex && Board[currentIndex - 1].startsWith("w")) {
                    canMove = true;
                    moveType = "Jump left";
                }
            }

            if (canMove) {
                if (!silent) System.out.printf("%12s : %s\n", marbleId, moveType);
                Board[emptyIndex] = Board[currentIndex];
                Board[currentIndex] = "__";
                emptyIndex = currentIndex;

                int newIndex = -1;
                for (int i = 0; i < Board.length; i++) {
                    if (Board[i].equals(marbleId)) {
                        newIndex = i;
                        break;
                    }
                }

                // Mark done if reached end
                if (marbleId.startsWith("w") && newIndex == Board.length - 1) {
                    doneMarbles.add(marbleId);
                    if (!silent) System.out.printf("%12s : %s is done!\n", marbleId, marbleId);
                } else if (marbleId.startsWith("b") && newIndex == 0) {
                    doneMarbles.add(marbleId);
                    if (!silent) System.out.printf("%12s : %s is done!\n", marbleId, marbleId);
                }

                // Mark done if next to a done marble of same color
                if (!doneMarbles.contains(marbleId)) {
                    if (marbleId.startsWith("w")) {
                        if (newIndex + 1 < Board.length
                                && Board[newIndex + 1].startsWith("w")
                                && doneMarbles.contains(Board[newIndex + 1])) {
                            doneMarbles.add(marbleId);
                            if (!silent) System.out.printf("%12s : %s is done!\n", marbleId, marbleId);
                        }
                    } else if (marbleId.startsWith("b")) {
                        if (newIndex - 1 >= 0
                                && Board[newIndex - 1].startsWith("b")
                                && doneMarbles.contains(Board[newIndex - 1])) {
                            doneMarbles.add(marbleId);
                            if (!silent) System.out.printf("%12s : %s is done!\n", marbleId, marbleId);
                        }
                    }
                }

                return true;
            } else {
                if (!silent) System.out.printf("%21s: %s\n", "Cannot move", marbleId);
                return false;
            }
        }
    }

    // State class to save board state for backtracking
    class BoardState {
        String[] board;
        Set<String> doneMarbles;
        String lastMovedMarble; // which marble was just moved to reach this state
        String color;           // which color was being tried

        BoardState(String[] board, Set<String> done, String lastMoved, String color) {
            this.board = Arrays.copyOf(board, board.length);
            this.doneMarbles = new HashSet<>(done);
            this.lastMovedMarble = lastMoved;
            this.color = color;
        }
    }

    private int getEmptyIndex(String[] board) {
        for (int i = 0; i < board.length; i++) {
            if (board[i].equals("__")) return i;
        }
        return -1;
    }

    // Get list of movable marbles for a color, checking Rule 1 (no same-color non-done neighbor at landing)
    // Returns ordered list: white searches right-to-left, black searches left-to-right
    private List<String> getMovableMarblesInOrder(String[] boardArr, String color, Set<String> doneMarbles, String skipMarble) {
        List<String> candidates = new ArrayList<>();
        int emptyIdx = getEmptyIndex(boardArr);

        if (color.equals("w")) {
            for (int i = boardArr.length - 1; i >= 0; i--) {
                if (boardArr[i].startsWith("w")) {
                    String marbleId = boardArr[i];
                    if (marbleId.equals(skipMarble)) continue;

                    boolean canMove = (i + 1 == emptyIdx)
                            || (i + 2 == emptyIdx && boardArr[i + 1].startsWith("b"));

                    if (canMove) {
                        int landingPos = emptyIdx;
                        // Rule 1: landing position must not have a non-done same color marble next to it
                        boolean violatesRule1 = (landingPos + 1 < boardArr.length)
                                && boardArr[landingPos + 1].startsWith("w")
                                && !doneMarbles.contains(boardArr[landingPos + 1]);

                        if (!violatesRule1) {
                            candidates.add(marbleId);
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < boardArr.length; i++) {
                if (boardArr[i].startsWith("b")) {
                    String marbleId = boardArr[i];
                    if (marbleId.equals(skipMarble)) continue;

                    boolean canMove = (i - 1 == emptyIdx)
                            || (i - 2 == emptyIdx && boardArr[i - 1].startsWith("w"));

                    if (canMove) {
                        int landingPos = emptyIdx;
                        boolean violatesRule1 = (landingPos - 1 >= 0)
                                && boardArr[landingPos - 1].startsWith("b")
                                && !doneMarbles.contains(boardArr[landingPos - 1]);

                        if (!violatesRule1) {
                            candidates.add(marbleId);
                        }
                    }
                }
            }
        }

        return candidates;
    }

    public void autoSolve(marbleBoard board, int startStep) {
        int step = startStep;

        // Stack stores states BEFORE each move, along with which marble was moved
        Deque<BoardState> stack = new ArrayDeque<>();

        String currentColor = "w";

        // Try both colors alternately using backtracking
        // We need a different approach: at each step, try all possible moves
        // Stack stores: (state before move, marble that was moved, color tried)

        // Save initial state
        String[] initBoard = Arrays.copyOf(board.getBoard(), board.getBoard().length);
        Set<String> initDone = new HashSet<>(board.getDoneMarbles());

        // We use an iterative DFS with explicit stack
        // Each stack entry = state before a move was applied
        // We also track which marbles have been tried from each state

        // Redesign: stack holds BoardState (state BEFORE move) + index of next marble to try
        // Use a different stack structure

        // Stack of: [savedState, listOfCandidates, nextCandidateIndex]
        Deque<Object[]> dfsStack = new ArrayDeque<>();

        // Get initial candidates
        List<String> initCandidates = new ArrayList<>();
        initCandidates.addAll(getMovableMarblesInOrder(board.getBoard(), "w", board.getDoneMarbles(), null));
        initCandidates.addAll(getMovableMarblesInOrder(board.getBoard(), "b", board.getDoneMarbles(), null));

        if (!initCandidates.isEmpty()) {
            dfsStack.push(new Object[]{
                new BoardState(board.getBoard(), board.getDoneMarbles(), null, currentColor),
                initCandidates,
                0
            });
        }

        boolean solved = false;

        while (!dfsStack.isEmpty()) {
            Object[] top = dfsStack.peek();
            BoardState savedState = (BoardState) top[0];
            List<String> candidates = (List<String>) top[1];
            int idx = (int) top[2];

            if (idx >= candidates.size()) {
                // Backtrack: no more candidates to try from this state
                dfsStack.pop();
                // Restore board to savedState
                board.restoreState(savedState.board, savedState.doneMarbles);
                step--;
                continue;
            }

            // Try next candidate
            top[2] = idx + 1; // advance index for next time we revisit this frame

            String marbleToMove = candidates.get(idx);

            // Restore board to the saved state before attempting this move
            board.restoreState(savedState.board, savedState.doneMarbles);

            // Attempt move
            boolean moved = board.move(marbleToMove, true);
            if (!moved) continue;

            // Print board state
            System.out.print("Auto " + step + " >> ");
            board.printBoard();
            step++;

            if (board.isGameOver()) {
                System.out.println("Done !!");
                solved = true;
                break;
            }

            // Compute next candidates from new state (both colors, alternating - try current then opposite)
            String movedColor = marbleToMove.startsWith("w") ? "w" : "b";
            String otherColor = movedColor.equals("w") ? "b" : "w";

            List<String> nextCandidates = new ArrayList<>();
            // Try same color first, then other color
            nextCandidates.addAll(getMovableMarblesInOrder(board.getBoard(), movedColor, board.getDoneMarbles(), null));
            nextCandidates.addAll(getMovableMarblesInOrder(board.getBoard(), otherColor, board.getDoneMarbles(), null));

            if (!nextCandidates.isEmpty()) {
                dfsStack.push(new Object[]{
                    new BoardState(board.getBoard(), board.getDoneMarbles(), marbleToMove, movedColor),
                    nextCandidates,
                    0
                });
            }
            // else: no moves from this state, loop will backtrack
        }

        if (!solved) {
            System.out.println("No solution !!");
        }
    }

    public void menu() {
        Scanner scan = new Scanner(System.in);
        int marble_num;
        do {
            do {
                System.out.printf("Enter number of white marbles = ");
                marble_num = scan.nextInt();

                if (marble_num >= 2) {
                    System.out.print("Initial >> ");
                    marbleBoard board = new marbleBoard(marble_num);
                    board.printBoard();

                    int step = 1;
                    while (true) {
                        System.out.println("=".repeat(103));
                        System.out.print("Step " + step + " >> Enter marble ID or A to switch to auto mode = ");
                        String input = scan.next();

                        if (input.equalsIgnoreCase("A")) {
                            System.out.println("Switching to Auto Mode...");
                            autoSolve(board, step);
                            break;
                        }

                        if (board.move(input, false)) {
                            System.out.printf("%15s : ", "Board");
                            board.printBoard();
                        }
                        step++;

                        if (board.isGameOver()) {
                            System.out.println("=".repeat(103));
                            System.out.println("You win!");
                            break;
                        }
                    }

                } else {
                    System.out.println("n must be at least 2.");
                }

            } while (marble_num < 2);

            System.out.println("=".repeat(103));
            System.out.println("Do you want to play again?(y/n)");

        } while (scan.next().equalsIgnoreCase("y"));
    }

    public static void main(String[] args) {
        MainApplication mainapp = new MainApplication();
        mainapp.menu();
    }
}
