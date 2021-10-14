package com.bbs.demo.entity;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
@JsonIgnoreProperties({"enabled","accountNonExpired","accountNonExpired","accountNonLocked","CredentialsNonExpired","authorities"})
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;//权限0-用户，1-管理员，2-版主
    private int status;
    private String activation_code;
    private String header_url;
    private Date create_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getActivationCode() {
        return activation_code;
    }

    public void setActivationCode(String activation_code) {
        this.activation_code = activation_code;
    }

    public String getHeaderUrl() {
        return header_url;
    }

    public void setHeaderUrl(String header_url) {
        this.header_url = header_url;
    }

    public Date getCreateTime() {
        return create_time;
    }

    public void setCreateTime(Date create_time) {
        this.create_time = create_time;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", salt='" + salt + '\'' +
                ", email='" + email + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", activationCode='" + activation_code + '\'' +
                ", headerUrl='" + header_url + '\'' +
                ", createTime=" + create_time +
                '}';
    }


}
