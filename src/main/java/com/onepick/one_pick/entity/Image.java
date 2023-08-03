package com.onepick.one_pick.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "member")
public class Image extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member;
}
