package com.example.weightloss.repository;

import com.example.weightloss.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	Optional<AppUser> findByPlatformAndUsername(String platform, String username);

	List<AppUser> findAllByOrderByDisplayNameAscUsernameAsc();
}
