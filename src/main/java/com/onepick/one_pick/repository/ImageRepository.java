package com.onepick.one_pick.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.onepick.one_pick.entity.Image;
import com.onepick.one_pick.entity.Member;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByMember(Member member);
}
