package Project1;

import java.util.*;

/**
 *
 * 6713225 Chatchawal Labkim 6713232 Thanapon Rojanabenjakul 6713234 Thanawat
 * Mekwattanawanit
 */
public class MainApplication {

    // ─── State saved on the explicit stack ───────────────────────────────────
    class BoardState {
        String[]     board;
        Set<String>  doneMarbles;
        String       triedMarble;   // marble moved TO reach this state (null = root)
        int          candidateIdx;  // next candidate index to try FROM this state

        BoardState(String[] board, Set<String> done, String tried) {
            this.board        = Arrays.copyOf(board, board.length);
            this.doneMarbles  = new HashSet<>(done);
            this.triedMarble  = tried;
            this.candidateIdx = 0;
        }
    }

    // ─── marbleBoard ─────────────────────────────────────────────────────────
    class marbleBoard {

        private String[]    Board;
        private int         num;
        private int         emptyIndex;
        private Set<String> doneMarbles = new HashSet<>();

        public marbleBoard(int n) {
            this.num   = n;
            this.Board = new String[2 * n + 1];
            initialBoard();
        }

        public void initialBoard() {
            for (int i = 0; i < num; i++)         Board[i]           = "w" + i;
            emptyIndex = num;
            Board[emptyIndex] = "__";
            for (int i = 0; i < num; i++)         Board[num + 1 + i] = "b" + i;
            doneMarbles.clear();
        }

        public void printBoard() {
            for (String s : Board) System.out.print(s + ' ');
            System.out.println();
        }

        public boolean isGameOver() {
            for (int i = 0; i < num; i++)
                if (!Board[i].startsWith("b")) return false;
            for (int i = num + 1; i < Board.length; i++)
                if (!Board[i].startsWith("w")) return false;
            return true;
        }

        public String[]    getBoard()       { return Board; }
        public int         getNum()         { return num;   }
        public Set<String> getDoneMarbles() { return doneMarbles; }

        /** Restore board to a previously saved BoardState snapshot. */
        public void restoreState(BoardState state) {
            for (int i = 0; i < Board.length; i++) {
                Board[i] = state.board[i];
                if (Board[i].equals("__")) emptyIndex = i;
            }
            doneMarbles.clear();
            doneMarbles.addAll(state.doneMarbles);
        }

