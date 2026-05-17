package com.parkcore.backend.config;

import com.parkcore.backend.simulation.SimulationEngine;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimulationConfig {
    @Bean
    public CommandLineRunner startSimulation(SimulationEngine engine) {
        return args -> {
            System.out.println("🚗 Starting parking simulation...");
            engine.start();
        };
    }
}