package com.example.hackathon2024;

import com.example.hackathon2024.components.BotCommands;
import com.example.hackathon2024.components.Buttons;
import com.example.hackathon2024.config.BotConfiguration;
import com.example.hackathon2024.model.CryptoCurrency;
import com.example.hackathon2024.model.User;
import com.example.hackathon2024.repository.CryptoRepository;
import com.example.hackathon2024.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class CryptoCurrencyBot extends TelegramLongPollingBot implements BotCommands {

    private final BotConfiguration config;

    private final UserRepository userRepository;
    private final CryptoRepository cryptoRepository;

    public CryptoCurrencyBot(BotConfiguration config, UserRepository userRepository, CryptoRepository cryptoRepository) {
        this.config = config;
        this.userRepository = userRepository;
        try {
            this.execute(new SetMyCommands(LIST_OF_COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        log.info("CryptoCurrencyBot started");
        this.cryptoRepository = cryptoRepository;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        long chatId = 0;
        long userId = 0;
        String userName = "";
        String receivedMessage;

        //если получено сообщение текстом
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getFirstName();

            if (update.getMessage().hasText()) {
                receivedMessage = update.getMessage().getText();
                botAnswerUtils(receivedMessage, chatId, userName);
            }

            //если нажата одна из кнопок бота
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            userId = update.getCallbackQuery().getFrom().getId();
            userName = update.getCallbackQuery().getFrom().getFirstName();
            receivedMessage = update.getCallbackQuery().getData();

            botAnswerUtils(receivedMessage, chatId, userName);
        }

        handleUserMessage(userId, chatId, userName);
    }

    private void botAnswerUtils(String receivedMessage, long chatId, String userName) {
        switch (receivedMessage) {
            case "/start":
                startBot(chatId, userName);
                break;
            case "/help":
                sendHelpText(chatId, HELP_TEXT);
                break;
            case "/percentage":
                handlePercentageButtons(chatId);
                break;
            case "/all":
                //implemented with pagination principle
                displayAllCryptoRecords(chatId, 100);
                break;
            case "3":
                handlePercentageChange(chatId, 3);
                break;
            case "5":
                handlePercentageChange(chatId, 5);
                break;
            case "10":
                handlePercentageChange(chatId, 10);
                break;
            case "15":
                handlePercentageChange(chatId, 15);
                break;
            default:
                break;
        }
    }

    private void startBot(long chatId, String userName) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Hi, " + userName + "! I'm a Crypto Manager.'");
        message.setReplyMarkup(Buttons.inlineMarkup());
        tryExtract(message);
    }

    private void sendHelpText(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        tryExtract(message);
    }

    public void sendAlert(Long chatId, String alertMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(alertMessage);
        tryExtract(message);
    }

    private void handleUserMessage(long userId, long chatId, String userName) {
        if (userRepository.findById(userId).isEmpty()) {
            User user = new User();
            user.setId(userId);
            user.setChat_id(chatId);
            user.setName(userName);
            //as a default percentage
            user.setPercentage(10);
            user.setMsg_numb(1);

            userRepository.save(user);
            log.info("Added to DB: {}", user);
        } else {
            userRepository.updateMsgNumberByUserId(userId);
        }
    }

    private void handlePercentageChange(long userId, double percentage) {
        if (userRepository.findById(userId).isEmpty()) {
            sendAlert(userId, "You should call /start first");
        } else {
            userRepository.updatePercentageById(userId, percentage);
        }
    }

    private void displayAllCryptoRecords(long chatId, int pageSize) {
        List<CryptoCurrency> cryptos = new ArrayList<>(cryptoRepository.findAll());
        int totalPages = (int) Math.ceil((double) cryptos.size() / pageSize);

        for (int pageNumber = 1; pageNumber <= totalPages; pageNumber++) {
            int startIndex = (pageNumber - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, cryptos.size());

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = startIndex; i < endIndex; i++) {
                CryptoCurrency cryptoCurrency = cryptos.get(i);
                stringBuilder
                        .append("Symbol: ")
                        .append(cryptoCurrency.getSymbol())
                        .append(" Price: ")
                        .append(cryptoCurrency.getPrice())
                        .append("\n");
            }

            sendAlert(chatId, stringBuilder.toString());
        }
    }

    private void tryExtract(SendMessage message) {
        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void handlePercentageButtons(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        // Create buttons
        InlineKeyboardButton button3Percent = new InlineKeyboardButton("3%");
        InlineKeyboardButton button5Percent = new InlineKeyboardButton("5%");
        InlineKeyboardButton button10Percent = new InlineKeyboardButton("10%");
        InlineKeyboardButton button15Percent = new InlineKeyboardButton("15%");

        // Set callback data (you can use any unique string for each button)
        button3Percent.setCallbackData("3");
        button5Percent.setCallbackData("5");
        button10Percent.setCallbackData("10");
        button15Percent.setCallbackData("15");

        // Add buttons to the row
        List<InlineKeyboardButton> row1 = Arrays.asList(button3Percent, button5Percent);
        List<InlineKeyboardButton> row2 = Arrays.asList(button10Percent, button15Percent);

        // Add rows to the markup
        markup.setKeyboard(Arrays.asList(row1, row2));

        // Create a message with the markup
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Select a percentage:");
        message.setReplyMarkup(markup);

        // Send the message
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
