package mk.ukim.finki.wp.kol2022.g1.service.ServiceImpl;

import mk.ukim.finki.wp.kol2022.g1.model.Employee;
import mk.ukim.finki.wp.kol2022.g1.model.EmployeeType;
import mk.ukim.finki.wp.kol2022.g1.model.Skill;
import mk.ukim.finki.wp.kol2022.g1.model.exceptions.InvalidEmployeeIdException;
import mk.ukim.finki.wp.kol2022.g1.repository.EmployeeRepository;
import mk.ukim.finki.wp.kol2022.g1.repository.SkillRepository;
import mk.ukim.finki.wp.kol2022.g1.service.EmployeeService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EmployeeServiceImpl implements EmployeeService, UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, SkillRepository skillRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Employee> listAll() {
        List<Employee> employees = this.employeeRepository.findAll();
        return employees;
    }

    @Override
    public Employee findById(Long id) {
        Employee employee = this.employeeRepository.findById(id).orElseThrow(InvalidEmployeeIdException::new);
        return employee;
    }

    @Override
    public Employee create(String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        String encryptedPassword = this.passwordEncoder.encode(password);
        List<Skill> allById = this.skillRepository.findAllById(skillId);
        //  Employee(String name, String email, String password, EmployeeType type, List<Skill> skills, LocalDate employmentDate)
        Employee employee = new Employee(name,email,encryptedPassword,type,allById,employmentDate);
        return this.employeeRepository.save(employee);
    }

    @Override
    public Employee update(Long id, String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        Employee employee = this.findById(id);

        employee.setName(name);
        employee.setEmail(email);

        String encryptedPassword = this.passwordEncoder.encode(password);
        employee.setPassword(encryptedPassword);

        employee.setType(type);

        List<Skill> allById = this.skillRepository.findAllById(skillId);
        employee.setSkills(allById);

        employee.setEmploymentDate(employmentDate);

        return this.employeeRepository.save(employee);
    }

    @Override
    public Employee delete(Long id) {
        Employee employee = this.findById(id);
        this.employeeRepository.delete(employee);
        return employee;
    }

    @Override
    public List<Employee> filter(Long skillId, Integer yearsOfService) {
        Skill skill = skillId!=null? this.skillRepository.findById(skillId).orElse(null): null;

        if (skill == null && yearsOfService == null) {
            return listAll();
        } else if (skill == null) {
            return employeeRepository.findByEmploymentDateBefore(LocalDate.now().minusYears(yearsOfService));
        } else if (yearsOfService == null) {
            return employeeRepository.findAllBySkillsContains(skill);
        } else {
            return employeeRepository.findAllBySkillsContainsAndEmploymentDateBefore(skill, LocalDate.now().minusYears(yearsOfService));
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee user = this.employeeRepository.findByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return new User(user.getEmail(), user.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getType().toString())));
    }
}
