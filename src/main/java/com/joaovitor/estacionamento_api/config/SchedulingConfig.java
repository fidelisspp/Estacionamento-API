package com.joaovitor.estacionamento_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Liga as tarefas agendadas (@Scheduled), como a limpeza das chaves de idempotência vencidas.
 */
@EnableScheduling
@Configuration
public class SchedulingConfig {
}
