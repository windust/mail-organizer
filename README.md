
# Mail Organizer

Mail Organizer is a Java-based application designed to help manage and organize emails, particularly for handling spam and other unwanted messages. The application uses IMAP to connect to email servers, retrieve messages, and classify them using machine learning models.

## BACK UP YOUR MAIL BEFORE USING THIS SOFTWARE
- LLMs do Make Mistakes
- This helps you pre-sort tons of emails
  - You can choose to keep those
  
## THIS SOFTWARE WILL CREATE NEW IMAP FOLDERS
- This software will create new IMAP folders
- You will need to manually subscribe to them

## THIS SOFTWARE CAN TAKE A WHILE TO RUN
- IMAP synchronization can take a while, especially if you have a large number of emails
- You may need to wait a while for the application to finish processing
- You may need to configure your mail client to re-sync the contents of your inbox (and may take a while)

## Features

- **IMAP Integration**: Connects to email servers using IMAP to retrieve messages.
- **Email Classification**: Uses machine learning models to classify emails based on their content.
- **Spam Detection**: Identifies and moves spam emails to a designated folder.
- **Batch Processing**: Processes emails in batches for efficiency.
- **Customizable Configuration**: Allows customization of classification rules and folder mappings.

## Technologies Used

- **Java**: The primary programming language used for the application.
- **Spring Boot**: Framework for building the application.
- **Kotlin**: Used for some parts of the application.
- **Gradle**: Build tool for managing dependencies and building the project.
- **Reactor**: For reactive programming and handling asynchronous operations.
- **Jakarta Mail**: For handling email operations.
- **Jsoup**: For parsing and manipulating HTML content in emails.

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 7.0 or higher

### Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/windust/mail-organizer.git
   cd mail-organizer
   ```

2. Build the project:
   ```sh
   ./gradlew build
   ```

3. Run the application:
   ```sh
   ./gradlew bootRun
   ```

### Download and Installing Ollama
 - Visit the Ollama website and sign up for an account if you don't have one.
 - Follow the instructions on the website to download the Ollama SDK for your operating system.
 - Install the SDK by following the provided installation guide.


### Make sure you have the latest version of the Llama3 model:
   ```sh
   ollama pull llama3.2:latest
   ```
### Configure the Application
By default the application.yml is configured to use the Llama3 model. If you want to use a different model, you can change the configuration in the `application.yml` file.
1. Open the `src/main/resources/application.yml` file.
2. Locate the `spring.ai.ollama.chat.options.model` property and set it to the desired model name. For example, to use the latest version of Llama3, you can set it as follows:
3. 
   ```yaml
   spring:
     ai:
       ollama:
         chat:
           options:
             model: llama3.2:latest
   ```

3. Ensure you have the necessary API keys and access configured for Ollama.
## Configuration

The application can be configured using a `application.yml` file. Here is an example configuration:

```yaml
imap-configuration:
  host: imap.example.com
  port: 993
  username: your-email@example.com
  password: your-password

classifier-configuration:
  useBody: true
  descriptions:
    Spam: "Unsolicited or irrelevant emails typically sent in bulk,
          often for advertising, phishing, or spreading malware.
          These emails are usually unwanted and may be flagged as junk
          by email systems."
    Flyer: "Email that include sales, percent off, price drops,
          and is usually from known retailers"
    Newsletter: "Regularly sent emails that provide updates, articles,
        or news about a specific topic, organization, or interest.
        These are typically informational and targeted at a subscribed audience."
    Order: "Email contains order number information,
        or purchase information, or payment information, or status updates"
```
### The UseBody configuration
The `useBody` configuration determines whether the email body should be used for classification. If set to `true`, the application will analyze the content of the email body in addition to the subject line. This can improve classification accuracy, especially for emails with ambiguous subjects.

When is false, the application will only use the subject line for classification. This may be faster but could lead to less accurate results in some cases.


## Usage

Once the application is running, it will connect to the specified IMAP server, retrieve emails, and classify them based on the configured rules. Classified emails will be moved to the appropriate folders.

**Warning**: IMAP synchronization can take a while, especially if you have a large number of emails. Please be patient during this process.

**Important**: Before running this project, make sure to backup your mailbox to prevent any accidental data loss.

## License

This project is licensed under the Apache License 2.0. See the `LICENSE` file for details.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any improvements or bug fixes.