        public boolean move(String marbleId, boolean silent) {
            int cur = -1;
            for (int i = 0; i < Board.length; i++)
                if (Board[i].equals(marbleId)) { cur = i; break; }

            if (cur == -1) {
                if (!silent) System.out.printf("%19s %s\n", "Not Found", marbleId);
                return false;
            }

            boolean canMove  = false;
            String  moveType = "";

            if (marbleId.startsWith("w")) {
                if      (cur + 1 == emptyIndex)                                   { canMove = true; moveType = "Move right"; }
                else if (cur + 2 == emptyIndex && Board[cur+1].startsWith("b"))   { canMove = true; moveType = "Jump right"; }
            } else {
                if      (cur - 1 == emptyIndex)                                   { canMove = true; moveType = "Move left";  }
                else if (cur - 2 == emptyIndex && Board[cur-1].startsWith("w"))   { canMove = true; moveType = "Jump left";  }
            }

            if (!canMove) {
                if (!silent) System.out.printf("%21s: %s\n", "Cannot move", marbleId);
                return false;
            }

            if (!silent) System.out.printf("%12s : %s\n", marbleId, moveType);
            Board[emptyIndex] = Board[cur];
            Board[cur]        = "__";
            emptyIndex        = cur;

            int newIdx = -1;
            for (int i = 0; i < Board.length; i++)
                if (Board[i].equals(marbleId)) { newIdx = i; break; }

            // mark done: reached far end
            if      (marbleId.startsWith("w") && newIdx == Board.length - 1) { doneMarbles.add(marbleId); if (!silent) System.out.printf("%12s : %s is done!\n", marbleId, marbleId); }
            else if (marbleId.startsWith("b") && newIdx == 0)                { doneMarbles.add(marbleId); if (!silent) System.out.printf("%12s : %s is done!\n", marbleId, marbleId); }

            // mark done: adjacent to a done marble of same colour
            if (!doneMarbles.contains(marbleId)) {
                if (marbleId.startsWith("w") && newIdx + 1 < Board.length
                        && Board[newIdx+1].startsWith("w")
                        && doneMarbles.contains(Board[newIdx+1])) {
                    doneMarbles.add(marbleId);
                    if (!silent) System.out.printf("%12s : %s is done!\n", marbleId, marbleId);
                } else if (marbleId.startsWith("b") && newIdx - 1 >= 0
                        && Board[newIdx-1].startsWith("b")
                        && doneMarbles.contains(Board[newIdx-1])) {
                    doneMarbles.add(marbleId);
                    if (!silent) System.out.printf("%12s : %s is done!\n", marbleId, marbleId);
                }
            }
            return true;
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private int getEmptyIndex(String[] board) {
        for (int i = 0; i < board.length; i++)
            if (board[i].equals("__")) return i;
        return -1;
    }

    /**
     * Build an ordered candidate list of marbles that can legally move from
     * the current board state, respecting Rule 1.
     *
     * Order:
     *   White marbles → right-to-left scan (index high → low)
     *   Black marbles → left-to-right scan (index low  → high)
     *   Same colour as lastMoved is listed first, then the opposite colour.
     */
    private List<String> buildCandidateList(String[] boardArr,
                                             Set<String> done,
                                             String lastMoved) {
        List<String> result = new ArrayList<>();

        String first  = (lastMoved == null || lastMoved.startsWith("w")) ? "w" : "b";
        String second = first.equals("w") ? "b" : "w";

        for (String color : new String[]{ first, second }) {
            int emptyIdx = getEmptyIndex(boardArr);

            if (color.equals("w")) {
                // scan right-to-left so the marble nearest the gap is tried first
                for (int i = boardArr.length - 1; i >= 0; i--) {
                    if (!boardArr[i].startsWith("w")) continue;
                    boolean canMove = (i + 1 == emptyIdx)
                            || (i + 2 == emptyIdx && boardArr[i+1].startsWith("b"));
                    if (!canMove) continue;

                    // Rule 1: landing cell must not have a non-done white marble right of it
                    int land = emptyIdx;
                    boolean blockedByRule1 = (land + 1 < boardArr.length)
                            && boardArr[land+1].startsWith("w")
                            && !done.contains(boardArr[land+1]);
                    if (!blockedByRule1) result.add(boardArr[i]);
                }
            } else {
                // scan left-to-right so the marble nearest the gap is tried first
                for (int i = 0; i < boardArr.length; i++) {
                    if (!boardArr[i].startsWith("b")) continue;
                    boolean canMove = (i - 1 == emptyIdx)
                            || (i - 2 == emptyIdx && boardArr[i-1].startsWith("w"));
                    if (!canMove) continue;

                    int land = emptyIdx;
                    boolean blockedByRule1 = (land - 1 >= 0)
                            && boardArr[land-1].startsWith("b")
                            && !done.contains(boardArr[land-1]);
                    if (!blockedByRule1) result.add(boardArr[i]);
                }
            }
        }
        return result;
    }

    // ─── autoSolve ── Backtracking with explicit ArrayDeque stack ────────────
    /**
     * DFS / Backtracking using two parallel ArrayDeques:
     *
     *   stack          – BoardState snapshots (board array + doneMarbles taken
     *                    BEFORE the move that led to each level)
     *   candidateStack – ordered list of marble IDs still untried at each level
     *
     * Forwarding  : restore frame's snapshot → apply next candidate → push new frame
     * Backtracking: candidateIdx exhausted → pop both stacks → step-- → continue
     * No solution : stack becomes empty without reaching isGameOver()
     */
    public void autoSolve(marbleBoard board, int startStep) {
        int step = startStep;

        // ── Two parallel explicit ArrayDeque stacks ──
        Deque<BoardState>   stack          = new ArrayDeque<>();
        Deque<List<String>> candidateStack = new ArrayDeque<>();

        // ── Push root frame (state before any auto-move) ──
        BoardState   rootState      = new BoardState(board.getBoard(), board.getDoneMarbles(), null);
        List<String> rootCandidates = buildCandidateList(board.getBoard(), board.getDoneMarbles(), null);

        stack.push(rootState);
        candidateStack.push(rootCandidates);

        boolean solved = false;

        // ── Main DFS loop ──
        while (!stack.isEmpty()) {

            BoardState   topFrame    = stack.peek();          // current DFS frame
            List<String> candidates  = candidateStack.peek(); // moves still to try

            // ── Backtracking condition: all candidates for this frame exhausted ──
            if (topFrame.candidateIdx >= candidates.size()) {
                stack.pop();
                candidateStack.pop();

                if (!stack.isEmpty()) {
                    // Restore board to the snapshot stored in the new top frame
                    board.restoreState(stack.peek());
                }
                step--;   // undo the step counter for the move we're un-doing
                continue;
            }

            // ── Forwarding: pick the next untried marble ──
            String marble = candidates.get(topFrame.candidateIdx);
            topFrame.candidateIdx++;   // advance pointer so we don't retry it

            // Restore board to this frame's snapshot, then apply the move
            board.restoreState(topFrame);
            boolean moved = board.move(marble, true);
            if (!moved) continue;   // safety guard – shouldn't happen

            // ── Print step ──
            System.out.print("Auto " + step + " >> ");
            board.printBoard();
            step++;

            // ── Win condition ──
            if (board.isGameOver()) {
                System.out.println("Done !!");
                solved = true;
                break;
            }

            // ── Push new frame for the state just reached ──
            BoardState   newFrame      = new BoardState(board.getBoard(), board.getDoneMarbles(), marble);
            List<String> newCandidates = buildCandidateList(board.getBoard(), board.getDoneMarbles(), marble);

            stack.push(newFrame);
            candidateStack.push(newCandidates);
            // If newCandidates is empty the next iteration will immediately backtrack.
        }

        if (!solved) {
            System.out.println("No solution !!");
        }
    }

    // ─── menu ─────────────────────────────────────────────────────────────────
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
        new MainApplication().menu();
    }
}
