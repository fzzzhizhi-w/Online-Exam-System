package com.jepai.exam.modules.grade.listener;

import com.jepai.exam.modules.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 评卷消息监听器
 * 仅在 exam.rabbitmq.enabled=true 时启用，避免 RabbitMQ 未部署时频繁报错
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "exam.rabbitmq.enabled", havingValue = "true")
public class GradeMessageListener {

    private final GradeService gradeService;

    @RabbitListener(queues = "${rabbitmq.grading-queue:exam.grading.queue}")
    public void onGradingMessage(Long recordId) {
        log.info("收到评卷消息，recordId: {}", recordId);
        gradeService.autoGrade(recordId);
    }
}
