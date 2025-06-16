// QuizDataInitializer.java
package com.unknown.kimsblog.config;

import com.unknown.kimsblog.model.Quiz;
import com.unknown.kimsblog.model.User;
import com.unknown.kimsblog.model.UserStats;
import com.unknown.kimsblog.repository.QuizRepository;
import com.unknown.kimsblog.repository.UserRepository;
import com.unknown.kimsblog.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class QuizDataInitializer implements CommandLineRunner {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 샘플 데이터가 이미 존재하는지 확인
        if (quizRepository.count() > 0) {
            return; // 이미 데이터가 있으면 초기화하지 않음
        }

        // 샘플 사용자 생성
        User sampleUser1 = createSampleUser("김민수", "minsu@example.com", "password123");
        User sampleUser2 = createSampleUser("이영희", "younghee@example.com", "password123");
        User sampleUser3 = createSampleUser("박개발", "dev.park@example.com", "password123");

        // 샘플 퀴즈 생성
        createSampleQuiz("대한민국의 수도는?", "서울", 10, "지리", Quiz.Difficulty.EASY, sampleUser1);
        createSampleQuiz("1 + 1 = ?", "2", 5, "수학", Quiz.Difficulty.EASY, sampleUser2);
        createSampleQuiz("JavaScript에서 변수를 선언하는 키워드는? (ES6)", "let", 15, "프로그래밍", Quiz.Difficulty.MEDIUM, sampleUser3);
        createSampleQuiz("한국의 가장 긴 강은?", "낙동강", 20, "지리", Quiz.Difficulty.MEDIUM, sampleUser1);
        createSampleQuiz("2의 10제곱은?", "1024", 30, "수학", Quiz.Difficulty.HARD, sampleUser2);
        createSampleQuiz("Spring Framework의 핵심 개념 중 하나는?", "DI", 25, "프로그래밍", Quiz.Difficulty.HARD, sampleUser3);
        createSampleQuiz("세계에서 가장 높은 산은?", "에베레스트", 15, "지리", Quiz.Difficulty.EASY, sampleUser1);
        createSampleQuiz("원주율 파이의 소수점 첫째 자리는?", "1", 10, "수학", Quiz.Difficulty.EASY, sampleUser2);
        createSampleQuiz("HTML의 정식 명칭은?", "HyperText Markup Language", 20, "프로그래밍", Quiz.Difficulty.MEDIUM, sampleUser3);
        createSampleQuiz("조선왕조의 마지막 왕은?", "순종", 25, "역사", Quiz.Difficulty.MEDIUM, sampleUser1);

        System.out.println("샘플 퀴즈 데이터가 초기화되었습니다.");
    }

    private User createSampleUser(String nickname, String email, String password) {
        // 기존 User 엔티티의 Builder 패턴 사용
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password(passwordEncoder.encode(password))
                .build();

        User savedUser = userRepository.save(user);

        // 사용자 통계 초기화
        UserStats stats = new UserStats();
        stats.setUser(savedUser);
        userStatsRepository.save(stats);

        return savedUser;
    }

    private void createSampleQuiz(String question, String answer, int timeLimit,
                                  String category, Quiz.Difficulty difficulty, User creator) {
        Quiz quiz = new Quiz();
        quiz.setQuestion(question);
        quiz.setAnswer(answer);
        quiz.setTimeLimit(timeLimit);
        quiz.setCategory(category);
        quiz.setDifficulty(difficulty);
        quiz.setCreatedBy(creator);
        quiz.setPlayCount(0);
        quiz.setCorrectCount(0);

        quizRepository.save(quiz);
    }
}