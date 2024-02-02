package com.unilorin.attendance_system.datapersistence_api;

import com.unilorin.attendance_system.datapersistence_api.constants.Role;
import com.unilorin.attendance_system.datapersistence_api.entity.ApplicationUser;
import com.unilorin.attendance_system.datapersistence_api.entity.Student;
import com.unilorin.attendance_system.datapersistence_api.entity.Subject;
import com.unilorin.attendance_system.datapersistence_api.repo.ApplicationUserRepository;
import com.unilorin.attendance_system.datapersistence_api.repo.StudentRepository;
import com.unilorin.attendance_system.datapersistence_api.repo.SubjectRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
@Slf4j
public class DatapersistenceApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(DatapersistenceApiApplication.class, args);
	}
	@Autowired
	private StudentRepository studentRepository;
	@Autowired
	private SubjectRepository subjectRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private ApplicationUserRepository userRepository;
	@Bean
	@Transactional
	public CommandLineRunner preBuild(){
        return  args ->
		{
			// Create demo user
			ApplicationUser user = new ApplicationUser();
			user.setId("001");
			user.setFirstname("Demo");
			user.setLastname("User");
			user.setMiddleName("middlename");
			user.setSchoolEmail("demo@example.com");
			user.setPassword(passwordEncoder.encode("password")); // Encrypt password
			user.setUserRole(List.of(Role.ROLE_LECTURER,Role.ROLE_SUPER_ADMIN));
			user.setAccountNonExpired(true);
			user.setAccountNonLocked(true);
			user.setCredentialsNonExpired(true);
			user.setEnabled(true);
			user = userRepository.save(user);
			log.info("Demo user created with ID: {}", user.getId());
			// Create two students
			log.info("Creating students...");
			// Create two students
			Student student1 = new Student();
			student1.setMatriculationNumber("123456");
			student1.setSchoolEmail("student1@example.com");
			student1.setFirstname("John");
			student1.setLastname("Doe");

			Student student2 = new Student();
			student2.setMatriculationNumber("789012");
			student2.setSchoolEmail("student2@example.com");
			student2.setFirstname("Jane");
			student2.setLastname("Doe");

			student2 = studentRepository.save(student2);
			student1 = studentRepository.save(student1);

			log.info("Students created with IDs: {}, {}", student1.getMatriculationNumber(), student2.getMatriculationNumber());

			log.info("Creating subject...");
			Subject course = new Subject();
			course.setSubjectCode("CS101");
			course.setSubjectTitle("Introduction to Computer Science");
			course.setLecturerInCharge(user);
			course = subjectRepository.save(course);
			log.info("Subject created with ID: {}", course.getSubjectTitle());

			log.info("Updating properties...");
			course.setStudents(new HashSet<>(Set.of(student1,student2)));
			studentRepository.saveAll(List.of(student1,student2));
			log.info("Updated students");
			subjectRepository.save(course);

			log.info("Data initialization complete.");
		};
	}


}
