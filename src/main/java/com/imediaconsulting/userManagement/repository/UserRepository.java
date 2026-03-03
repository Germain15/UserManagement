/**
 * Accès base de données pour l'entité User.
 * Les méthodes sont générées par Spring Data JPA à partir de leur nom.
 */

package com.imediaconsulting.userManagement.repository;

import com.imediaconsulting.userManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}