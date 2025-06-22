package bookstoread;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BookShelfSpec {
    private BookShelf shelf;
    private Book effectiveJava;
    private Book codeComplete;
    private Book mythicalManMonth;
    private Book cleanCode;

    @BeforeEach
    void init() throws Exception {
        shelf = new BookShelf();
        effectiveJava = new Book("Effective Java", "Joshua Bloch",
                LocalDate.of(2008, Month.MAY, 8));
        codeComplete = new Book("Code Complete", "Steve McConnel",
                LocalDate.of(2004, Month.JUNE, 9));
        mythicalManMonth = new Book("The Mythical Man-Month", "Frederick Phillips Brooks",
                LocalDate.of(1975, Month.JANUARY, 1));
        cleanCode = new Book("Clean Code", "Robert C. Martin",
                LocalDate.of(2008, Month.AUGUST, 1));
    }

    @Test
    public void shelfEmptyWhenNoBookAdded() throws Exception {
        List<Book> books = shelf.books();
        assertTrue(books.isEmpty(), () -> "BookShelf should be empty.");
    }

    @Test
    void bookshelfContainsTwoBooksWhenTwoBooksAdded() {
        shelf.add(effectiveJava, codeComplete);
        List<Book> books = shelf.books();
        assertEquals(2, books.size(), () -> "BookShelf should have two books.");
    }

    @Test
    public void emptyBookShelfWhenAddIsCalledWithoutBooks() {
        shelf.add();
        List<Book> books = shelf.books();
        assertTrue(books.isEmpty(), () -> "BookShelf should be empty.");
    }

    @Test
    void booksReturnedFromBookShelfIsImmutableForClient() {
        shelf.add(effectiveJava, codeComplete);
        List<Book> books = shelf.books();
        try {
            books.add(mythicalManMonth);
            fail(() -> "Should not be able to add book to books");
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException, () -> "Should throw UnsupportedOperationException.");
        }
    }

    @Test
    void bookshelfArrangedByBookTitle() {
        shelf.add(effectiveJava, codeComplete, mythicalManMonth);
        List<Book> books = shelf.arrange();
        assertEquals(asList(codeComplete, effectiveJava,mythicalManMonth), books, () -> "Books in a bookshelf should be arranged lexicographically by book title");
    }

    @Test
    void booksInBookShelfAreInInsertionOrderAfterCallingArrange() {
        shelf.add(effectiveJava, codeComplete, mythicalManMonth);
        shelf.arrange();
        List<Book> books = shelf.books();
        assertEquals(asList(effectiveJava, codeComplete, mythicalManMonth), books, () -> "Books in bookshelf are in insertion order");
    }
    @Test
    void bookshelfArrangedByUserProvidedCriteria() {
        shelf.add(effectiveJava, codeComplete, mythicalManMonth);
        List<Book> books = shelf.arrange(Comparator.<Book>naturalOrder().reversed());
        assertEquals(asList(mythicalManMonth, effectiveJava, codeComplete), books, () -> "Books in a bookshelf are arranged in descending order of book title");
    }
    @Test
    @DisplayName("books inside bookshelf are grouped by publication year")
    void groupBooksInsideBookShelfByPublicationYear() {
        shelf.add(effectiveJava, codeComplete, mythicalManMonth, cleanCode);
        Map<Year, List<Book>> booksByPublicationYear = shelf.groupByPublicationYear();
        assertThat(booksByPublicationYear).containsKey(Year.of(2008)).containsValues(Arrays.asList(effectiveJava, cleanCode));
        assertThat(booksByPublicationYear).containsKey(Year.of(2004)).containsValues(Collections.singletonList(codeComplete));
        assertThat(booksByPublicationYear).containsKey(Year.of(1975)).containsValues(Collections.singletonList(mythicalManMonth));
    }
    @Test
    @DisplayName("Les livres à l'intérieur de la bibliothèque sont regroupés selon les critères fournis par l'utilisateur (regroupés par nom d'auteur)")
    void groupBooksByUserProvidedCriteria() {
        shelf.add(effectiveJava, codeComplete, mythicalManMonth, cleanCode);
        Map<String, List<Book>> booksByAuthor = shelf.groupBy(Book::getAuthor);
        assertThat(booksByAuthor).containsKey("Joshua Bloch").containsValues(Collections.singletonList(effectiveJava));
        assertThat(booksByAuthor).containsKey("Steve McConnel").containsValues(Collections.singletonList(codeComplete));
        assertThat(booksByAuthor).containsKey("Frederick Phillips Brooks").containsValues(Collections.singletonList(mythicalManMonth));
        assertThat(booksByAuthor).containsKey("Robert C. Martin").containsValues(Collections.singletonList(cleanCode));
    }

    @Test
    @DisplayName("Trier les livres par date de publication croissante")
    void arrangeBooksByPublicationDateInAscendingOrder() {
        // Arrange - Utilisation des livres déjà définis dans init()
        shelf.add(effectiveJava, codeComplete, mythicalManMonth, cleanCode);

        // Création du comparateur par date de publication
        Comparator<Book> byPublicationDate = Comparator.comparing(Book::getPublishedOn);

        // Act
        List<Book> booksByDate = shelf.arrange(byPublicationDate);

        // Assert - Vérification de l'ordre chronologique
        assertThat(booksByDate)
                .extracting(Book::getTitle)
                .containsExactly(
                        "The Mythical Man-Month",  // 1975 (le plus ancien)
                        "Code Complete",          // 2004
                        "Effective Java",        // 2008-05
                        "Clean Code"              // 2008-08 (le plus récent)
                );
    }

    @Test
    @DisplayName("Trier les livres par date de publication décroissante")
    void arrangeBooksByPublicationDateInDescendingOrder() {
        // Arrange
        shelf.add(effectiveJava, codeComplete, mythicalManMonth);

        // Comparateur par date décroissante
        Comparator<Book> byPublicationDateDesc =
                Comparator.comparing(Book::getPublishedOn).reversed();

        // Act
        List<Book> booksByDateDesc = shelf.arrange(byPublicationDateDesc);

        // Assert
        assertThat(booksByDateDesc)
                .extracting(Book::getPublishedOn)
                .containsExactly(
                        LocalDate.of(2008, Month.MAY, 8),   // Effective Java
                        LocalDate.of(2004, Month.JUNE, 9),  // Code Complete
                        LocalDate.of(1975, Month.JANUARY, 1) // Mythical Man-Month
                );
    }

    @Test
    @DisplayName("Livres avec même année de publication conservent leur ordre")
    void booksWithSamePublicationYearKeepOrder() {
        // Arrange - effectiveJava et cleanCode sont tous deux de 2008
        shelf.add(cleanCode, effectiveJava); // Ajout dans cet ordre

        // Act
        List<Book> booksByYear = shelf.arrange(
                Comparator.comparing(book -> Year.from(book.getPublishedOn()))
        );

        // Assert
        assertThat(booksByYear)
                .as("Doit conserver l'ordre d'insertion pour les livres de même année")
                .containsExactly(cleanCode, effectiveJava);
    }
}