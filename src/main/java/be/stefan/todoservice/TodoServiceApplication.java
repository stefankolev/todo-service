package be.stefan.todoservice;

import static org.springframework.web.servlet.function.ServerResponse.ok;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.servlet.ServletException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
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
    RouterFunction<ServerResponse> routes(TodoHandlerService todoService) {

        return RouterFunctions.route()
                .GET("/getAll", todoService::getAll)
                .GET("/getComplete", todoService::getComplete)
                .GET("/get/{id}", todoService::getById)
                .POST("/todo", todoService::add)
                .build();
        // curl -d'{"title": "curl title", "complete": "true"}' -H"content-type:
        // application/json" http://localhost:8080/todo

    }

}

@RequiredArgsConstructor
@Service
class TodoHandlerService {

    private final TodoRepository todoRepository;

    public ServerResponse getAll(ServerRequest req) {

        return ok().body(todoRepository.findAll());
    }

    public ServerResponse getById(ServerRequest r) {

        Long id = Long.parseLong(r.pathVariable("id"));
        return ok().body(todoRepository.findById(id));
    }

    public ServerResponse getComplete(ServerRequest r) {

        return ok().body(todoRepository.findByCompleteTrue());
    }

    public ServerResponse add(ServerRequest req) throws ServletException, IOException {

        TodoEntry entry = todoRepository.save(req.body(TodoEntry.class));
        URI uri = URI.create("/get/" + entry.getId());

        return ServerResponse.created(uri).body(entry);
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private LocalDateTime dueDate;
    private Boolean complete;

}

@Slf4j
@Service
@RequiredArgsConstructor
class DummyData {

    private final TodoRepository repository;

    @PostConstruct
    public void initDummyData() {

        repository.save(TodoEntry
                .builder()
                .complete(false)
                .content("some content")
                .title("title 1")
                .dueDate(LocalDateTime.now().minusDays(1))
                .build());

        repository.save(TodoEntry
                .builder()
                .complete(true)
                .content("some content2")
                .title("title 2")
                .dueDate(LocalDateTime.now().minusDays(1))
                .id(2L)
                .build());

        log.info("All todos in db: {}", repository.findAll().size());
    }

}