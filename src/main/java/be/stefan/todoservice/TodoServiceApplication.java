package be.stefan.todoservice;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class TodoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoServiceApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(TodoService todoService) {

		return route().GET("/getAll", serverRequest -> ok().body(todoService.getAll())).build();

	}

}

@RequiredArgsConstructor
@Service
class TodoService {

	private final TodoRepository todoRepository;

	List<TodoEntry> getAll() {
		return todoRepository.findAll();
	}

}

interface TodoRepository extends CrudRepository<TodoEntry, Long> {

	List<TodoEntry> findByCompleteTrue();

	List<TodoEntry> findAll();

}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
class TodoEntry {
	@Id
	private Long id;
	private String title;
	private String content;
	private LocalDateTime dueDate;
	private Boolean complete;

}

@Slf4j
@Service
@AllArgsConstructor
class DummyData {

	private final TodoRepository repository;

	@PostConstruct
	public void initDummyData() {
		repository.save(TodoEntry.builder().complete(false).content("some content").title("title 1")
				.dueDate(LocalDateTime.now().minusDays(1)).id(1L).build());

		repository.save(TodoEntry.builder().complete(false).content("some content2").title("title 2")
				.dueDate(LocalDateTime.now().minusDays(1)).id(2L).build());
		
		log.info("All todos in db: {}", repository.findAll().size());
	}

}
