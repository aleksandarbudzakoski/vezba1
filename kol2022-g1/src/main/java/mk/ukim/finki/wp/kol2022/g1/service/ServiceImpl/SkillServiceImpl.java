package mk.ukim.finki.wp.kol2022.g1.service.ServiceImpl;

import mk.ukim.finki.wp.kol2022.g1.model.Skill;
import mk.ukim.finki.wp.kol2022.g1.model.exceptions.InvalidEmployeeIdException;
import mk.ukim.finki.wp.kol2022.g1.repository.SkillRepository;
import mk.ukim.finki.wp.kol2022.g1.service.SkillService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    public SkillServiceImpl(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    public Skill findById(Long id) {
        Skill skill = skillRepository.findById(id).orElseThrow(InvalidEmployeeIdException::new);
        return skill;
    }

    @Override
    public List<Skill> listAll() {
        List<Skill> skills = skillRepository.findAll();
        return skills;
    }

    @Override
    public Skill create(String name) {
        Skill skill = new Skill(name);
        return this.skillRepository.save(skill);
    }
}
