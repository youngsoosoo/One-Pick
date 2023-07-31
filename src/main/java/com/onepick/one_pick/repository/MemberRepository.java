package com.onepick.one_pick.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.onepick.one_pick.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

        //mid를 매개변수로 받아서
        //social의 값이 false인 데이터를 전부 찾아오는 메서드
        // @Query("select m from Member m where m.email = :email and m.social = false")
        // Optional<Member> getWithRoles(String email);

        @EntityGraph(attributePaths = "roleSet", type = EntityGraph.EntityGraphType.LOAD)
        @Query("select m from Member m where m.memberEmail = :memberEmail")
        Optional<Member> findByEmail(@Param("memberEmail") String memberEmail);

        //이메일 중복확인
        boolean existsByMemberEmail(String memberEmail);
}

