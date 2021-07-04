package com.sda.testing.service;

import com.sda.testing.exception.InvalidOperation;
import com.sda.testing.model.*;
import com.sda.testing.repository.EmployeeRepository;
import com.sda.testing.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;

    /**
     * Return sum of all salaries.
     *
     * @return sum of salaries.
     */
    public double summarizeSalaries() {
        return sumOfSalaries(employeeRepository.findAll());
    }

    /**
     * Return sum of salaries on a given level.
     *
     * @param level - employee level of which salaries should be summarized. Can't be null.
     * @return sum of salaries.
     */
    public double salaries(EmployeeLevel level) {
        return sumOfSalaries(employeeRepository.findAllByLevel(level));
    }

    private double sumOfSalaries(List<Employee> employeeList) {
        return employeeList.stream().mapToDouble(Employee::getSalary).sum();
    }

    /**
     * Adds new Employee to the company.
     *
     * @param employeeDto - dto containing all employee information.
     * @throws InvalidOperation can be thrown if name, surname, or salary has not been provided.
     */
    public void hireEmployee(EmployeeDto employeeDto) throws InvalidOperation {
        if (validateHiredEmployee(employeeDto)) {
            employeeRepository.save(
                    Employee.builder()
                            .firstName(employeeDto.getName())
                            .lastName(employeeDto.getSurname())
                            .salary(employeeDto.getGrossSalary())
                            .build()
            );
        } else {
            throw new InvalidOperation();
        }
    }

    private boolean validateHiredEmployee(EmployeeDto employeeDto) {
        return Objects.nonNull(employeeDto)
                && Objects.nonNull(employeeDto.getName())
                && Objects.nonNull(employeeDto.getSurname());
    }

    /**
     * Fire employee with given id.
     *
     * @param employeeId - employee which should be fired.
     */
    public void fireEmployee(Long employeeId) {
        employeeRepository.findById(employeeId).ifPresent(employeeRepository::delete);
    }

    /**
     * Create an empty team.
     *
     * @param teamName - name of the team. Name has to be unique.
     * @throws InvalidOperation - exception might be thrown if team name is not unique.
     */
    public void createTeam(String teamName) throws InvalidOperation {
        if (Objects.nonNull(teamName) && teamRepository.findByTeamName(teamName).isPresent()) {
            teamRepository.save(Team.builder()
                    .name(teamName)
                    .build());
        } else {
            throw new InvalidOperation();
        }
    }

    /**
     * Remove team with given name.
     *
     * @param teamName name of the team to remove.
     * @throws InvalidOperation - if team name is incorrect/or null or team does not exist, exception will be thrown.
     */
    public void removeTeam(String teamName) throws InvalidOperation {
        Optional<Team> teamOptional = teamRepository.findByTeamName(teamName);
        if (Objects.nonNull(teamName) && teamOptional.isPresent()) {
            teamRepository.delete(teamOptional.get());
        } else {
            throw new InvalidOperation();
        }
    }

    /**
     * List team names.
     */
    public List<String> listTeams() {
        return teamRepository.findAll().stream().map(Team::getName).collect(Collectors.toList());
    }

    /**
     * Add employee to team. Team might not have more than 6 members. Single Employee can't be in two teams.
     * In team there can be max one Lead and max one Manager.
     *
     * @param employeeId - employee identifier.
     * @param teamName   - team name
     * @throws InvalidOperation - exception might be thrown if this operation is invalid.
     */
    public void addEmployeeToTeam(Long employeeId, String teamName) throws InvalidOperation {
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        Optional<Team> teamOptional = teamRepository.findByTeamName(teamName);

        boolean addedSucessfully = false;

        if (optionalEmployee.isPresent() && teamOptional.isPresent()) {
            Employee employee = optionalEmployee.get();
            Team team = teamOptional.get();

            if (validateAddingEmployeeToTeam(employee, team)) {
                team.getEmployeeSet().add(employee);
                addedSucessfully = true;
            }
        }

        if (!addedSucessfully) {
            throw new InvalidOperation();
        }

    }

    private boolean validateAddingEmployeeToTeam(Employee employee, Team team) {
        return !isEmployeeMemberOfAnyTeam(employee)
                && hasLessThanSixMembers(team)
                && !hasEmployeeOfLevel(team, EmployeeLevel.LEAD)
                && !hasEmployeeOfLevel(team, EmployeeLevel.MANAGER);
    }

    private boolean isEmployeeMemberOfAnyTeam(Employee employee) {
        return Objects.isNull(employee.getTeam());
    }

    private boolean hasLessThanSixMembers(Team team) {
        return team.getEmployeeSet().size() < 6;
    }

    private boolean hasEmployeeOfLevel(Team team, EmployeeLevel level) {
        return team.getEmployeeSet().stream().filter(employee -> employee.getLevel() == level).count() == 1;
    }

    /**
     * Remove employees team assignment.
     *
     * @param employeeId - employee of which team has to be removed.
     */
    public void removeEmployeeFromTeam(Long employeeId) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            Optional<Team> teamOptional = Optional.ofNullable(employee.getTeam());
            teamOptional.ifPresent(team -> team.getEmployeeSet().remove(employee));
        }
    }

    /**
     * Find team with given name and return it's info.
     *
     * @param teamName - name of an existing team.
     * @return transfer object with team info.
     * @throws InvalidOperation can be thrown if team does not exist, it's name is invalid or null.
     */
    public TeamDto teamInfo(String teamName) throws InvalidOperation {
        Optional<Team> teamOptional = teamRepository.findByTeamName(teamName);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();

            Optional<Employee> teamLead = findTeamMemberOfLevel(teamName, EmployeeLevel.LEAD);
            EmployeeDto teamLeadDto;
            teamLeadDto = teamLead.map(this::getEmployeeDto).orElse(null);

            Optional<Employee> teamManager = findTeamMemberOfLevel(teamName, EmployeeLevel.MANAGER);
            EmployeeDto teamManagerDto;
            teamManagerDto = teamManager.map(this::getEmployeeDto).orElse(null);

            List<EmployeeDto> employeeDtoList = getListOfTeamEmployees(team);

            return new TeamDto(team.getName(), employeeDtoList, teamLeadDto, teamManagerDto);
        } else {
            throw new InvalidOperation();
        }
    }

    private List<EmployeeDto> getListOfTeamEmployees(Team team) {
        return team.getEmployeeSet().stream().map(employee ->
                new EmployeeDto(employee.getFirstName(), employee.getLastName(), employee.getSalary())
        ).collect(Collectors.toList());
    }

    private Optional<Employee> findTeamMemberOfLevel(String teamName, EmployeeLevel level) {
        Optional<Team> teamOptional = teamRepository.findByTeamName(teamName);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            return team.getEmployeeSet().stream().filter(employee -> employee.getLevel() == level).findFirst();
        } else {
            return Optional.empty();
        }
    }

    private EmployeeDto getEmployeeDto(Employee employee) {
        return new EmployeeDto(employee.getFirstName(), employee.getLastName(), employee.getSalary());
    }
}
