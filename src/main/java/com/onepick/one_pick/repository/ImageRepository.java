package com.onepick.one_pick.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.onepick.one_pick.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
