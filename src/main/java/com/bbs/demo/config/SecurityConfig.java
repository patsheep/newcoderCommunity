
package com.bbs.demo.config;

import com.bbs.demo.entity.User;
import com.bbs.demo.service.UserService;
import com.bbs.demo.util.CommunityConstant;
import com.bbs.demo.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;



@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Override
    public void configure(WebSecurity web) {
        //忽略静态资源访问
        web.ignoring().antMatchers("/resources/**");
    }
    //AuthenticationManager:认证的核心接口
    //AuthenticationManagerBuilder:用于构建AuthenticationManager对象工具
    //ProviderManager: AuthenticationManager接口的默认类
/*
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        //内置认证规则
        //auth.userDetailsService(userService).passwordEncoder(new Pbkdf2PasswordEncoder());
        auth.authenticationProvider(new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username=authentication.getName();
                String password=(String) authentication.getCredentials();

                User user=userService.findUserByName(username);
                if(user==null){
                    throw new UsernameNotFoundException("账号不存在");
                }
                password=CommunityUtil.md5(password+user.getSalt());
                if(user.getPassword()!=password){
                    throw new BadCredentialsException("密码错误");
                }
                return new UsernamePasswordAuthenticationToken(user,user.getPassword(), user.getAuthorities());//主要信息,凭证，权限
            }

            @Override
            public boolean supports(Class<?> aClass) {
                return UsernamePasswordAuthenticationToken.class.equals(aClass);
            }
        });
    }*/

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests()
                .antMatchers(
                    "/user/setting",
                    "/user/upload",
                    "/discuss/add",
                    "/comment/add/**",
                    "/letter/**",
                    "/notice/**",
                    "/like",
                    "/follow",
                    "/unfollow"
                ).hasAnyAuthority(
                     AUTHORITY_ADMIN,
                    AUTHORITY_MODERATOR,
                    AUTHORITY_USER
                ).
                antMatchers("/discuss/top","/discuss/wonderful").hasAnyAuthority(AUTHORITY_MODERATOR)
                .antMatchers("/discuss/detail","/data/**").hasAnyAuthority(AUTHORITY_ADMIN).anyRequest().permitAll()
                .and().csrf().disable();
        //权限不够的时候的处理
        http.exceptionHandling()
                //没有登录
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        String xRequestedwith=request.getHeader("x-requested_with");
                        //异步请求
                        if("XMLHttpRequest".equals(xRequestedwith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJsonString(403,"你还没有登录"));
                        }else{
                            System.out.println(request.getContextPath()+"/login");
                            response.sendRedirect(request.getContextPath()+"/login");
                        }
                    }
                })//权限不足
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestedwith=request.getHeader("x-requested_with");
                        //异步请求
                        if("XMLHttpRequest".equals(xRequestedwith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJsonString(403,"你没有访问此功能的权限"));
                        }else{
                            response.sendRedirect(request.getContextPath()+"/denied");
                        }
                    }
                });
        //security的底层默认为拦截/logout,进行退出处理
        //覆盖它默认的逻辑才能执行我们自己的退出代码
        http.logout().logoutUrl("/securitylogout");
    }
}

