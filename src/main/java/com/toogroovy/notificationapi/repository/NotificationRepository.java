package com.toogroovy.notificationapi.repository;

import com.toogroovy.notificationapi.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByTriggered(boolean status);
    List<Notification> findByAsset(String asset);
    List<Notification> findByAssetType(String assetType);
}