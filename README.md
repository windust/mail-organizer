
# Mail Organizer

Mail Organizer is a Java-based application designed to help manage and organize emails, particularly for handling spam and other unwanted messages. The application uses IMAP to connect to email servers, retrieve messages, and classify them using machine learning models.

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

- Java 17 or higher
- Gradle 7.0 or higher

### Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/mail-organizer.git
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

## Usage

Once the application is running, it will connect to the specified IMAP server, retrieve emails, and classify them based on the configured rules. Classified emails will be moved to the appropriate folders.

**Warning**: IMAP synchronization can take a while, especially if you have a large number of emails. Please be patient during this process.

**Important**: Before running this project, make sure to backup your mailbox to prevent any accidental data loss.

## License

This project is licensed under the Apache License 2.0. See the `LICENSE` file for details.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any improvements or bug fixes.
