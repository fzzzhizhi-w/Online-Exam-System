package com.jepai.exam.modules.auth.controller;

import com.jepai.exam.common.exception.BusinessException;
import com.jepai.exam.common.response.Result;
import com.jepai.exam.common.utils.JwtUtils;
import com.jepai.exam.modules.auth.dto.LoginDTO;
import com.jepai.exam.modules.auth.dto.LoginVO;
import com.jepai.exam.modules.user.entity.SysUser;
import com.jepai.exam.modules.user.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 认证控制器
 */
@Api(tags = "登录认证")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserService userService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );
            SysUser user = (SysUser) authentication.getPrincipal();

            // 生成 Token
            String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleCode());

            // 更新最后登录时间
            userService.lambdaUpdate()
                    .eq(SysUser::getId, user.getId())
                    .set(SysUser::getLastLoginTime, LocalDateTime.now())
                    .update();

            // 清除密码信息
            user.setPassword(null);

            LoginVO vo = LoginVO.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(86400L)
                    .userInfo(user)
                    .build();
            return Result.success("登录成功", vo);
        } catch (BadCredentialsException e) {
            throw new BusinessException(401, "用户名或密码错误");
        }
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success("退出成功");
    }
}
