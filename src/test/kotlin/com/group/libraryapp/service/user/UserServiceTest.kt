package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService
) {

    @AfterEach
    fun clear() {
        userRepository.deleteAll()
    }

    @Test
    fun saveUserTest() {
        //given
        val request = UserCreateRequest("현식", null)
        //when
        userService.saveUser(request)
        //then
        val results = userRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("현식")
        assertThat(results[0].age).isNull()
    }

    @Test
    fun getUsersTest() {
        //given
        userRepository.saveAll(listOf(
            User("A", 20),
            User("B", null),
        ))
        //when
        val results = userService.getUsers()
        //then
        assertThat(results).hasSize(2)
        assertThat(results).extracting("name").containsExactlyInAnyOrder("A", "B")
        assertThat(results).extracting("age").containsExactlyInAnyOrder(20, null)
    }

    @Test
    fun updateUserNameTest() {
        //given
        val savedUser = userRepository.save(User("A", null))
        val request = UserUpdateRequest(savedUser.id, "B")
        //when
        userService.updateUserName(request)
        //then
        val result = userRepository.findAll()[0]
        assertThat(result.name).isEqualTo("B")
    }

    @Test
    fun deleteUserTest() {
        //given
        val savedUser = userRepository.save(User("A", null))
        //when
        userService.deleteUser(savedUser.name)
        //then
        assertThat(userRepository.findAll()).isEmpty()
    }

    @Test
    fun deleteUserExceptionTest() {
        //given
        userRepository.save(User("A", null))
        //when && then
        assertThrows<IllegalArgumentException> {
            userService.deleteUser("B")
        }
    }
}