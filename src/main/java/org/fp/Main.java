package org.fp;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    public static void main(String[] args) {
        // Load API key and other values from .env
        Dotenv dotenv = Dotenv.configure().load();

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(dotenv.get("OPENAI_API_KEY"))
                .build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage("Give me a fun Java fact!")
                .model(ChatModel.GPT_4O_MINI)
                .build();

        ChatCompletion completion = client.chat().completions().create(params);
        String output = completion.choices().get(0).message().content().orElse("No response");
        System.out.println("🤖::::::\n " + output);
    }
}