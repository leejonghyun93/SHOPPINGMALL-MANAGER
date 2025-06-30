package org.kosa.shoppingmaillmanager.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private String userId;
    private String nickname;
}