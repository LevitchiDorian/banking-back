//package com.example.app;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class AppApplicationTests {
//	@Autowired
//	private UserRepository userRepository;
//
//	@Test
//	void testSaveUser() {
//		User user = new User();
//		user.setUsername("test_user");
//		user.setPassword("password123");
//		user.setRoles(List.of("USER"));
//		userRepository.save(user);
//		assertNotNull(user.getId());
//	}
//}