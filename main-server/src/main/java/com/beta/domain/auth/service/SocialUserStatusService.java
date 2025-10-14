package com.beta.domain.auth.service;

import com.beta.common.exception.UserSuspendedException;
import com.beta.common.exception.UserWithdrawnException;
import com.beta.domain.auth.User;
import com.beta.infra.auth.entity.UserEntity;
import org.springframework.stereotype.Service;

@Service
public class SocialUserStatusService {
    public boolean isNewUser(User user) {
        return user == null || user.isNewUser();
    }

    public void validateUserStatus(User user) {
        if (user.getStatus().equals(UserEntity.UserStatus.WITHDRAWN.name())) {
            throw new UserWithdrawnException("탈퇴한 사용자입니다.");
        }

        if (user.getStatus().equals(UserEntity.UserStatus.SUSPENDED.name())) {
            throw new UserSuspendedException("정지된 사용자입니다. 관리자에게 문의 하세요.");
        }
    }
}
