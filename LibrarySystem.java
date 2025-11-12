import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Simple Library Management System (console)
 * - Beginner friendly (uses Scanner)
 * - Admin password: admin123
 * - Default loan period: 14 days
 * - Fine per day late: 5.0
 *
 * Data is stored in memory (lost when program exits).
 */
public class LibrarySystem {

    // ---------- Data classes ----------
    static class Book {
        int id;
        String title;
        String author;
        int copies;

        Book(int id, String title, String author, int copies) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.copies = copies;
        }

        @Override
        public String toString() {
            return String.format("ID:%d | \"%s\" by %s | copies: %d", id, title, author, copies);
        }
    }

    static class IssueRecord {
        int bookId;
        String studentName;
        LocalDate issueDate;
        LocalDate dueDate;

        IssueRecord(int bookId, String studentName, LocalDate issueDate, LocalDate dueDate) {
            this.bookId = bookId;
            this.studentName = studentName;
            this.issueDate = issueDate;
            this.dueDate = dueDate;
        }

        @Override
        public String toString() {
            return String.format("BookID:%d | Student:%s | Issued:%s | Due:%s",
                    bookId, studentName, issueDate, dueDate);
        }
    }

    // ---------- Library data ----------
    private Map<Integer, Book> books = new LinkedHashMap<>(); // keep insertion order
    private List<IssueRecord> issued = new ArrayList<>();
    private int nextBookId = 1;
    private final double finePerDay = 5.0;
    private final int defaultLoanDays = 14;
    private final Scanner sc = new Scanner(System.in);

    // ---------- Constructor ----------
    public LibrarySystem() {
        seedSampleData();
    }

    // ---------- Seed sample books ----------
    private void seedSampleData() {
        addBookInternal("Introduction to Algorithms", "Cormen", 2);
        addBookInternal("Effective Java", "Joshua Bloch", 1);
        addBookInternal("Clean Code", "Robert C. Martin", 3);
        addBookInternal("Head First Java", "Kathy Sierra", 2);
    }

    private void addBookInternal(String title, String author, int copies) {
        Book b = new Book(nextBookId++, title, author, copies);
        books.put(b.id, b);
    }

    // ---------- Admin operations ----------
    private void adminAddBook() {
        System.out.println("\n-- Add New Book --");
        String title = readNonEmptyLine("Enter title: ");
        String author = readNonEmptyLine("Enter author: ");
        int copies = readIntPositive("Enter number of copies: ");
        addBookInternal(title, author, copies);
        System.out.println("Book added successfully.");
    }

    private void adminRemoveBook() {
        System.out.println("\n-- Remove Book --");
        listAllBooks();
        if (books.isEmpty()) return;
        int id = readInt("Enter book ID to remove: ");
        Book b = books.get(id);
        if (b == null) {
            System.out.println("No book with that ID.");
            return;
        }
        long issuedCount = 0;
        for (IssueRecord r : issued) {
            if (r.bookId == id) issuedCount++;
        }
        if (issuedCount > 0) {
            System.out.println("Cannot remove. There are currently " + issuedCount + " issued copy(ies).");
            return;
        }
        books.remove(id);
        System.out.println("Removed book: " + b.title);
    }

    private void adminViewIssuedBooks() {
        System.out.println("\n-- Issued Books --");
        if (issued.isEmpty()) {
            System.out.println("No books are currently issued.");
            return;
        }
        for (IssueRecord r : issued) {
            System.out.println("  " + r);
        }
    }

    // ---------- Student operations ----------
    private void studentIssueBook() {
        System.out.println("\n-- Issue Book --");
        if (books.isEmpty()) {
            System.out.println("Library has no books.");
            return;
        }
        String student = readNonEmptyLine("Enter your name: ");
        listAllBooks();
        int id = readInt("Enter book ID to issue: ");
        Book b = books.get(id);
        if (b == null) {
            System.out.println("No book with that ID.");
            return;
        }
        if (b.copies <= 0) {
            System.out.println("No copies available right now.");
            return;
        }
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(defaultLoanDays);
        IssueRecord rec = new IssueRecord(id, student, issueDate, dueDate);
        issued.add(rec);
        b.copies = b.copies - 1;
        System.out.println("Book issued successfully. Due date: " + dueDate);
    }

    private void studentReturnBook() {
        System.out.println("\n-- Return Book --");
        if (issued.isEmpty()) {
            System.out.println("No issued books exist.");
            return;
        }
        String student = readNonEmptyLine("Enter your name: ");
        int id = readInt("Enter book ID to return: ");
        IssueRecord found = null;
        for (IssueRecord r : issued) {
            if (r.bookId == id && r.studentName.equalsIgnoreCase(student)) {
                found = r;
                break;
            }
        }
        if (found == null) {
            System.out.println("No matching issue record found for you and that book ID.");
            return;
        }
        LocalDate returnDate = LocalDate.now();
        long daysLate = ChronoUnit.DAYS.between(found.dueDate, returnDate);
        double fine = 0.0;
        if (daysLate > 0) fine = daysLate * finePerDay;
        Book b = books.get(id);
        if (b != null) b.copies = b.copies + 1;
        issued.remove(found);
        System.out.println("Book returned on: " + returnDate);
        if (fine > 0) {
            System.out.printf("Late by %d day(s). Fine = %.2f%n", daysLate, fine);
        } else {
            System.out.println("Returned on time. No fine.");
        }
    }

    // ---------- Search & list ----------
    private void searchBooks() {
        System.out.println("\n-- Search Books --");
        if (books.isEmpty()) {
            System.out.println("Library has no books.");
            return;
        }
        String choice;
        while (true) {
            choice = readNonEmptyLine("Search by (1) Title or (2) Author? Enter 1 or 2: ");
            if ("1".equals(choice) || "2".equals(choice)) break;
            System.out.println("Please enter 1 or 2.");
        }
        String query = readNonEmptyLine("Enter search text: ").toLowerCase();
        List<Book> results = new ArrayList<>();
        for (Book b : books.values()) {
            if ("1".equals(choice) && b.title.toLowerCase().contains(query)) results.add(b);
            if ("2".equals(choice) && b.author.toLowerCase().contains(query)) results.add(b);
        }
        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            System.out.println("Search results:");
            for (Book b : results) System.out.println("  " + b);
        }
    }

    private void listAllBooks() {
        System.out.println("\n-- Books in Library --");
        if (books.isEmpty()) {
            System.out.println("No books in library.");
            return;
        }
        for (Book b : books.values()) System.out.println("  " + b);
    }

    // ---------- Input helpers ----------
    private String readNonEmptyLine(String prompt) {
        String line;
        while (true) {
            System.out.print(prompt);
            line = sc.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("Input cannot be empty. Try again.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private int readIntPositive(String prompt) {
        while (true) {
            int x = readInt(prompt);
            if (x > 0) return x;
            System.out.println("Please enter a positive integer.");
        }
    }

    // ---------- Menus & run ----------
    private boolean adminLogin() {
        System.out.print("Enter admin password: ");
        String pw = sc.nextLine().trim();
        return "admin123".equals(pw);
    }

    private void adminMenu() {
        System.out.println("Admin logged in.");
        while (true) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1) Add Book");
            System.out.println("2) Remove Book");
            System.out.println("3) View Issued Books");
            System.out.println("4) List All Books");
            System.out.println("0) Logout");
            int c = readInt("Choose: ");
            switch (c) {
                case 1: adminAddBook(); break;
                case 2: adminRemoveBook(); break;
                case 3: adminViewIssuedBooks(); break;
                case 4: listAllBooks(); break;
                case 0: System.out.println("Admin logged out."); return;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void studentMenu() {
        while (true) {
            System.out.println("\nStudent Menu:");
            System.out.println("1) Issue Book");
            System.out.println("2) Return Book");
            System.out.println("3) Search Books");
            System.out.println("4) List All Books");
            System.out.println("0) Back to Main Menu");
            int c = readInt("Choose: ");
            switch (c) {
                case 1: studentIssueBook(); break;
                case 2: studentReturnBook(); break;
                case 3: searchBooks(); break;
                case 4: listAllBooks(); break;
                case 0: return;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public void run() {
        System.out.println("=== Welcome to Simple Library Management System ===");
        while (true) {
            System.out.println("\nMain Menu:");
            System.out.println("1) Admin Login");
            System.out.println("2) Student Login");
            System.out.println("3) Search Books");
            System.out.println("4) List All Books");
            System.out.println("0) Exit");
            int choice = readInt("Choose: ");
            switch (choice) {
                case 1:
                    if (adminLogin()) adminMenu();
                    else System.out.println("Admin login failed.");
                    break;
                case 2:
                    studentMenu();
                    break;
                case 3:
                    searchBooks();
                    break;
                case 4:
                    listAllBooks();
                    break;
                case 0:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        new LibrarySystem().run();
    }
}
