package com.example.notesplugin.util

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object MyNotifier {

    fun showNotification(project: Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Simple Snippet Notifications")
            .createNotification(message, type)
            .notify(project)
    }
}
