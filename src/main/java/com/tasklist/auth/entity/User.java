package com.tasklist.auth.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "USER_DATA")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column
    private String email;

    @Column
    private String password;

    @Column
    private String username;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Activity activity;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USER_ROLE",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id"))
    private Set<Role> roles = new HashSet<>();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user=(User) obj;
        return email.equalsIgnoreCase(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

}
