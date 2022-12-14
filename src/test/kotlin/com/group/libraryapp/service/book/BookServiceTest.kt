package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.time.Duration.Companion.milliseconds

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {

    @AfterEach
    fun clear() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun saveBookTest() {
        //given
        val request = BookRequest("이펙티브 코틀린", BookType.COMPUTER)
        //when
        bookService.saveBook(request)
        //then
        val books = bookRepository.findAll()
        assertThat(books[0].name).isEqualTo("이펙티브 코틀린")
        assertThat(books[0].type).isEqualTo(BookType.COMPUTER)
    }

    @Test
    fun loanBookTest() {
        //given
        bookRepository.save(Book.fixture("이펙티브 코틀린"))
        val savedUser = userRepository.save(
            User(
                "현식",
                null
            )
        )
        val request = BookLoanRequest("현식", "이펙티브 코틀린")
        //when
        bookService.loanBook(request)
        //then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo("이펙티브 코틀린")
        assertThat(results[0].user.id).isEqualTo(savedUser.id)
        assertThat(results[0].user.name).isEqualTo("현식")
        assertThat(results[0].status).isEqualTo(UserLoanStatus.LOANED)
    }

    @Test
    fun loanBookExceptionTest() {
        //given
        val savedBook = bookRepository.save(Book.fixture("이펙티브 코틀린"))
        val savedJavaUser = userRepository.save(
            User(
                "현식",
                null
            )
        )
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedJavaUser, savedBook.name))
        val request = BookLoanRequest("현식", "이펙티브 코틀린")
        //when && then
        assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.apply {
            assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
        }
    }
    
    @Test
    fun returnBookTest() {
        //given
        val savedBook = bookRepository.save(Book.fixture("이펙티브 코틀린"))
        val savedJavaUser = userRepository.save(
            User(
                "현식",
                null
            )
        )
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedJavaUser, savedBook.name))
        val request = BookReturnRequest("현식", "이펙티브 코틀린")
        //when
        bookService.returnBook(request)
        //then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].status).isEqualTo(UserLoanStatus.RETURNED)
    }

    @Test
    fun countLoanedBookTest() {
        //given
        val savedUser = userRepository.save(User("A", null))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser, "책1"),
            UserLoanHistory.fixture(savedUser, "책2", UserLoanStatus.RETURNED),
            UserLoanHistory.fixture(savedUser, "책3", UserLoanStatus.RETURNED),
        ))
        //when
        val result = bookService.countLoanedBook()
        //then
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun getBookStatisticsTest() {
        //given
        bookRepository.saveAll(listOf(
            Book.fixture("책1", BookType.COMPUTER),
            Book.fixture("책1", BookType.COMPUTER),
            Book.fixture("책1", BookType.SCIENCE),
        ))
        //when
        val results = bookService.getBookStatistics()
        //then
        assertThat(results).hasSize(2)
        assertCount(results, BookType.COMPUTER, 2L)
        assertCount(results, BookType.SCIENCE, 1L)
    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Long) {
        assertThat(results.first { result -> result.type == type }.count).isEqualTo(count)
    }
}