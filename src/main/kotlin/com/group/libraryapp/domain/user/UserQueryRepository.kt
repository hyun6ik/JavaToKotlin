package com.group.libraryapp.domain.user

import com.group.libraryapp.domain.user.QUser.user
import com.group.libraryapp.domain.user.loanhistory.QUserLoanHistory.userLoanHistory
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class UserQueryRepository(
    private val queryFactory: JPAQueryFactory
) {

    fun findAllWithHistories(): List<User> {
        return queryFactory
            .selectFrom(user)
            .distinct()
            .leftJoin(userLoanHistory)
            .on(userLoanHistory.user.id.eq(user.id))
            .fetchJoin()
            .fetch()
    }
}