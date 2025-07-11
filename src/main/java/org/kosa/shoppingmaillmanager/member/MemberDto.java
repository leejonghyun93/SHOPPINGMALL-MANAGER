package org.kosa.shoppingmaillmanager.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString // 이거 추가!
public class MemberDto {
    private String userId;
    private String nickname;
    private boolean isHost;
}