package com.example.testcontainer.study;

import com.example.testcontainer.domain.Member;
import com.example.testcontainer.domain.Study;
import com.example.testcontainer.domain.StudyStatus;
import com.example.testcontainer.member.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Testcontainers
@Slf4j
@ContextConfiguration(initializers = StudyServiceTest.ContainerPropertyInitializer.class)
class StudyServiceTest {

    @Mock
    MemberService memberService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    Environment environment;

    @Value("${container.port}") int port;


    @Container
//    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer().withDatabaseName("studytest");
    static GenericContainer postgreSQLContainer = new GenericContainer("postgres")
            .withExposedPorts(5432)
            .withEnv("POSTGRES_DB", "studytest")
            .withEnv("POSTGRES_PASSWORD", "study");

    @BeforeAll
    static void beforeAll() {
        Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);
        postgreSQLContainer.followOutput(logConsumer);
    }

    @BeforeEach
    void beforeEach() {
        System.out.println("==============");
        System.out.println(postgreSQLContainer.getMappedPort(5432));
        System.out.println(postgreSQLContainer.getLogs());

        System.out.println(environment.getProperty("container.port"));
        System.out.println(port);

        studyRepository.deleteAll();
    }

    @Test
    void createNewStudy() {
        // Given
        StudyService studyService = new StudyService(memberService, studyRepository);
        assertNotNull(studyService);

        Member member = new Member();
        member.setId(1L);
        member.setEmail("keesun@email.com");

        Study study = new Study(10, "테스트");

        given(memberService.findById(1L)).willReturn(Optional.of(member));

        // When
        studyService.createNewStudy(1L, study);

        // Then
        assertEquals(1L, study.getOwnerId());
        then(memberService).should(times(1)).notify(study);
        then(memberService).shouldHaveNoMoreInteractions();
    }

    @DisplayName("다른 사용자가 볼 수 있도록 스터디를 공개한다.")
    @Test
    void openStudy() {
        // Given
        StudyService studyService = new StudyService(memberService, studyRepository);
        Study study = new Study(10, "더 자바, 테스트");
        assertNull(study.getOpenedDateTime());

        // When
        studyService.openStudy(study);

        // Then
        assertEquals(StudyStatus.OPENED, study.getStatus());
        assertNotNull(study.getOpenedDateTime());
        then(memberService).should().notify(study);
    }

    static class ContainerPropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of("container.port=" + postgreSQLContainer.getMappedPort(5432))
                    .applyTo(context.getEnvironment());
        }
    }
}