package com.example.community.config;

import com.example.community.common.ApiResult;
import com.example.community.filter.CorsFilter;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.PrintWriter;
import java.util.List;

/**
 * 配置Spring Security
 *
 * @author yao 2022/11/30
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
    @Override
    public void configure(WebSecurity web) {
        // 忽略对静态资源的权限检查
        web.ignoring().antMatchers("/resources/**");
    }

    /**
     * 配置跨域
     * 配置了 但是没有生效
     */
    @Bean
    CorsConfigurationSource configurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
        corsConfiguration.setAllowedHeaders(List.of("Origin, X-Requested-With, Content-Type, Accept"));
        corsConfiguration.setAllowedMethods(List.of("POST, GET, PATCH, DELETE, PUT"));
        corsConfiguration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().configurationSource(configurationSource());
        /*
        授权
         */
        http.cors().configurationSource(configurationSource()).
                and().authorizeRequests()
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
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**",
                        "/actuator/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                .and()
                .addFilterBefore(new CorsFilter(), ChannelProcessingFilter.class)
                // 关闭了凭证生成和检查
                .csrf().disable();

        /*
        没有登陆、权限不足时
         */
        http.exceptionHandling()
                // 没有登陆时的处理
                .authenticationEntryPoint((request, response, e) -> {
                    // axios发请求而不是ajax，获取不到自定义头，ajax的头其实只是一个标记意义无实意
                    String xRequestedWith = request.getHeader("x-requested-with");
                    if ("XMLHttpRequest".equals(xRequestedWith)) {
                        response.setContentType("application/plain;charset=utf-8");
                        PrintWriter writer = response.getWriter();
                        writer.write(ApiResult.fail(403, "用户未登录").toString());
                    } else response.sendRedirect(request.getContextPath() + "/login");
                })
                // 权限不足时的处理
                .accessDeniedHandler((request, response, e) -> {
                    String xRequestedWith = request.getHeader("x-requested-with");
                    if ("XMLHttpRequest".equals(xRequestedWith)) {
                        response.setContentType("application/plain;charset=utf-8");
                        PrintWriter writer = response.getWriter();
                        writer.write(ApiResult.fail(403, "权限不足").toString());
                    } else response.sendRedirect(request.getContextPath() + "/denied");
                });

        /*
        Security底层默认会拦截/logout请求,进行退出处理.
        覆盖它默认的逻辑,才能执行我们自己的退出代码.
        这里只是随便拦截了一个，免得logout被拦截了
         */
        http.logout().logoutUrl("/securitylogout");
    }
}
