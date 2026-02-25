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
        private Set<String> doneMarbles = new HashSet<>();  // track done marbles

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

        // Check if a marble's neighbor in its direction is done
        public boolean isNeighborDone(int currentIndex, String marbleId) {
            if (marbleId.startsWith("w")) {
                // White moves right, check right neighbor
                int rightNeighbor = currentIndex + 1;
                if (rightNeighbor < Board.length && !Board[rightNeighbor].equals("__")) {
                    return doneMarbles.contains(Board[rightNeighbor]);
                }
            } else {
                // Black moves left, check left neighbor
                int leftNeighbor = currentIndex - 1;
                if (leftNeighbor >= 0 && !Board[leftNeighbor].equals("__")) {
                    return doneMarbles.contains(Board[leftNeighbor]);
                }
            }
            return false;
        }

        public boolean move(String marbleId, boolean silent) {
            int currentIndex = -1;

            for (int i = 0; i < Board.length; i++) {
                if (Board[i].equals(marbleId)) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex == -1 && !marbleId.equalsIgnoreCase("a")) {
                System.out.printf("%19s %s\n", "Not Found", marbleId);
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
                } // Rule 2: allow move into same color if that neighbor is done
                else if (currentIndex + 1 < Board.length && Board[currentIndex + 1].startsWith("w")
                        && doneMarbles.contains(Board[currentIndex + 1])
                        && currentIndex + 1 == emptyIndex) {
                    canMove = true;
                    moveType = "Move right (done neighbor)";
                }
            }

            if (marbleId.startsWith("b")) {
                if (currentIndex - 1 == emptyIndex) {
                    canMove = true;
                    moveType = "Move left";
                } else if (currentIndex - 2 == emptyIndex && Board[currentIndex - 1].startsWith("w")) {
                    canMove = true;
                    moveType = "Jump left";
                } // Rule 2: allow move into same color if that neighbor is done
                else if (currentIndex - 1 >= 0 && Board[currentIndex - 1].startsWith("b")
                        && doneMarbles.contains(Board[currentIndex - 1])
                        && currentIndex - 1 == emptyIndex) {
                    canMove = true;
                    moveType = "Move left (done neighbor)";
                }
            }

            if (canMove) {
                if (!silent) System.out.printf("%12s : %s\n", marbleId, moveType);
                Board[emptyIndex] = Board[currentIndex];
                Board[currentIndex] = "__";
                emptyIndex = currentIndex;

                // Find new position of marble
                int newIndex = -1;
                for (int i = 0; i < Board.length; i++) {
                    if (Board[i].equals(marbleId)) {
                        newIndex = i;
                        break;
                    }
                }

                // Rule 1: check if marble has reached the end
                if (marbleId.startsWith("w") && newIndex == Board.length - 1) {
                    doneMarbles.add(marbleId);
                    if (!silent) System.out.printf("%12s : %s is done!\n", marbleId, marbleId);
                } else if (marbleId.startsWith("b") && newIndex == 0) {
                    doneMarbles.add(marbleId);
                    if (!silent) System.out.printf("%12s : %s is done!\n", marbleId, marbleId);
                }

                if (!doneMarbles.contains(marbleId)) {
                    if (marbleId.startsWith("w")) {
                        // Check right neighbor
                        if (newIndex + 1 < Board.length
                                && Board[newIndex + 1].startsWith("w")
                                && doneMarbles.contains(Board[newIndex + 1])) {
                            doneMarbles.add(marbleId);
                            if (!silent) System.out.printf("%12s : %s is done!\n", marbleId, marbleId);
                        }
                    } else if (marbleId.startsWith("b")) {
                        // Check left neighbor
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
                if (!marbleId.equalsIgnoreCase("a")) {
                    if (!silent) System.out.printf("%21s: %s\n", "Cannot move", marbleId);
                }
                return false;
            }
        }
    }

