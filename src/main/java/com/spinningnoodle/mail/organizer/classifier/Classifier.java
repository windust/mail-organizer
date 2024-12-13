package com.spinningnoodle.mail.organizer.classifier;

import com.spinningnoodle.mail.organizer.model.ClassifierConfiguration;
import com.spinningnoodle.mail.organizer.model.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Classifier {
    private final OllamaChatModel chatModel;
    private final String templateContent;
    private final ClassifierConfiguration configuration;

    public Classifier(OllamaChatModel chatModel, ClassifierConfiguration configuration) throws IOException {
        this.chatModel = chatModel;
        this.configuration = configuration;
        Path path = new ClassPathResource("prompt.st").getFile().toPath();
        templateContent = new String(Files.readAllBytes(path));

    }

    public Mono<String> classify(Email email) {
        PromptTemplate promptTemplate = new PromptTemplate(templateContent);
        Prompt prompt = promptTemplate.create(Map.of(
                "email", email.toString(),
                "classifier", configuration.forPrompt()
                )
        );

        String string = chatModel.call(prompt).getResult().getOutput().toString();
        return Mono.just(configuration.identify(string));
    }
}
