package com.sda.testing.configuration;

import com.sda.testing.model.Employee;
import com.sda.testing.model.EmployeeLevel;
import com.sda.testing.model.Team;
import com.sda.testing.repository.EmployeeRepository;
import com.sda.testing.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
@RequiredArgsConstructor
@Profile(value = {"production"})
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        teamRepository.save(Team.builder().name("Team A").build());
        teamRepository.save(Team.builder().name("Team B").build());
        teamRepository.save(Team.builder().name("Team Best").build());

        employeeRepository.save(Employee.builder()
                .firstName("Pawel")
                .lastName("Gawel")
                .level(EmployeeLevel.WORKER)
                .salary(3001).build());
        employeeRepository.save(Employee.builder()
                .firstName("Marian")
                .lastName("Nowak")
                .level(EmployeeLevel.WORKER)
                .salary(3050).build());
        employeeRepository.save(Employee.builder()
                .firstName("Lukasz")
                .lastName("Czegoszukasz")
                .level(EmployeeLevel.WORKER)
                .salary(3302).build());
        employeeRepository.save(Employee.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .level(EmployeeLevel.MANAGER)
                .salary(5000).build());
        employeeRepository.save(Employee.builder()
                .firstName("Ryszard")
                .lastName("Zgrunwaldu")
                .level(EmployeeLevel.LEAD)
                .salary(6000).build());
        employeeRepository.save(Employee.builder()
                .firstName("Mariusz")
                .lastName("Roztocki")
                .level(EmployeeLevel.LEAD)
                .salary(6000).build());
        employeeRepository.save(Employee.builder()
                .firstName("Piter")
                .lastName("Pan")
                .level(EmployeeLevel.EXECUTIVE)
                .salary(5500).build());
        employeeRepository.save(Employee.builder()
                .firstName("Filip")
                .lastName("Stokrotka")
                .level(EmployeeLevel.WORKER)
                .salary(4900).build());
    }
}
