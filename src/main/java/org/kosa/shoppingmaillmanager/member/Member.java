package org.kosa.shoppingmaillmanager.member;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_member")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @Column(name = "USER_ID", length = 50)
    private String userId;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    @Column(name = "PHONE", length = 20)
    private String phone;

    @Column(name = "ZIPCODE", length = 10)
    private String zipcode;

    @Column(name = "ADDRESS", length = 500)
    private String address;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Column(name = "GENDER", length = 1)
    private String gender;

    @Column(name = "SUCCESSION_YN", length = 1)
    private String successionYn;

    @Column(name = "BLACKLISTED", length = 1)
    private String blacklisted;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "SESSION_DATE")
    private LocalDateTime sessionDate;

    @Column(name = "LOGIN_FAIL_CNT")
    private Integer loginFailCnt;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "LAST_LOGIN")
    private LocalDateTime lastLogin;

    @Column(name = "MARKETING_AGREE", length = 1)
    private String marketingAgree;

    @Column(name = "SOCIAL_ID", length = 100)
    private String socialId;

    @Column(name = "MARKETING_AGENT", length = 100)
    private String marketingAgent;

    @Column(name = "GRADE_ID", length = 20)
    private String gradeId; // 추후 MemberGrade와 연관관계 설정 가능

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    @Column(name = "MYADDRESS", length = 200)
    private String myAddress;

    @Column(name = "SECESSION_YN", length = 1)
    private String secessionYn;

    @Column(name = "SECESSION_DATE")
    private LocalDate secessionDate;

    @Column(name = "PROFILE_IMG", length = 255)
    private String profileImg;

    @Column(name = "SOCIAL_TYPE", length = 50)
    private String socialType;

    @Column(name = "nickname", length = 100, unique = true)
    private String nickname;
}
