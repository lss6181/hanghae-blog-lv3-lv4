package com.sparta.hanghaebloglv3.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * SignupRequestDto.
 */
@Setter
@Getter
public class SignupRequestDto {
  @NotBlank
  @Size(min = 4, max = 10, message = "최소 4글자에서 최대 10글자까지 입력 가능합니다.")
  @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "알파벳 대소문자와 숫자만 입력 가능합니다.")
  private String username;

  @NotBlank
  @Size(min = 8, max = 15, message = "최소 8글자에서 최대 15글자까지 입력 가능합니다.")
  // 추가된 요구사항 1: 회원 가입시, 특수문자도 포함하는 정규표현식 작성
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]*$", message = "소문자, 대문자, 숫자, 특수문자 조합의 비밀번호만 입력 가능합니다.")
  private String password;

  // 관리자용 암호입력 시 관리자등록
  private boolean admin = false;
  private String adminToken = "";
}