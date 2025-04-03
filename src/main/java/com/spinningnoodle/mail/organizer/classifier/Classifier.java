package com.spinningnoodle.mail.organizer.classifier;

import com.spinningnoodle.mail.organizer.model.ClassifierConfiguration;
import com.spinningnoodle.mail.organizer.model.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
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
    private final ChatModel chatModel;
    private final String templateContent;
    private final ClassifierConfiguration configuration;

    /**
     * Constructor to initialize Classifier with chat model and configuration.
     * Reads the prompt template content from the classpath resource.
     *
     * @param chatModel the chat model to use for classification
     * @param configuration the classifier configuration
     * @throws IOException if there is an error reading the prompt template file
     */
    public Classifier(ChatModel chatModel, ClassifierConfiguration configuration) throws IOException {
        this.chatModel = chatModel;
        this.configuration = configuration;
        Path path = new ClassPathResource("prompt.st").getFile().toPath();
        templateContent = new String(Files.readAllBytes(path));
    }

    /**
     * Classifies the given email using the chat model and returns the classification result.
     *
     * @param email the email to classify
     * @return a Mono emitting the classification result
     */
    public Mono<String> classify(Email email) {
        PromptTemplate promptTemplate = new PromptTemplate(templateContent);
        Prompt prompt = promptTemplate.create(Map.of(
                        "email", email.toString(),
                        "classifier", configuration.forPrompt()
                )
        );

        String llmResponse = chatModel.call(prompt).getResult().getOutput().toString();
        String mailType = configuration.identify(llmResponse);
        return Mono.just(mailType);
    }

    /**
     * Checks if the classifier needs the body content of the email for classification.
     *
     * @return true if the body content is needed, false otherwise
     */
    public boolean needsBody() {
        return configuration.isUseBody();
    }

    public boolean dryRun() {
        return configuration.isDryRun();
    }
}