// Updated tryMove to respect done status
    private boolean tryMove(marbleBoard board, String color) {
        String[] boardArr = board.getBoard();
        Set<String> doneMarbles = board.getDoneMarbles();

        if (color.equals("w")) {
            for (int i = boardArr.length - 1; i >= 0; i--) {
                if (boardArr[i].startsWith("w")) {
                    int emptyIdx = getEmptyIndex(boardArr);
                    boolean canMove = (i + 1 == emptyIdx)
                            || (i + 2 == emptyIdx && boardArr[i + 1].startsWith("b"));

                    if (canMove) {
                        int landingPos = emptyIdx;
                        boolean rightSame = (landingPos + 1 < boardArr.length)
                                && boardArr[landingPos + 1].startsWith("w")
                                && !doneMarbles.contains(boardArr[landingPos + 1]); // Rule 2: allow if done

                        if (!rightSame) {
                            if (board.move(boardArr[i], true)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < boardArr.length; i++) {
                if (boardArr[i].startsWith("b")) {
                    int emptyIdx = getEmptyIndex(boardArr);
                    boolean canMove = (i - 1 == emptyIdx)
                            || (i - 2 == emptyIdx && boardArr[i - 1].startsWith("w"));

                    if (canMove) {
                        int landingPos = emptyIdx;
                        boolean leftSame = (landingPos - 1 >= 0)
                                && boardArr[landingPos - 1].startsWith("b")
                                && !doneMarbles.contains(boardArr[landingPos - 1]); // Rule 2: allow if done

                        if (!leftSame) {
                            if (board.move(boardArr[i], true)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean checkViolatesRule1(String[] boardArr, String color, Set<String> doneMarbles) {
        if (color.equals("w")) {
            for (int i = boardArr.length - 1; i >= 0; i--) {
                if (boardArr[i].startsWith("w")) {
                    int emptyIdx = getEmptyIndex(boardArr);
                    boolean canMove = (i + 1 == emptyIdx)
                            || (i + 2 == emptyIdx && boardArr[i + 1].startsWith("b"));

                    if (canMove) {
                        int landingPos = emptyIdx;
                        boolean rightSame = (landingPos + 1 < boardArr.length)
                                && boardArr[landingPos + 1].startsWith("w")
                                && !doneMarbles.contains(boardArr[landingPos + 1]);
                        return rightSame;
                    }
                }
            }
        } else {
            for (int i = 0; i < boardArr.length; i++) {
                if (boardArr[i].startsWith("b")) {
                    int emptyIdx = getEmptyIndex(boardArr);
                    boolean canMove = (i - 1 == emptyIdx)
                            || (i - 2 == emptyIdx && boardArr[i - 1].startsWith("w"));

                    if (canMove) {
                        int landingPos = emptyIdx;
                        boolean leftSame = (landingPos - 1 >= 0)
                                && boardArr[landingPos - 1].startsWith("b")
                                && !doneMarbles.contains(boardArr[landingPos - 1]);
                        return leftSame;
                    }
                }
            }
        }
        return false;
    }

    private boolean canColorMove(String[] boardArr, String color) {
        if (color.equals("w")) {
            for (int i = boardArr.length - 1; i >= 0; i--) {
                if (boardArr[i].startsWith("w")) {
                    int emptyIdx = getEmptyIndex(boardArr);
                    if ((i + 1 == emptyIdx)
                            || (i + 2 == emptyIdx && boardArr[i + 1].startsWith("b"))) {
                        return true;
                    }
                }
            }
        } else {
            for (int i = 0; i < boardArr.length; i++) {
                if (boardArr[i].startsWith("b")) {
                    int emptyIdx = getEmptyIndex(boardArr);
                    if ((i - 1 == emptyIdx)
                            || (i - 2 == emptyIdx && boardArr[i - 1].startsWith("w"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void autoSolve(marbleBoard board, int startStep) {
        int step = startStep;
        String[] boardArr = board.getBoard();
        String currentColor = "w";

        while (!board.isGameOver()) {
            

            boolean moved = false;
            boolean switchedDueToRule1 = false;

            moved = tryMove(board, currentColor);

            if (!moved) {
                boolean violatesRule1 = checkViolatesRule1(boardArr, currentColor, board.getDoneMarbles());

                if (violatesRule1) {
                    currentColor = currentColor.equals("w") ? "b" : "w";
                    switchedDueToRule1 = true;
                } else {
                    // Rule 3: no valid move, try switching color
                    String oppositeColor = currentColor.equals("w") ? "b" : "w";
                    if (!checkViolatesRule1(boardArr, oppositeColor, board.getDoneMarbles())
                            && canColorMove(boardArr, oppositeColor)) {
                        currentColor = oppositeColor;
                    } else {
                        System.out.println("No solution !!");
                        return;
                    }
                }
            }

            if (switchedDueToRule1) {
                continue;
            }
            if (!moved) {
                continue;
            }
            
            //System.out.println("=".repeat(103));
            System.out.print("Auto " + step + " >> ");
            //System.out.printf("%15s : ", "Board");
            board.printBoard();
            step++;
        }

        if (board.isGameOver()) {
            //System.out.println("=".repeat(103));
            System.out.println("Done !!");
        }
    }

    private int getEmptyIndex(String[] board) {
        for (int i = 0; i < board.length; i++) {
            if (board[i].equals("__")) {
                return i;
            }
        }
        return -1;
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
                            autoSolve(board, step);  // ← auto mode called here
                            break;
                        }
                        
                        

                        if (board.move(input, false)) {
                            System.out.printf("%15s : ", "Board");
                            board.printBoard();
                        }
                        step++;
                        if (input.equalsIgnoreCase("A")) {
                            System.out.println("Switching to Auto Mode...");

                            break;
                        }

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

