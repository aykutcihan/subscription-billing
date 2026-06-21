package com.cadence.subscription.repository;

import com.cadence.subscription.domain.Subscription;
import com.cadence.subscription.domain.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
}
