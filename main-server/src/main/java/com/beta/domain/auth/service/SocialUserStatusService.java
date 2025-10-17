package com.beta.domain.auth.service;

import com.beta.common.exception.auth.NameDuplicateException;
import com.beta.common.exception.auth.PersonalInfoAgreementRequiredException;
import com.beta.common.exception.auth.UserSuspendedException;
import com.beta.common.exception.auth.UserWithdrawnException;
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

    public void validateAgreePersonalInfo(Boolean agreePersonalInfo) {
        if(agreePersonalInfo == null || !agreePersonalInfo){
            throw new PersonalInfoAgreementRequiredException("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다.");
        }
    }

    public void validateNameDuplicate(boolean nameDuplicate) {
        if(nameDuplicate){
            throw new NameDuplicateException("이미 존재하는 이름입니다.");
        }
    }
}
